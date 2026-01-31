package cn.jason31416.chatx.channel;

import cn.jason31416.chatx.handler.PunishmentHandler;
import cn.jason31416.chatx.util.TimeUtil;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import cn.jason31416.chatx.channel.type.serverwide.RoomChannelHandler;
import cn.jason31416.chatx.message.Message;
import cn.jason31416.chatx.util.SimplePlayer;

import javax.annotation.Nonnull;

public interface ChannelHandler {
    void handle(@Nonnull SimplePlayer player, @Nonnull String message);

    default void destroy() {}

    default SimpleCommand getCommand(Channel channel) {
        return invocation -> {
            if(channel.getSendPermission()!=null&&!invocation.source().hasPermission(channel.getSendPermission())){
                invocation.source().sendMessage(Message.getMessage("chat.no-send-permission").toComponent());
                return;
            }
            if(invocation.source() instanceof Player sender) {
                long timeMuted= PunishmentHandler.fetchMuted(new SimplePlayer(sender));
                if(timeMuted!=-1){
                    sender.sendMessage(Message.getMessage("chat.player-is-muted").add("time_left", TimeUtil.displayMillis(timeMuted-System.currentTimeMillis())).toComponent());
                    return;
                }
            }
            if (invocation.arguments().length == 0) {
                if (!(invocation.source() instanceof Player pl)) {
                    invocation.source().sendMessage(Message.getMessage("command.cannot-execute-from-console").toComponent());
                    return;
                }
                pl.sendMessage(Message.getMessage("command.joined-channel").add("channel", channel.getDisplayName()).toComponent());
                Channel.getPlayerChannels().put(pl.getUniqueId(), channel);
                RoomChannelHandler.leaveRoom(pl);
            } else {
                if(channel.isPassthrough()) {
                    invocation.source().sendMessage(Message.getMessage("chat.cannot-command-send-passthrough").toComponent());
                }else{
                    if (!(invocation.source() instanceof Player pl)) {
                        Channel.handleChat(null, channel, String.join(" ", invocation.arguments()));
                        return;
                    }
                    Channel.handleChat(pl, channel, String.join(" ", invocation.arguments()));
                }
            }
        };
    }
}
