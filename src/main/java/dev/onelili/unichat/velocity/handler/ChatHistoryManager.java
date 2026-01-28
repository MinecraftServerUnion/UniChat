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

    @SneakyThrows
    public static void recordMessage(String sender, String channel, String server, String message) {
        try (Connection connection = DatabaseHandler.getInstance().getConnection()) {
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
    public static List<ChatMessage> searchHistory(String query, @Nullable String channel, int limit, int offset){
        try (Connection connection = DatabaseHandler.getInstance().getConnection()) {
            String tmp="SELECT * FROM chathistory WHERE message LIKE '%' || ? || '%'"+(channel!= null ? " AND channel = '"+channel+"'" : "")+" ORDER BY time DESC LIMIT ? OFFSET ?";
            System.out.println(tmp.replaceFirst("\\?", "'"+query+"'").replaceFirst("\\?", String.valueOf(limit)).replaceFirst("\\?", String.valueOf(offset)));
            PreparedStatement st = connection.prepareStatement("SELECT * FROM chathistory WHERE message LIKE '%' || ? || '%'"+(channel!= null ? " AND channel = '"+channel+"'" : "")+" ORDER BY time DESC LIMIT ? OFFSET ?");
            st.setString(1, query);
            st.setInt(2, limit);
            st.setInt(3, offset);
            ResultSet rs = st.executeQuery();
            List<ChatMessage> result = new ArrayList<>();
            while (rs.next()) {
                result.add(new ChatMessage(rs.getString("sender"), rs.getString("channel"), rs.getString("server"), rs.getString("message"), rs.getLong("time")));
            }
            return result;
        }
    }
    @SneakyThrows
    public static List<ChatMessage> listHistoryBefore(long startTime, int limit, @Nullable String channel){
        try (Connection connection = DatabaseHandler.getInstance().getConnection()) {
            PreparedStatement st = connection.prepareStatement("SELECT * FROM chathistory WHERE time <= ?"+(channel!= null ? " AND channel = '"+channel+"'" : "")+" ORDER BY time DESC LIMIT ?");
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
    @SneakyThrows
    public static List<ChatMessage> listHistoryAfter(long startTime, int limit, @Nullable String channel){
        try (Connection connection = DatabaseHandler.getInstance().getConnection()) {
            PreparedStatement st = connection.prepareStatement("SELECT * FROM chathistory WHERE time >= ?"+(channel!= null ? " AND channel = '"+channel+"'" : "")+" ORDER BY time LIMIT ?");
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
