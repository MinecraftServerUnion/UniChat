package cn.jason31416.chatx.handler;

import cn.jason31416.chatx.util.*;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.ServerInfo;
import cn.jason31416.chatx.ChatX;
import cn.jason31416.chatx.channel.Channel;
import cn.jason31416.chatx.command.DirectMessageCommand;
import cn.jason31416.chatx.message.Message;
import cn.jason31416.chatx.module.PatternModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import javax.annotation.Nonnull;
import java.util.List;

public class EventListener {
    @SuppressWarnings("deprecation")
    @Subscribe(priority = Short.MIN_VALUE)
    public void onPlayerChat(@Nonnull PlayerChatEvent event) {
        if(!event.getResult().isAllowed()) return;
        long timemuted = PunishmentHandler.fetchMuted(new SimplePlayer(event.getPlayer()));
        if(timemuted!=-1){
//            System.out.println(timemuted+" "+System.currentTimeMillis());
            event.getPlayer().sendMessage(Message.getMessage("chat.player-is-muted").add("time_left", TimeUtil.displayMillis(timemuted-System.currentTimeMillis())).toComponent());
            event.setResult(PlayerChatEvent.ChatResult.denied());
            return;
        }
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
        else{
            if (channel.getChannelConfig().getBoolean("respect-backend", true)) {
                Component msg = PatternModule.handleMessage(event.getPlayer(), message, List.of());
                if(channel.isLogToConsole()) {
                    PlaceholderUtil.replacePlaceholders(channel.getChannelConfig().getString("format"), event.getPlayer())
                            .thenAccept(text -> {
                                Component component = new Message(text)
                                        .add("player", event.getPlayer().getUsername())
                                        .add("channel", channel.getDisplayName())
                                        .toComponent().append(msg);
                                ChatX.getProxy().getConsoleCommandSource().sendMessage(component);
                            });
                }
                ChatHistoryManager.recordMessage(event.getPlayer().getUsername(),
                        channel.getId(),
                        event.getPlayer().getCurrentServer().map(ServerConnection::getServerInfo).map(ServerInfo::getName).orElse("Unknown"),
                        LegacyComponentSerializer.legacyAmpersand().serialize(msg));
            }

            if(channel.getChannelConfig().getBoolean("respect-backend", true)) {
                return;
            }
        }
        ChatX.getProxy().getScheduler()
                .buildTask(ChatX.getInstance(),
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
