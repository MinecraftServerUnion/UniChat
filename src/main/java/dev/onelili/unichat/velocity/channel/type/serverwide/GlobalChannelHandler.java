package dev.onelili.unichat.velocity.channel.type.serverwide;

import com.velocitypowered.api.proxy.Player;
import dev.onelili.unichat.velocity.UniChat;
import dev.onelili.unichat.velocity.channel.Channel;
import dev.onelili.unichat.velocity.channel.ChannelHandler;
import dev.onelili.unichat.velocity.channel.type.ServerWideChannelHandler;
import dev.onelili.unichat.velocity.handler.ChatHistoryManager;
import dev.onelili.unichat.velocity.message.Message;
import dev.onelili.unichat.velocity.module.PatternModule;
import dev.onelili.unichat.velocity.util.PlaceholderUtil;
import dev.onelili.unichat.velocity.util.SimplePlayer;
import lombok.SneakyThrows;
import net.kyori.adventure.chat.ChatType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.List;

public class GlobalChannelHandler extends ServerWideChannelHandler {
    private final Channel channel;

    @SneakyThrows
    public GlobalChannelHandler(@Nonnull Channel channel) {
        this.channel = channel;
    }

    @Override
    public List<SimplePlayer> getReceivers(SimplePlayer sender) {
        return UniChat.getProxy().getAllPlayers().stream().map(SimplePlayer::new).toList();
    }

    @Override
    public Component getPrefix(String text, SimplePlayer sender) {
        Message msg = new Message(text);
        msg.add("player", sender.getName());
        msg.add("channel", channel.getDisplayName());
        return msg.toComponent();
    }

    @Override
    public Channel getChannel() {
        return channel;
    }
}
