package dev.onelili.unichat.velocity.channel;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.chat.LastSeenMessages;
import com.github.retrooper.packetevents.util.crypto.MessageSignData;
import com.github.retrooper.packetevents.util.crypto.SaltSignature;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatMessage;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import dev.onelili.unichat.velocity.UniChat;
import dev.onelili.unichat.velocity.channel.type.RoomChannelHandler;
import dev.onelili.unichat.velocity.message.Message;
import dev.onelili.unichat.velocity.util.Config;
import dev.onelili.unichat.velocity.util.SimplePlayer;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface ChannelHandler {
    void handle(@Nonnull SimplePlayer player, @Nonnull String message);

    default void destroy() {}

    default SimpleCommand getCommand(Channel channel) {
        return invocation -> {
            if (!(invocation.source() instanceof Player pl)) {
                invocation.source().sendMessage(Message.getMessage("command.cannot-execute-from-console").toComponent());
                return;
            }
            if(channel.getSendPermission()!=null&&!pl.hasPermission(channel.getSendPermission())){
                pl.sendMessage(Message.getMessage("chat.no-send-permission").toComponent());
                return;
            }
            if (invocation.arguments().length == 0) {
                pl.sendMessage(Message.getMessage("command.joined-channel").add("channel", channel.getDisplayName()).toComponent());
                Channel.getPlayerChannels().put(pl.getUniqueId(), channel);
                RoomChannelHandler.leaveRoom(pl);
            } else {
                if(channel.isPassthrough()) {
                    pl.sendMessage(Message.getMessage("chat.cannot-command-send-passthrough").toComponent());
                }else{
                    Channel.handleChat(pl, channel, String.join(" ", invocation.arguments()));
                }
            }
        };
    }
}
