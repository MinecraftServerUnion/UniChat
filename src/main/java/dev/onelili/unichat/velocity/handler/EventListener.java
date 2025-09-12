package dev.onelili.unichat.velocity.handler;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import dev.onelili.unichat.velocity.UniChat;
import dev.onelili.unichat.velocity.channel.Channel;
import dev.onelili.unichat.velocity.command.DirectMessageCommand;
import dev.onelili.unichat.velocity.message.Message;
import dev.onelili.unichat.velocity.module.PatternModule;
import dev.onelili.unichat.velocity.util.Config;
import dev.onelili.unichat.velocity.util.SimplePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import javax.annotation.Nonnull;

public class EventListener {
    @SuppressWarnings("deprecation")
    @Subscribe(priority = Short.MIN_VALUE)
    public void onPlayerChat(@Nonnull PlayerChatEvent event) {
        Channel channel;
        String message;
        if (Channel.channelPrefixes.containsKey(event.getMessage().substring(0, 1))) {
            channel = Channel.channelPrefixes.get(event.getMessage().substring(0, 1));
            message = event.getMessage().substring(1);
        } else {
            channel = Channel.getPlayerChannel(event.getPlayer());
            message = event.getMessage();
        }
        if (channel == null) return;
        if (event.getPlayer().getCurrentServer().isPresent()) {
            if (channel.isPassthrough() && Config.getConfigTree().getStringList("unhandled-servers").contains(event.getPlayer().getCurrentServer().get().getServerInfo().getName())){
                return;
            }
        }
        if(event.getPlayer().getCurrentServer().isPresent()){
            String serverid = event.getPlayer().getCurrentServer().get().getServerInfo().getName();
            if (channel.getRestrictedServers().contains(serverid)) {
                event.setResult(PlayerChatEvent.ChatResult.denied());
                event.getPlayer().sendMessage(Message.getMessage("chat.channel-declined").toComponent());
                return;
            }
        }
        if(channel.getSendPermission()!=null&&!event.getPlayer().hasPermission(channel.getSendPermission())){
            event.getPlayer().sendMessage(Message.getMessage("chat.no-send-permission").toComponent());
            event.setResult(PlayerChatEvent.ChatResult.denied());
            return;
        }
        if (!channel.isPassthrough()) event.setResult(PlayerChatEvent.ChatResult.denied());
        else if(channel.getChannelConfig().getBoolean("respect-backend", true)) return;
        UniChat.getProxy().getScheduler()
                .buildTask(UniChat.getInstance(),
                        () -> Channel.handleChat(event.getPlayer(), channel, message))
                .schedule();
    }
    @Subscribe
    public void onPlayerJoin(@Nonnull LoginEvent event){
        Channel.getPlayerChannels().put(event.getPlayer().getUniqueId(), Channel.defaultChannel);
    }

    @Subscribe
    public void onPlayerLeave(@Nonnull DisconnectEvent event){
        Channel.getPlayerChannels().remove(event.getPlayer().getUniqueId());
        PlayerData.getPlayerDataMap().remove(event.getPlayer().getUniqueId());
        DirectMessageCommand.lastMessage.remove(event.getPlayer().getUniqueId());
    }
}
