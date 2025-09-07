package dev.onelili.unichat.velocity.channel.type;

import com.velocitypowered.api.proxy.Player;
import dev.onelili.unichat.velocity.UniChat;
import dev.onelili.unichat.velocity.channel.Channel;
import dev.onelili.unichat.velocity.channel.ChannelHandler;
import dev.onelili.unichat.velocity.message.Message;
import dev.onelili.unichat.velocity.module.PatternModule;
import dev.onelili.unichat.velocity.util.SimplePlayer;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
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
        Component component = msg.toComponent().append(PatternModule.handleMessage(player.player, message));

        for(Player receiver : UniChat.getProxy().getAllPlayers()) {
            receiver.sendMessage(component);
        }
        if(channel.isLogToConsole())
            UniChat.getProxy().getConsoleCommandSource().sendMessage(component);
    }
}
