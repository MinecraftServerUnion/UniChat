package dev.onelili.unichat.velocity.channel.type.serverwide;

import com.velocitypowered.api.proxy.Player;
import dev.onelili.unichat.velocity.channel.Channel;
import dev.onelili.unichat.velocity.channel.ChannelHandler;
import dev.onelili.unichat.velocity.channel.type.ServerWideChannelHandler;
import dev.onelili.unichat.velocity.message.Message;
import dev.onelili.unichat.velocity.module.PatternModule;
import dev.onelili.unichat.velocity.util.PlaceholderUtil;
import dev.onelili.unichat.velocity.util.SimplePlayer;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LocalChannelHandler extends ServerWideChannelHandler {
    Channel channel;
    public LocalChannelHandler(Channel channel) {
        this.channel = channel;
    }

    @Override
    public List<SimplePlayer> getReceivers(SimplePlayer sender) {
        if (sender.getCurrentServer()==null) {
            return List.of();
        }
        return sender.getServerPlayers().stream().map(SimplePlayer::new).toList();
    }

    @Override
    public Component getPrefix(String text, SimplePlayer sender) {
        return new Message(text)
                .add("player", sender.getName())
                .add("channel", channel.getDisplayName())
                .toComponent();
    }

    @Override
    public Channel getChannel() {
        return channel;
    }
}
