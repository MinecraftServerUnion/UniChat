package dev.onelili.unichat.velocity.handler;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.onelili.unichat.velocity.UniChat;
import dev.onelili.unichat.velocity.util.Config;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseHandler {
    @Getter
    private static DatabaseHandler instance;

    @Getter
    private HikariDataSource datasource;

    @SneakyThrows
    public Connection getConnection() {
        return datasource.getConnection();
    }

    @SneakyThrows
    public static void init() {
        instance = new DatabaseHandler();
        HikariConfig otherConfig = new HikariConfig();
        String database = Config.getString("database.type").toLowerCase();
        if(database.equals("sqlite")) {
            otherConfig.setDriverClassName("org.sqlite.JDBC");
            otherConfig.setJdbcUrl("jdbc:sqlite:" + new File(UniChat.getDataDirectory(), "chathistory.db").getAbsolutePath());
        }else if(database.equals("mysql")) {
            otherConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
            otherConfig.setJdbcUrl("jdbc:mysql://"+Config.getInt("database.host")+":"+Config.getInt("database.port")+"/"+Config.getString("database.database"));
            otherConfig.setUsername(Config.getString("database.username"));
            otherConfig.setPassword(Config.getString("database.password"));
            otherConfig.addDataSourceProperty("cachePrepStmts", "true");
            otherConfig.addDataSourceProperty("prepStmtCacheSize", "250");
            otherConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        }
        instance.datasource = new HikariDataSource(otherConfig);

        try (Connection connection = instance.getConnection()) {
            connection.prepareStatement("CREATE TABLE IF NOT EXISTS chathistory (id INTEGER PRIMARY KEY AUTOINCREMENT, channel VARCHAR(255), sender VARCHAR(255), server VARCHAR(255), message TEXT, time BIGINT)").execute();
//            connection.prepareStatement("CREATE TABLE IF NOT EXISTS chatfriend (username VARCHAR(255) PRIMARY KEY, )").execute();
        }
    }
}
