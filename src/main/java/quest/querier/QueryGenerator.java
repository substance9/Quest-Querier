package quest.querier;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;

import quest.model.EncWifiData;
import quest.util.AES;
import quest.util.Epoch;

public class QueryGenerator {
    private Epoch epoch;
    private String keyStr;
    private String secret;
    private String encTableName;
    private DbQuerier dbQuerier;
    private EncDataAnalyzer encDataAnalyzer;
    private int maxLocCounter = 4;

    private static String locTraceQueryTemplate = "SELECT enccl FROM %s WHERE encid='%s' AND %s;";
    private static String userTraceQueryTemplate = "SELECT encid FROM %s WHERE %s;";

    public QueryGenerator(Epoch epoch, String keyStr, String secretZStr, String encTableName, DbQuerier dbQuerier, EncDataAnalyzer encDataAnalyzer) {
        this.epoch = epoch;
        this.keyStr = keyStr;
        this.secret = secretZStr;
        this.encTableName = encTableName;
        this.dbQuerier = dbQuerier;
        this.encDataAnalyzer = encDataAnalyzer;
    }

    public ArrayList<Long> getCoveredEpochIds(Timestamp queryStartTimeTs, Timestamp queryEndTimeTs){
        ArrayList<Long> epochIds = new ArrayList<Long>();
        long endEpochId = epoch.getEpochIdByMs(queryEndTimeTs.getTime());
        long currEpochId = epoch.getEpochIdByMs(queryStartTimeTs.getTime());
        epochIds.add(currEpochId);
        if (currEpochId == endEpochId){
            return epochIds;
        }

        boolean foundEnd = false;
        
        while(!foundEnd){
            currEpochId = epoch.getNextEpoch(currEpochId);
            epochIds.add(currEpochId);
            if(currEpochId == endEpochId){
                foundEnd = true;
            }
        }
        return epochIds;
    }

    //Query Type: 1-Location Trace, 2-User Trace, 3-Social Distance, 4-Crowd Flow
    public String getEncQuery(int queryType, String queryStartTimeStr, String queryEndTimeStr, String queryDevice){
        Timestamp startTimeTs = null;
        Timestamp endTimeTs = null;
        
        try{
            startTimeTs = epoch.parseTimeStrToTs(queryStartTimeStr);
            endTimeTs = epoch.parseTimeStrToTs(queryEndTimeStr);
        }
        catch(ParseException e){
            e.printStackTrace();
        }

        String encSqlStr = null;
        if (queryType == 1){
            encSqlStr = generateLocTraceEncQuery(startTimeTs,endTimeTs,queryDevice);
        }
        else if (queryType == 2){
            encSqlStr = generateUserTraceEncQuery(startTimeTs,endTimeTs,queryDevice);
        }
        else if (queryType == 3){
            encSqlStr = generateSocialDistEncQuery(startTimeTs,endTimeTs);
        }
        else if (queryType == 4){
            encSqlStr = generateCrowdFlowEncQuery(startTimeTs,endTimeTs);
        }
        else{
            System.err.println("Unsupported Query Type: " + String.valueOf(queryType));
            return null;
        }
        System.out.println("Encrypted SQL Query: " + encSqlStr);
        return encSqlStr;
    }

    public String generateLocTraceEncQuery(Timestamp queryStartTimeTs, Timestamp queryEndTimeTs, String queryDevice){
        ArrayList<Long> coveredEpochIds = getCoveredEpochIds(queryStartTimeTs,queryEndTimeTs);
        String epochConditionsStr = "(encd=";

        for (int i = 0; i < coveredEpochIds.size(); i++){
            long epochId = coveredEpochIds.get(i);
            String encEpochIdStr = AES.encrypt(String.valueOf(epochId));
            epochConditionsStr = epochConditionsStr + "'" + encEpochIdStr + "'";

            //if this is the last epoch id, adds ')'. Otherwise, adds " OR encd="
            if (i == coveredEpochIds.size() - 1){
                epochConditionsStr = epochConditionsStr + ")";
            }
            else{
                epochConditionsStr = epochConditionsStr + " OR encd=";
            }
        }

        String encIdStr = AES.concatAndEncrypt(queryDevice,"1",secret);

        String locTraceQuery = String.format(locTraceQueryTemplate, encTableName, encIdStr, epochConditionsStr);

        return locTraceQuery;
    }

    public String generateUserTraceEncQuery(Timestamp queryStartTimeTs, Timestamp queryEndTimeTs, String queryDevice){
        //Run location trace query first to get the list of locations
        String encLocTraceSqlStr = generateLocTraceEncQuery(queryStartTimeTs,queryEndTimeTs,queryDevice);
        ArrayList<EncWifiData> encResults = dbQuerier.execQuery(1, encLocTraceSqlStr);
        String[] locArray = encDataAnalyzer.getLocTraces(encResults);
        
        String conditionsStr = "(";

        ArrayList<Long> coveredEpochIds = getCoveredEpochIds(queryStartTimeTs,queryEndTimeTs);
        //For each covered epoch, each location, try 1-max to get A_L
        for (int i = 0; i < coveredEpochIds.size(); i++){
            long epochId = coveredEpochIds.get(i);
            String encEpochIdStr = AES.encrypt(String.valueOf(epochId));
            conditionsStr = conditionsStr + " ((encd=" + "'" + encEpochIdStr + "') AND (";
            for (int j = 0; j < locArray.length; j ++){
                String loc = locArray[j];
                for (int k = 1 ; k <= maxLocCounter; k++){
                    String encL = AES.concatAndEncrypt(loc, String.valueOf(k));
                    conditionsStr = conditionsStr + "encl='" + encL + "'";
                    if (k == maxLocCounter && j == locArray.length - 1){
                        conditionsStr = conditionsStr + "))";
                    }
                    else{
                        conditionsStr = conditionsStr + " OR ";
                    }
                }
            }

            if(i == coveredEpochIds.size() - 1){
                conditionsStr = conditionsStr + ")";
            }
            else{
                conditionsStr = conditionsStr + " OR ";
            }
        }

        String userTraceQuery = String.format(userTraceQueryTemplate, encTableName, conditionsStr);

        return userTraceQuery;
    }

    public String generateSocialDistEncQuery(Timestamp queryStartTimeTs, Timestamp queryEndTimeTs){
        return "";
    }

    public String generateCrowdFlowEncQuery(Timestamp queryStartTimeTs, Timestamp queryEndTimeTs){
        return "";
    }

    public static void main(String[] args) throws IOException{
        Epoch epoch = new Epoch(Integer.valueOf(15));

        DbQuerier dbQuerier = new DbQuerier(10009,
                                            "quest_15_1000000");

        EncDataAnalyzer encDataAnalyzer = new EncDataAnalyzer("tippersquest",
                                                              "questsecret");

        QueryGenerator queryGenerator = new QueryGenerator(epoch, 
                                                           "tippersquest",
                                                           "questsecret",
                                                           "quest_15_1000000",
                                                           dbQuerier,
                                                           encDataAnalyzer);

        String encSqlStr = queryGenerator.getEncQuery(1,
                                                    "2018-10-04 05:00:00.000000", 
                                                    "2018-10-04 05:16:22.000000", 
                                                    "123123jh1i23s89df98ssdf");

        System.out.println("Encrypted SQL Query: " + encSqlStr);
    }
}
