package dev.onelili.unichat.velocity.channel.type;

import com.velocitypowered.api.proxy.Player;
import dev.onelili.unichat.velocity.UniChat;
import dev.onelili.unichat.velocity.channel.Channel;
import dev.onelili.unichat.velocity.channel.ChannelHandler;
import dev.onelili.unichat.velocity.message.Message;
import dev.onelili.unichat.velocity.module.PatternModule;
import dev.onelili.unichat.velocity.util.Logger;
import dev.onelili.unichat.velocity.util.MapTree;
import dev.onelili.unichat.velocity.util.SimplePlayer;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import redis.clients.jedis.*;

import javax.annotation.Nonnull;

public class RedisChannelHandler implements ChannelHandler {
    private final String address;
    private final int port;
    private final String password, username;
    private final JedisPooled jedis;
    private final Channel channel;
    private final Thread thread;

    @SneakyThrows
    public RedisChannelHandler(@Nonnull Channel channel) {
        this.address = channel.getChannelConfig().getString("redis-address");
        this.username = channel.getChannelConfig().getString("redis-user", null);
        this.password = channel.getChannelConfig().getString("redis-password", null);
        this.port = channel.getChannelConfig().getInt("redis-port", 6379);

        this.channel = channel;
        try {
            if (username != null)
                jedis = new JedisPooled(address, port, username, password);
            else
                jedis = new JedisPooled(address, port);

            thread = new Thread(()-> {
                jedis.subscribe(new JedisPubSub() {
                    @Override
                    public void onMessage(@Nonnull String ch, @Nonnull String messageTree) {
                        MapTree cont = MapTree.fromJson(messageTree);
                        Component message = MiniMessage.miniMessage().deserialize(cont.getString("msg"));
                        String sender = cont.getString("sender");
                        Message msg = new Message(channel.getChannelConfig().getString("format"));
                        msg.add("player", sender);
                        msg.add("channel", channel.getDisplayName());
                        Component component = msg.toComponent().append(message);
                        for (Player receiver : UniChat.getProxy().getAllPlayers()) {
                            receiver.sendMessage(component);
                        }
                        if (channel.isLogToConsole())
                            UniChat.getProxy().getConsoleCommandSource().sendMessage(component);
                    }
                }, "unichat-channel-" + channel.getId());
            });
            thread.start();
        }catch (Exception e){
            Logger.error("Failed to connect to Redis server: " + e.getMessage());
            Logger.error("Disabling redis channel: "+channel.getId());
            throw e;
        }
    }

    @Override
    public void destroy(){
        jedis.close();
        thread.interrupt();
    }

    @Override
    public void handle(@Nonnull SimplePlayer player, @Nonnull String message) {
        Message msg = new Message(channel.getChannelConfig().getString("format"));
        msg.add("player", player.getName());
        msg.add("channel", channel.getDisplayName());
        MapTree cont =  new MapTree()
                .put("msg", MiniMessage.miniMessage().serialize(PatternModule.handleMessage(player.player, message)))
                .put("sender", player.getName());
        jedis.publish("unichat-channel-" + channel.getId(), cont.toJson());
    }
}
