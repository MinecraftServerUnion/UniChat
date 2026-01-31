package cn.jason31416.chatx.channel.type;

import cn.jason31416.chatx.ChatX;
import cn.jason31416.chatx.channel.Channel;
import cn.jason31416.chatx.channel.ChannelHandler;
import cn.jason31416.chatx.handler.ChatHistoryManager;
import cn.jason31416.chatx.message.Message;
import cn.jason31416.chatx.module.PatternModule;
import cn.jason31416.chatx.util.PlaceholderUtil;
import cn.jason31416.chatx.util.SimplePlayer;
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
        if(getChannel().getRateLimiter()!=null&&!getChannel().getRateLimiter().invoke(player.getName())){
            player.sendMessage(Message.getMessage("chat.rate-limited"));
            return;
        }
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
                            ChatX.getProxy().getConsoleCommandSource().sendMessage(component);
                    }

                    for(SimplePlayer receiver : receivers) {
                        if(getChannel().getReceivePermission() != null&&!receiver.hasPermission(getChannel().getReceivePermission()))
                            continue;
                        receiver.getPlayer().sendMessage(component, ChatType.CHAT.bind(component));
                    }
                });
    }
}
