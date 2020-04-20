package quest.querier;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import quest.model.EncWifiData;

public class DbQuerier {
    public HikariDataSource dataDBConnectionPool;
    private Connection dataDbCon = null;

    public DbQuerier(int dbPort, String encTableName) {
        Properties prop = getHikariDbProperties();

        String jdbcUrlBase = prop.getProperty("jdbcUrl");
        String dataJdbcUrl = jdbcUrlBase + ":" + String.valueOf(dbPort) + "/tippers_quest";

        System.out.println("Preparing to connect to DB for data storage: " + dataJdbcUrl);

        // Data DB COnnection
        HikariConfig dataDbCfg = new HikariConfig(prop);
        dataDbCfg.setJdbcUrl(dataJdbcUrl);
        dataDbCfg.setMaximumPoolSize(2); // no need for MPL
        dataDbCfg.setAutoCommit(false);
        dataDBConnectionPool = new HikariDataSource(dataDbCfg);

    }

    public Properties getHikariDbProperties() {
        Properties prop = null;
        try (InputStream input = DbQuerier.class.getClassLoader().getResourceAsStream("postgres.properties")) {
            prop = new Properties();
            prop.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return prop;
    }

    public ArrayList<EncWifiData> execQuery(int queryType,  String encQueryStr){
        PreparedStatement pst = null;
        ResultSet rs = null;
        ArrayList<EncWifiData> resultEncWifiDataSet = new ArrayList<EncWifiData>();
        try{
            dataDbCon = dataDBConnectionPool.getConnection();
            dataDbCon.setAutoCommit(true);

            pst = dataDbCon.prepareStatement(encQueryStr);
            rs = pst.executeQuery();

            if (queryType == 1){
                //Loc Trace query only returns enccl
                while (rs.next()) {
                    EncWifiData retEncData = new EncWifiData();
                    retEncData.encCL = rs.getString(1);
                    resultEncWifiDataSet.add(retEncData);
                }
            }
            else if (queryType == 2){

            }
            else if (queryType == 3){
                
            }
            else if (queryType == 4){
                
            }
            
            
        } catch (SQLException ex){
            ex.printStackTrace();
        }finally {
            try {
                if (dataDbCon != null) {
                    dataDbCon.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        return resultEncWifiDataSet;
    }
}
