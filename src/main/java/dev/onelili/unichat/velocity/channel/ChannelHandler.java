package dev.onelili.unichat.velocity.channel;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import dev.onelili.unichat.velocity.channel.type.serverwide.RoomChannelHandler;
import dev.onelili.unichat.velocity.message.Message;
import dev.onelili.unichat.velocity.util.SimplePlayer;

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
