package quest.querier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import quest.model.EncWifiData;
import quest.util.AES;

public class EncDataAnalyzer {
    private String secretZStr;
    private String keyStr;


    public EncDataAnalyzer(String keyStr, String secretZStr) {
        this.keyStr = keyStr;
        this.secretZStr = secretZStr;
    }

    public void processResults(int queryType, ArrayList<EncWifiData> encResults) {
        if (queryType == 1){
            processLocTraceResults(encResults);
            return;
        }
        else if (queryType == 2){
            processUserTraceResults(encResults);
            return;
        }
        else if (queryType == 3){
            processSocialDistResults(encResults);
            return;
        }
        else if (queryType == 4){
            
        }
    }

    public void processLocTraceResults(ArrayList<EncWifiData> encResults){
        String[] locArray = getLocTraces(encResults);

        System.out.println("Location Trace Query Finished");
        System.out.println("Found " + String.valueOf(locArray.length) + " visited location for the device. The list of the locations: ");
        for (String loc : locArray){
            System.out.println(loc);
        }
    }

    public String[] getLocTraces(ArrayList<EncWifiData> encResults){
        HashSet<String> visitedLocs = new HashSet<String>();
        //debug info
        System.out.println("EncResults size: " + String.valueOf(encResults.size()));
        for (EncWifiData encData:encResults){
            String concatCL = AES.decrypt(encData.encCL);
            String[] clSecs = concatCL.split("\\|\\|",0);
            String[] visitedLocsInEpoch = clSecs[1].split(",", 0);

            for (String loc : visitedLocsInEpoch){
                visitedLocs.add(loc);
            }
        }

        String[] locArray = new String[visitedLocs.size()];
        visitedLocs.toArray(locArray);
        return locArray;
    }

    public void processUserTraceResults(ArrayList<EncWifiData> encResults){
        HashSet<String> affectedUsers = new HashSet<String>();
        //debug info
        System.out.println("EncResults size: " + String.valueOf(encResults.size()));
        for (EncWifiData encData:encResults){
            String concatId = AES.decrypt(encData.encId);
            String[] IdSecs = concatId.split("\\|\\|",0);
            String userInData = IdSecs[0];

            affectedUsers.add(userInData);
        }

        System.out.println("User Trace Query Finished");
        System.out.println("Found " + String.valueOf(affectedUsers.size()) + " Users visited potential affected locations. The list of the Users: ");
        for (String user : affectedUsers){
            System.out.println(user);
        }
    }

    public void processSocialDistResults(ArrayList<EncWifiData> encResults){
        HashMap<String,Integer> locCounterMap = new HashMap<String,Integer>();
        //debug info
        System.out.println("EncResults size: " + String.valueOf(encResults.size()));
        for (EncWifiData encData:encResults){
            String concatL = AES.decrypt(encData.encL);
            String[] LSecs = concatL.split("\\|\\|",0);
            String loc = LSecs[0];

            if (!locCounterMap.containsKey(loc)){
                locCounterMap.put(loc, 1);
            }
            else{
                Integer locCounter = locCounterMap.get(loc);
                locCounter = locCounter + 1;
                locCounterMap.put(loc, locCounter);
            }
        }

        System.out.println("Social Distance Query Finished");
        System.out.println("List of location occupancy: ");
        for (Map.Entry mapElement : locCounterMap.entrySet()) { 
            String loc = (String)mapElement.getKey(); 
            int counter = ((int)mapElement.getValue()); 
  
            System.out.println(loc + " : " + counter); 
        }

    }

    
}
