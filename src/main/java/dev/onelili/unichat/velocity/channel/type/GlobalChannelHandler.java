package dev.onelili.unichat.velocity.channel.type;

import com.velocitypowered.api.proxy.Player;
import dev.onelili.unichat.velocity.UniChat;
import dev.onelili.unichat.velocity.channel.Channel;
import dev.onelili.unichat.velocity.channel.ChannelHandler;
import dev.onelili.unichat.velocity.handler.ChatHistoryManager;
import dev.onelili.unichat.velocity.message.Message;
import dev.onelili.unichat.velocity.module.PatternModule;
import dev.onelili.unichat.velocity.util.SimplePlayer;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class GlobalChannelHandler implements ChannelHandler {
    private final Channel channel;

    @SneakyThrows
    public GlobalChannelHandler(@Nonnull Channel channel) {
        this.channel = channel;
    }

    @Override
    public void handle(@Nonnull SimplePlayer player, @NotNull String message) {
        Message msg = new Message(channel.getChannelConfig().getString("format"));
        msg.add("player", player.getName());
        msg.add("channel", channel.getDisplayName());
        Component cmp = PatternModule.handleMessage(player.player, message, true);
        Component component = msg.toComponent().append(cmp);

        ChatHistoryManager.recordMessage(player.getName(),
                channel.getId(),
                player.player.getCurrentServer().isPresent()?player.player.getCurrentServer().get().getServerInfo().getName():null,
                LegacyComponentSerializer.legacyAmpersand().serialize(cmp));

        for(Player receiver : UniChat.getProxy().getAllPlayers()) {
            if(channel.getReceivePermission() != null&&!receiver.hasPermission(channel.getReceivePermission()))
                continue;
            receiver.sendMessage(component);
        }
        if(channel.isLogToConsole())
            UniChat.getProxy().getConsoleCommandSource().sendMessage(component);
    }
}
