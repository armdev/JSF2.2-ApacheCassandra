package com.project.web.beans;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.set;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

@ManagedBean(name = "databaseBean")
@ApplicationScoped
public class DatabaseBean implements Serializable {

    private static final long serialVersionUID = 1L;

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
        metadata.getAllHosts().forEach((host) -> {
            System.out.printf("Datacenter: %s; Host: %s; Rack: %s\n",
                    host.getDatacenter(), host.getAddress(), host.getRack());
        });
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

    public void deleteUser(String id) {
        if (id != null) {
            Statement statement = QueryBuilder.delete()
                    .from("socnet", "user")
                    .where(eq("id", UUID.fromString(id)));
            session.execute(statement);
        }
    }

    public void updateUser(User user) {
        if (user.getId() != null) {
            Statement statement = QueryBuilder.update("socnet", "user").with(set("firstname", user.getFirstname())).and(set("lastname", user.getLastname()))
                    .and(set("email", user.getEmail()))
                    .and(set("age", user.getAge()))
                    .where(eq("id", UUID.fromString(user.getId().toString())));
            session.execute(statement);
        }
    }

    public List<User> getUserList() {
        List<User> userList = new ArrayList<>();
        ResultSet results = session.execute(selectAllUsers.bind());
        for (Row row : results) {
            userList.add(new User(row.getInt("status"), (row.getUUID("id")), row.getString("firstname"), row.getString("lastname"), row.getString("email"), row.getInt("age")));
        }
        return userList;
    }

    public User getUserById(String id) {
        Statement statement = QueryBuilder.select().from("socnet", "user").where(eq("id", UUID.fromString(id)));
        User user = new User();
        Row row = session.execute(statement).one();
        if (row != null) {
            user.setAge(row.getInt("age"));
            user.setStatus(row.getInt("status"));
            user.setFirstname(row.getString("firstname"));
            user.setLastname(row.getString("lastname"));
            user.setEmail(row.getString("email"));
            user.setId(row.getUUID("id"));
        }

        return user;
    }

    public List<User> getUserListSorted() {
        Statement statement = QueryBuilder.select().from("socnet", "user").where(eq("status", 1)).orderBy(QueryBuilder.desc("age")).limit(1000000).allowFiltering();
        List<User> userList = new ArrayList<>();
        ResultSet results = session.execute(statement);
        for (Row row : results) {
            userList.add(new User(row.getInt("status"), (row.getUUID("id")), row.getString("firstname"), row.getString("lastname"), row.getString("email"), row.getInt("age")));
        }
        return userList;
    }

    @PreDestroy
    private void stopDB() {
        cluster.close();
    }
}
