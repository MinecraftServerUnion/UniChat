package dev.onelili.unichat.velocity.channel.type;

import com.velocitypowered.api.proxy.Player;
import dev.onelili.unichat.velocity.UniChat;
import dev.onelili.unichat.velocity.channel.Channel;
import dev.onelili.unichat.velocity.channel.ChannelHandler;
import dev.onelili.unichat.velocity.handler.ChatHistoryManager;
import dev.onelili.unichat.velocity.handler.RedisRemoteManager;
import dev.onelili.unichat.velocity.message.Message;
import dev.onelili.unichat.velocity.module.PatternModule;
import dev.onelili.unichat.velocity.util.Config;
import dev.onelili.unichat.velocity.util.Logger;
import dev.onelili.unichat.velocity.util.MapTree;
import dev.onelili.unichat.velocity.util.SimplePlayer;
import lombok.Getter;
import lombok.SneakyThrows;
import net.kyori.adventure.chat.ChatType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import redis.clients.jedis.*;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class RedisChannelHandler implements ChannelHandler {
    @Getter
    private static Map<String, RedisChannelHandler> redisChannels = new HashMap<>();

    @Getter
    private Channel channel;

    @SneakyThrows
    public RedisChannelHandler(@Nonnull Channel channel) {
        this.channel = channel;

        redisChannels.put(channel.getId(), this);

        if(RedisRemoteManager.getInstance()==null){
            throw new IllegalArgumentException("Redis must be enabled in config.yml in order to use Redis-based channels!");
        }
    }

    @Override
    public void handle(@Nonnull SimplePlayer player, @Nonnull String message) {
        MapTree cont = new MapTree()
                .put("msg", MiniMessage.miniMessage().serialize(PatternModule.handleMessage(player.getPlayer(), message, List.of())))
                .put("sender", player.getName())
                .put("server", Config.getString("server-name"))
                .put("type", "channel")
                .put("channel", channel.getId());
        RedisRemoteManager.getInstance().getJedis().publish("unichat-channel", cont.toJson());
    }

    public void receive(String sender, String server, Component cmp) {
        Message msg = new Message(channel.getChannelConfig().getString("format"));
        msg.add("player", sender);
        msg.add("server", server);
        msg.add("channel", channel.getDisplayName());
        Component component = msg.toComponent().append(cmp);
        for(Player receiver : UniChat.getProxy().getAllPlayers()) {
            if(channel.getReceivePermission() != null&&!receiver.hasPermission(channel.getReceivePermission()))
                continue;
            receiver.sendMessage(component, ChatType.CHAT.bind(component));
        }
        if(channel.isLogToConsole())
            UniChat.getProxy().getConsoleCommandSource().sendMessage(component);
    }
}
