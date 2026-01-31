package cn.jason31416.chatx.handler;

import cn.jason31416.chatx.util.SimplePlayer;
import lombok.SneakyThrows;

import java.sql.ResultSet;

public class PunishmentHandler {
    /**
    Returns -1 if not muted
     */
    @SneakyThrows
    public static long fetchMuted(SimplePlayer player){
        try(var conn=DatabaseHandler.getInstance().getConnection()){
            var stmt = conn.prepareStatement("SELECT until FROM chatx_mutes WHERE player =? AND until >? AND cancelled = FALSE");
            stmt.setString(1, player.getName());
            stmt.setLong(2, System.currentTimeMillis());
            ResultSet rs = stmt.executeQuery();
            long ret = -1;
            while(rs.next()){
                ret = Math.max(ret, rs.getLong("until"));
            }
            return ret;
        }
    }
    @SneakyThrows
    public static void mutePlayer(SimplePlayer player, String executor, String reason, long time){
        try(var conn=DatabaseHandler.getInstance().getConnection()){
            var stmt = conn.prepareStatement("INSERT INTO chatx_mutes (player, reason, punisher, begin, until) VALUES (?,?,?,?,?)");
            stmt.setString(1, player.getName());
            stmt.setString(2, reason);
            stmt.setString(3, executor);
            stmt.setLong(4, System.currentTimeMillis());
            stmt.setLong(5, System.currentTimeMillis()+time);
            stmt.executeUpdate();
        }
    }
    @SneakyThrows
    public static void unmutePlayer(SimplePlayer player){
        try(var conn=DatabaseHandler.getInstance().getConnection()){
            var stmt = conn.prepareStatement("UPDATE chatx_mutes SET cancelled = TRUE WHERE player = ? AND until > ?");
            stmt.setString(1, player.getName());
            stmt.setLong(2, System.currentTimeMillis());
            stmt.executeUpdate();
        }
    }
}
