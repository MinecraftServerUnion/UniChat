package dev.onelili.unichat.velocity.handler;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.scheduler.ScheduledTask;
import dev.onelili.unichat.velocity.UniChat;
import dev.onelili.unichat.velocity.channel.Channel;
import dev.onelili.unichat.velocity.channel.type.RedisChannelHandler;
import dev.onelili.unichat.velocity.message.Message;
import dev.onelili.unichat.velocity.util.Config;
import dev.onelili.unichat.velocity.util.Logger;
import dev.onelili.unichat.velocity.util.MapTree;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.JedisPubSub;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Getter
public class RedisRemoteManager {
    @Getter @Setter
    private static RedisRemoteManager instance=null;

    private final String address;
    private final int port;
    private final String password, username;
    private final JedisPooled jedis;
    private final Thread thread;
    private final ScheduledTask updateTask;

    private final Map<String, List<String>> onlinePlayerCache = new ConcurrentHashMap<>();

    public RedisRemoteManager(){
        if(Config.getString("server-name").equals("Velocity"))
            throw new IllegalArgumentException("Please change 'server-name' to something else unique!");

        this.address = Config.getString("redis.address");
        this.username = Config.getConfigTree().getString("redis.user", null);
        this.password = Config.getConfigTree().getString("redis.password", null);
        this.port = Config.getConfigTree().getInt("redis.port", 6379);

        try {
            if (username != null)
                jedis = new JedisPooled(address, port, username, password);
            else
                jedis = new JedisPooled(address, port);

            if(jedis.hexists("servers", Config.getString("server-name"))){
                try{
                    long time = Long.parseLong(jedis.hget("servers", Config.getString("server-name")));
                    if(time + 5000L > System.currentTimeMillis()) {
                        throw new IllegalArgumentException("Existing server with the same server-name already exists in the network! If you believed that this is an error, please close the server and try restarting it again after 10 seconds.");
                    }
                }catch (NumberFormatException ignored){}
            }

            thread = new Thread(() -> jedis.subscribe(new JedisPubSub() {
                @Override
                public void onMessage(@Nonnull String ch, @Nonnull String messageTree) {
                    MapTree cont = MapTree.fromJson(messageTree);
                    String packetType = cont.getString("type");
                    Component message = MiniMessage.miniMessage().deserialize(cont.getString("msg"));
                    String sender = cont.getString("sender");
                    String server = cont.getString("server");

                    if(packetType.equals("channel")) {
                        String channel = cont.getString("channel");

                        if(RedisChannelHandler.getRedisChannels().containsKey(channel)) {
                            RedisChannelHandler.getRedisChannels().get(channel).receive(sender, server, message);
                        }
                    }else if(packetType.equals("msg")) {
                        String target = cont.getString("target");
                        if(UniChat.getProxy().getPlayer(target).isPresent()) {
                            Component inbound = new Message(Config.getString("message.format-inbound")).add("name", sender+"@"+server).toComponent()
                                        .append(message),
                                    thirdparty = new Message(Config.getString("message.format-third-party")).add("sender", sender+"@"+server).add("target", target).toComponent()
                                        .append(message);
                            UniChat.getProxy().getPlayer(target).get().sendMessage(inbound);
                            UniChat.getProxy().getConsoleCommandSource().sendMessage(thirdparty);
                        }
                    }
                }
            }, "unichat-channel"));
            thread.start();

            updateTask = UniChat.getProxy().getScheduler().buildTask(UniChat.getInstance(), ()->{
                jedis.hset("servers", Config.getString("server-name"), String.valueOf(System.currentTimeMillis()));
                jedis.set("onlines."+Config.getString("server-name"), String.join(",", UniChat.getProxy().getAllPlayers().stream().map(Player::getUsername).toList()));
                updateOnlinePlayers();
            }).repeat(3000, TimeUnit.MILLISECONDS).schedule();

        }catch (Exception e){
            Logger.error("Failed to connect to Redis server: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<String> fetchOnlineServers() {
        List<String> servers = new ArrayList<>();
        Map<String, String> ret = jedis.hgetAll("servers");
        for(String i: ret.keySet()) {
            try{
                long time = Long.parseLong(ret.get(i));
                if(time + 5000L > System.currentTimeMillis()) {
                    servers.add(i);
                }
            }catch (NumberFormatException ignored){}
        }
        return servers;
    }

    public List<String> fetchPlayers(String server) {
        if(jedis.exists("onlines."+server)) {
            String ret = jedis.get("onlines."+server);
            if(ret.isEmpty()) return new ArrayList<>();
            return List.of(ret.split(","));
        }
        return new ArrayList<>();
    }

    public void updateOnlinePlayers() {
        Map<String, List<String>> newOnlinePlayers = new HashMap<>();
        for(String i: fetchOnlineServers()) {
            newOnlinePlayers.put(i, fetchPlayers(i));
        }
        onlinePlayerCache.clear();
        onlinePlayerCache.putAll(newOnlinePlayers);
    }

    public void shutdown(){
        jedis.hdel("servers", Config.getString("server-name"));
        thread.interrupt();
        updateTask.cancel();
        jedis.close();
    }
}
