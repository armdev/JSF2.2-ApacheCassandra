package com.project.web.beans;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

@ManagedBean(name = "databaseBean")
@ApplicationScoped
public class DatabaseBean implements Serializable {

    private Cluster cluster;
    private Session session;

    private PreparedStatement selectAllUsers;
    private PreparedStatement insertUser;
  

    @PostConstruct
    private void initDB() {
        System.out.println("Init database Bean!!!!!!!");
       
        cluster = Cluster.builder()
                .addContactPoint("localhost")
                // .withSSL() // Uncomment if using client to node encryption
                .build();
        Metadata metadata = cluster.getMetadata();
        System.out.printf("Connected to cluster: %s\n", metadata.getClusterName());
        for (Host host : metadata.getAllHosts()) {
            System.out.printf("Datacenter: %s; Host: %s; Rack: %s\n",
                    host.getDatacenter(), host.getAddress(), host.getRack());
        }
        session = cluster.connect();
        session.execute("CREATE KEYSPACE IF NOT EXISTS socnet WITH replication "
                + "= {'class':'SimpleStrategy', 'replication_factor':1};");

        session.execute(
                "CREATE TABLE IF NOT EXISTS socnet.user ("
                + "status int ,"
                + "age int, "
                + "id uuid ,"
                + "firstname varchar, "
                + "lastname varchar,"
                + "email varchar,"               
                + "PRIMARY KEY (status, age, id, firstname, lastname, email)"
                + ");");

        selectAllUsers = session.prepare("SELECT status, id, firstname, lastname, email, age FROM socnet.user");
        insertUser = session.prepare(
                "INSERT INTO socnet.user (status, age, id, firstname, lastname, email) VALUES (?,?, now(), ?, ?, ?);"
        );
    }

    
    public void createPerson(User user) {
        session.execute(insertUser.bind(1, user.getAge(), user.getFirstname(), user.getLastname(), user.getEmail()));
    }

    public List<User> getUserList() {
        List<User> userList = new ArrayList<User>();
        ResultSet results = session.execute(selectAllUsers.bind());
        for (Row row : results) {
            userList.add(new User(row.getInt("status"),(row.getUUID("id")), row.getString("firstname"), row.getString("lastname"), row.getString("email"), row.getInt("age")));
        }
        return userList;
    }
    
    
     public List<User> getUserListSorted() {
        Statement statement = QueryBuilder.select().from("socnet", "user").where(eq("status", 1)).orderBy(QueryBuilder.desc("age")).limit(1000000).allowFiltering();
        List<User> userList = new ArrayList<User>();
        ResultSet results = session.execute(statement);        
        for (Row row : results) {
            userList.add(new User(row.getInt("status"),(row.getUUID("id")), row.getString("firstname"), row.getString("lastname"), row.getString("email"), row.getInt("age")));
        }
        return userList;
    }

   
    @PreDestroy
    private void stopDB() {
        cluster.close();
    }
}
