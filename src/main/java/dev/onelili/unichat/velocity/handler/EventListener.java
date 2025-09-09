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
        if (channel.getHandler() == null) {
            Component msg = PatternModule.handleMessage(event.getPlayer(), message, true);
            Component component = new Message(channel.getChannelConfig().getString("format"))
                    .add("player", event.getPlayer().getUsername())
                    .add("channel", channel.getDisplayName())
                    .toComponent().append(msg);
            ChatHistoryManager.recordMessage(event.getPlayer().getUsername(),
                    channel.getId(),
                    event.getPlayer().getCurrentServer().isPresent()?event.getPlayer().getCurrentServer().get().getServerInfo().getName():null,
                    LegacyComponentSerializer.legacyAmpersand().serialize(msg));
            UniChat.getProxy().getScheduler()
                    .buildTask(UniChat.getInstance(),
                            () -> {
                                if (channel.isLogToConsole())
                                    UniChat.getProxy().getConsoleCommandSource().sendMessage(component);
                                if (event.getPlayer().getCurrentServer().isPresent()) {
                                    for (Player player : event.getPlayer().getCurrentServer().get().getServer().getPlayersConnected()) {
                                        player.sendMessage(component);
                                    }
                                }
                            })
                    .schedule();
            if (event.getPlayer().getCurrentServer().isPresent()) {
                String serverid = event.getPlayer().getCurrentServer().get().getServerInfo().getName();
                if (channel.getChannelConfig().getStringList("force-handle-servers").contains(serverid)) {
                    event.setResult(PlayerChatEvent.ChatResult.denied());
                    return;
                }
            }
            return;
        }
        if(event.getPlayer().getCurrentServer().isPresent()){
            String serverid = event.getPlayer().getCurrentServer().get().getServerInfo().getName();
            if (channel.getRestrictedServers().contains(serverid)) {
                event.setResult(PlayerChatEvent.ChatResult.denied());
                event.getPlayer().sendMessage(Message.getMessage("chat.channel-declined").toComponent());
                return;
            }
        }
        event.setResult(PlayerChatEvent.ChatResult.denied());
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
