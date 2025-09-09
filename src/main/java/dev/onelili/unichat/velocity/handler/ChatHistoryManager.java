package dev.onelili.unichat.velocity.handler;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.onelili.unichat.velocity.UniChat;
import lombok.Getter;
import lombok.SneakyThrows;

import javax.annotation.Nullable;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Getter
public class ChatHistoryManager {
    public static record ChatMessage(String sender, String channel, String server, String message, long time){}
    public static HikariDataSource dataSource;

    @SneakyThrows
    public static void init() {
        HikariConfig otherConfig = new HikariConfig();
        otherConfig.setDriverClassName("org.sqlite.JDBC");
        otherConfig.setJdbcUrl("jdbc:sqlite:" + new File(UniChat.getDataDirectory(), "chathistory.db").getAbsolutePath());
        dataSource = new HikariDataSource(otherConfig);

        try (Connection connection = getConnection()) {
            connection.prepareStatement("CREATE TABLE IF NOT EXISTS chathistory (id INTEGER PRIMARY KEY AUTOINCREMENT, channel VARCHAR(255), sender VARCHAR(255), server VARCHAR(255), message TEXT, time BIGINT)").execute();
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    @SneakyThrows
    public static void recordMessage(String sender, String channel, String server, String message) {
        try (Connection connection = getConnection()) {
            PreparedStatement st = connection.prepareStatement("INSERT INTO chathistory (sender, channel, server, message, time) VALUES (?,?,?,?,?)");
            st.setString(1, sender);
            st.setString(2, channel);
            st.setString(3, server);
            st.setString(4, message);
            st.setLong(5, System.currentTimeMillis());
            st.execute();
        }
    }
    @SneakyThrows
    public static List<ChatMessage> searchHistory(String query, @Nullable String channel, int limit){
        try (Connection connection = getConnection()) {
            PreparedStatement st = connection.prepareStatement("SELECT * FROM chathistory WHERE message LIKE '%' || ? || '%'"+(channel!= null ? " AND channel = "+channel : "")+" ORDER BY time DESC LIMIT ?");
            st.setString(1, query);
            st.setInt(2, limit);
            ResultSet rs = st.executeQuery();
            List<ChatMessage> result = new ArrayList<>();
            while (rs.next()) {
                result.add(new ChatMessage(rs.getString("sender"), rs.getString("channel"), rs.getString("server"), rs.getString("message"), rs.getLong("time")));
            }
            return result;
        }
    }
    @SneakyThrows
    public static List<ChatMessage> listHistory(long startTime, int limit){
        try (Connection connection = getConnection()) {
            PreparedStatement st = connection.prepareStatement("SELECT * FROM chathistory WHERE time <= ? ORDER BY time DESC DESC LIMIT ?");
            st.setLong(1, startTime);
            st.setInt(2, limit);
            ResultSet rs = st.executeQuery();
            List<ChatMessage> result = new ArrayList<>();
            while (rs.next()) {
                result.add(new ChatMessage(rs.getString("sender"), rs.getString("channel"), rs.getString("server"), rs.getString("message"), rs.getLong("time")));
            }
            return result;
        }
    }
}
