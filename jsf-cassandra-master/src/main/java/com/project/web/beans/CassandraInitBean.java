package com.project.web.beans;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import java.io.Serializable;
import javax.annotation.PreDestroy;


//@ManagedBean
//@ApplicationScoped//test
public class CassandraInitBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Cluster cluster;
    private Session session;

    // private PreparedStatement selectAll;
    private PreparedStatement insertUser;
    
    private PreparedStatement insertData;
    private PreparedStatement insertData2;
    private PreparedStatement insertData3;

   // @PostConstruct
    private void initDB() {
      

        cluster = Cluster.builder()
                .addContactPoint("localhost")
                // .withSSL() // Uncomment if using client to node encryption
                .build();
        Metadata metadata = cluster.getMetadata();
        System.out.printf("Connected to cluster: %s\n", metadata.getClusterName());
        metadata.getAllHosts().forEach((host) -> {
            System.out.printf("Datacenter: %s; Host: %s; Rack: %s\n",
                    host.getDatacenter(), host.getAddress(), host.getRack());
        });
        session = cluster.connect();

        session.execute("CREATE KEYSPACE IF NOT EXISTS eod WITH replication "
                + "= {'class':'SimpleStrategy', 'replication_factor':1};");

        session.execute(
                "CREATE TABLE IF NOT EXISTS eod.eoddata ("
                + "id uuid ,"
                + "symbol varchar, "
                + "date varchar, "
                + "reportDate timestamp,"
                + "open double,"
                + "high double, "
                + "low double, "
                + "close double, "
                + "volume double, "
                + "ex_Dividend double, "
                + "splitRatio double, "
                + "adjOpen double, "
                + "adjHigh double, "
                + "adjLow double, "
                + "adjClose double, "
                + "adjVolume double, "
                + "PRIMARY KEY (id,symbol,reportDate, open, close,high,low,volume,date,adjOpen,adjHigh,adjLow,adjClose,adjVolume)"
                + ");");

        insertData = session.prepare(
                "INSERT INTO eod.eoddata (id, symbol, date, reportDate, open, high,low,close,volume,ex_Dividend,splitRatio,adjOpen,adjHigh,adjLow,adjClose,adjVolume)"
                + " VALUES (now(),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);"
        );
        insertData2 = session.prepare(
                "INSERT INTO eod.eoddata (id, symbol, date, reportDate, open, high,low,close,volume,ex_Dividend,splitRatio,adjOpen,adjHigh,adjLow,adjClose)"
                + " VALUES (now(),?,?,?,?,?,?,?,?,?,?,?,?,?,?);"
        );
        insertData3 = session.prepare(
                "INSERT INTO eod.eoddata (id, symbol, date, reportDate, open, high,low,close,volume,ex_Dividend,splitRatio,adjOpen,adjHigh,adjLow)"
                + " VALUES (now(),?,?,?,?,?,?,?,?,?,?,?,?,?);"
        );

        //selectAll = session.prepare("SELECT * FROM eod.eoddata where symbol = ? and reportDate > ?");
    }//https://gist.github.com/yangzhe1991/10349122

//    public List<QuandlEntity> getSymbolList(String symbol, Date reportDate, Date endDate) {
//         System.out.println("reportDate " + reportDate);
//        //Statement statement = QueryBuilder.select().from("eod", "eoddata").where(eq("symbol", symbol))
//          //      .and(gt("reportDate", reportDate)).and(lt("reportDate",endDate)).orderBy(QueryBuilder.desc("reportDate")).limit(1000000).allowFiltering();
//        
//         Statement statement = QueryBuilder.select().from("eod", "eoddata").where(eq("symbol", symbol))
//                .and(gt("reportDate", reportDate)).and(lt("reportDate",endDate)).limit(1000000).allowFiltering();

//        System.out.println("Statement " +statement);
//        List<QuandlEntity> list = new ArrayList<QuandlEntity>();
//        ResultSet results = session.execute(statement);
//        for (Row row : results) {
//            list.add(
//                    new QuandlEntity(
//                            row.getUUID("id"),
//                            row.getDate("reportDate"),
//                            row.getString("date"),
//                            row.getDouble("open"),
//                            row.getDouble("high"),
//                            row.getDouble("low"),
//                            row.getDouble("close"),
//                            row.getDouble("volume"),
//                            row.getDouble("ex_Dividend"),
//                            row.getDouble("splitRatio"),
//                            row.getDouble("adjOpen"),
//                            row.getDouble("adjHigh"),
//                            row.getDouble("adjLow"),
//                            row.getDouble("adjClose"),
//                            row.getDouble("adjVolume"),
//                            row.getString("symbol")));
//        }
      //  return list;
    //}

    @PreDestroy
    private void stopDB() {
        cluster.close();
    }
}
