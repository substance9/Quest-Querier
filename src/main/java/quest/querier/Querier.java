package quest.querier;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Properties;

import quest.model.EncWifiData;
import quest.util.*;

public class Querier {
    public static void main( String[] args ){

        System.out.println( "TIPPERS Quest Program (Querier Module) Initializing:");

        // Initializing....

        Properties prop = readConfig(args);

        Epoch epoch = new Epoch(Integer.parseInt(prop.getProperty("epoch")));

        DbQuerier dbQuerier = new DbQuerier(Integer.valueOf(prop.getProperty("db_port")),
                                                            prop.getProperty("result.enc_table_name"));

        EncDataAnalyzer encDataAnalyzer = new EncDataAnalyzer(prop.getProperty("enc_key"),
                                                                prop.getProperty("secret"));

        QueryGenerator queryGenerator = new QueryGenerator(epoch, 
                                                            prop.getProperty("enc_key"),
                                                            prop.getProperty("secret"),
                                                            prop.getProperty("result.enc_table_name"),
                                                            dbQuerier,
                                                            encDataAnalyzer);

        // Start executing query......
        Instant start = Instant.now();

        String encSqlStr = queryGenerator.getEncQuery(Integer.valueOf(prop.getProperty("query_type")),
                                                        prop.getProperty("query_start_time"), 
                                                        prop.getProperty("query_end_time"), 
                                                        prop.getProperty("query_device"));
     
        Instant queryFormingFinish = Instant.now();

        if (encSqlStr == null){
            System.out.println("Empty SQL Generated. Exit (If you are executing Query 2, this is caused by the reason that user did not present in the selected epochs.)");
            return;
        }

        ArrayList<EncWifiData> encResults = dbQuerier.execQuery(Integer.valueOf(prop.getProperty("query_type")),
                                                                encSqlStr);

        Instant queryExecFinish = Instant.now();

        encDataAnalyzer.processResults(Integer.valueOf(prop.getProperty("query_type")),
                                        encResults);

        Instant queryAnalysisFinish = Instant.now();

        System.out.println("Quest Query Execution Finished");
        long queryFormingTime = Duration.between(start, queryFormingFinish).toMillis();
        System.out.println("Encrypted SQL Query Forming Time: " + String.valueOf(queryFormingTime) + " ms");

        long queryExecTime = Duration.between(queryFormingFinish, queryExecFinish).toMillis();
        System.out.println("Query Execution (at DB) Time: " + String.valueOf(queryExecTime) + " ms");
        
        long queryAnalysisTime = Duration.between(queryExecFinish, queryAnalysisFinish).toMillis();
        System.out.println("Result Analysis Time: " + String.valueOf(queryAnalysisTime) + " ms");

        long totalTime = Duration.between(start, queryAnalysisFinish).toMillis();
        System.out.println("Total Time: " + String.valueOf(totalTime) + " ms");
    }

    private static Properties readConfig( String[] args ){
        Properties prop = new Properties();

        ArgumentParser parser = ArgumentParsers.newFor("Querier").build().defaultHelp(true)
                .description("TIPPERS QUEST Project - Querier Module");
        parser.addArgument("-d", "--epoch").required(true).help("Epoch of the batch processing (in minutes)");
        parser.addArgument("-x", "--id").required(true).help("Experiment ID");
        parser.addArgument("-k", "--enc_key").required(true).help("Encryption key");
        parser.addArgument("-s", "--secret").required(true).help("Secret");
        parser.addArgument("-p", "--db_port").required(true).help("Database port");
        parser.addArgument("-n", "--enc_table_name").required(true).help("Table name for encrypted data in the database");
        parser.addArgument("-o", "--output_path").required(true).help("Result(log) output path");
        parser.addArgument("-q", "--query_type").required(true).help("Query type");
        parser.addArgument("-b", "--query_start_time").required(true).help("Query start time");
        parser.addArgument("-e", "--query_end_time").required(true).help("Query end time");
        parser.addArgument("-v", "--query_device").required(true).help("Query device");
		Namespace ns = null;

		try {
			ns = parser.parseArgs(args);
		} catch (ArgumentParserException e) {
			parser.handleError(e);
			System.exit(1);
        }
        
        if (args.length >= 2){
            //read config from command line args
            prop.setProperty("epoch", ns.get("epoch"));
            prop.setProperty("experiment_id", ns.get("id"));
            prop.setProperty("enc_key", ns.get("enc_key"));
            prop.setProperty("secret", ns.get("secret"));
            prop.setProperty("db_port", ns.get("db_port"));
            prop.setProperty("result.enc_table_name", ns.get("enc_table_name"));
            prop.setProperty("result.output_path", ns.get("output_path"));
            prop.setProperty("query_type", ns.get("query_type"));
            prop.setProperty("query_start_time", ns.get("query_start_time"));
            prop.setProperty("query_end_time", ns.get("query_end_time"));
            prop.setProperty("query_device", ns.get("query_device"));
        }
            
        String resultDir = prop.getProperty("result.output_path")
                                                                +"dur_"+prop.getProperty("epoch")
                                                                +"|expID_"+prop.getProperty("experiment_id");
        prop.setProperty("result.output_dir", resultDir);
        System.out.println("--result.output_dir:\t"+resultDir);
        System.out.println("--result.enc_table_name:\t"+prop.getProperty("result.enc_table_name"));

        System.out.println( "Experiment Parameters:" );
            // get the property value and print it out
            System.out.println("--epoch:\t\t\t"+prop.getProperty("epoch"));
            System.out.println("--experiment_id:\t"+prop.getProperty("experiment_id"));
            System.out.println("--enc_key:\t\t\t"+prop.getProperty("enc_key"));
            System.out.println("--secret:\t"+prop.getProperty("secret"));
            System.out.println("--db_port:\t\t\t"+prop.getProperty("db_port"));
            System.out.println("--query_type:\t\t"+prop.getProperty("query_type"));
            System.out.println("--query_start_time:\t"+prop.getProperty("query_start_time"));
            System.out.println("--query_end_time:\t"+prop.getProperty("query_end_time"));
            System.out.println("--query_device:\t"+prop.getProperty("query_device"));

        return prop;
    }
 }