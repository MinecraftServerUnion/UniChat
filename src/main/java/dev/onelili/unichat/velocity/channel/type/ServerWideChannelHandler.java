package dev.onelili.unichat.velocity.channel.type;

import com.velocitypowered.api.proxy.Player;
import dev.onelili.unichat.velocity.UniChat;
import dev.onelili.unichat.velocity.channel.Channel;
import dev.onelili.unichat.velocity.channel.ChannelHandler;
import dev.onelili.unichat.velocity.handler.ChatHistoryManager;
import dev.onelili.unichat.velocity.message.Message;
import dev.onelili.unichat.velocity.module.PatternModule;
import dev.onelili.unichat.velocity.util.PlaceholderUtil;
import dev.onelili.unichat.velocity.util.SimplePlayer;
import net.kyori.adventure.chat.ChatType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import javax.annotation.Nonnull;
import java.util.List;

public abstract class ServerWideChannelHandler implements ChannelHandler {
    public abstract List<SimplePlayer> getReceivers(SimplePlayer sender);
    public abstract Component getPrefix(String text, SimplePlayer sender);
    public abstract Channel getChannel();

    @Override
    public void handle(@Nonnull SimplePlayer player, @Nonnull String message) {
        PlaceholderUtil.replacePlaceholders(getChannel().getChannelConfig().getString("format"), player.getPlayer())
                .thenAccept(text->{
                    List<SimplePlayer> receivers = getReceivers(player);
                    Component cmp = PatternModule.handleMessage(player.getPlayer(), message, receivers);
                    Component component = getPrefix(text, player).append(cmp);

                    if(!getChannel().isPassthrough() || !getChannel().getChannelConfig().getBoolean("respect-backend", true)){
                        ChatHistoryManager.recordMessage(player.getName(),
                                getChannel().getId(),
                                player.getCurrentServer(),
                                LegacyComponentSerializer.legacyAmpersand().serialize(cmp));

                        if (getChannel().isLogToConsole())
                            UniChat.getProxy().getConsoleCommandSource().sendMessage(component);
                    }

                    for(SimplePlayer receiver : receivers) {
                        if(getChannel().getReceivePermission() != null&&!receiver.hasPermission(getChannel().getReceivePermission()))
                            continue;
                        receiver.getPlayer().sendMessage(component, ChatType.CHAT.bind(component));
                    }
                });
    }
}
