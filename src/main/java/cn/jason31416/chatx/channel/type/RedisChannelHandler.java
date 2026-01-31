package cn.jason31416.chatx.channel.type;

import com.velocitypowered.api.proxy.Player;
import cn.jason31416.chatx.ChatX;
import cn.jason31416.chatx.channel.Channel;
import cn.jason31416.chatx.channel.ChannelHandler;
import cn.jason31416.chatx.handler.RedisRemoteManager;
import cn.jason31416.chatx.message.Message;
import cn.jason31416.chatx.module.PatternModule;
import cn.jason31416.chatx.util.Config;
import cn.jason31416.chatx.util.MapTree;
import cn.jason31416.chatx.util.SimplePlayer;
import lombok.Getter;
import lombok.SneakyThrows;
import net.kyori.adventure.chat.ChatType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import javax.annotation.Nonnull;
import java.util.*;

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
        if(channel.getRateLimiter()!=null&&!channel.getRateLimiter().invoke(player.getName())){
            player.sendMessage(Message.getMessage("chat.rate-limited"));
            return;
        }
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
        for(Player receiver : ChatX.getProxy().getAllPlayers()) {
            if(channel.getReceivePermission() != null&&!receiver.hasPermission(channel.getReceivePermission()))
                continue;
            receiver.sendMessage(component, ChatType.CHAT.bind(component));
        }
        if(channel.isLogToConsole())
            ChatX.getProxy().getConsoleCommandSource().sendMessage(component);
    }
}
