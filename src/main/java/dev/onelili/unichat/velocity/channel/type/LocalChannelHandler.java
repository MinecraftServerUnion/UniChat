package dev.onelili.unichat.velocity.channel.type;

import com.velocitypowered.api.proxy.Player;
import dev.onelili.unichat.velocity.UniChat;
import dev.onelili.unichat.velocity.channel.Channel;
import dev.onelili.unichat.velocity.channel.ChannelHandler;
import dev.onelili.unichat.velocity.handler.ChatHistoryManager;
import dev.onelili.unichat.velocity.message.Message;
import dev.onelili.unichat.velocity.module.PatternModule;
import dev.onelili.unichat.velocity.util.SimplePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

public class LocalChannelHandler implements ChannelHandler {
    Channel channel;
    public LocalChannelHandler(Channel channel) {
        this.channel = channel;
    }
    @Override
    public void handle(@NotNull SimplePlayer player, @NotNull String message) {
        Component msg = PatternModule.handleMessage(player.player, message, true);
        Component component = new Message(channel.getChannelConfig().getString("format"))
                .add("player", player.getName())
                .add("channel", channel.getDisplayName())
                .toComponent().append(msg);

        ChatHistoryManager.recordMessage(player.getName(),
                channel.getId(),
                player.player.getCurrentServer().isPresent()?player.player.getCurrentServer().get().getServerInfo().getName():null,
                LegacyComponentSerializer.legacyAmpersand().serialize(msg));
        if (channel.isLogToConsole())
            UniChat.getProxy().getConsoleCommandSource().sendMessage(component);
        if (player.player.getCurrentServer().isPresent()) {
            for (Player pl : player.player.getCurrentServer().get().getServer().getPlayersConnected()) {
                pl.sendMessage(component);
            }
        }
    }

}
