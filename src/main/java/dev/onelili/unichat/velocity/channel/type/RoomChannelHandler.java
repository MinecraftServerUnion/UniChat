package dev.onelili.unichat.velocity.channel.type;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import dev.onelili.unichat.velocity.UniChat;
import dev.onelili.unichat.velocity.channel.Channel;
import dev.onelili.unichat.velocity.channel.ChannelHandler;
import dev.onelili.unichat.velocity.handler.ChatHistoryManager;
import dev.onelili.unichat.velocity.message.Message;
import dev.onelili.unichat.velocity.module.PatternModule;
import dev.onelili.unichat.velocity.util.ShitMountainException;
import dev.onelili.unichat.velocity.util.SimplePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class RoomChannelHandler implements ChannelHandler {
    public static Map<SimplePlayer, String> rooms = new ConcurrentHashMap<>();

    private final Channel channel;

    public RoomChannelHandler(@Nonnull Channel channel) {
        this.channel = channel;
    }

    @Override
    public void handle(@NotNull SimplePlayer player, @NotNull String message) {
        if(!rooms.containsKey(player))
            throw new ShitMountainException("Player "+player.getName()+" is not in a room but somehow called room channel!");
        Component msg = PatternModule.handleMessage(player.player, message, true),
                component = new Message(channel.getChannelConfig().getString("format"))
                .add("player", player.getName())
                .add("room_code", rooms.get(player)).toComponent()
                .append(msg);

        ChatHistoryManager.recordMessage(player.getName(),
                channel.getId(),
                rooms.get(player),
                LegacyComponentSerializer.legacyAmpersand().serialize(msg));

        for(SimplePlayer p : getPlayersInRoom(rooms.get(player))){
            if(channel.getReceivePermission() != null&&!p.hasPermission(channel.getReceivePermission()))
                continue;
            p.player.sendMessage(component);
        }
    }

    public SimpleCommand getCommand(Channel channel) {
        return new SimpleCommand(){
            @Override
            public void execute(Invocation invocation) {
                if (!(invocation.source() instanceof Player pl)) {
                    invocation.source().sendMessage(Message.getMessage("command.cannot-execute-from-console").toComponent());
                    return;
                }
                if(channel.getSendPermission()!=null&&!pl.hasPermission(channel.getSendPermission())){
                    pl.sendMessage(Message.getMessage("chat.no-send-permission").toComponent());
                    return;
                }
                if (invocation.arguments().length == 0 || invocation.arguments()[0].equals("create")) {
                    String roomCode = "" + new Random().nextInt(1000, 9999);
                    pl.sendMessage(Message.getMessage("command.joined-room").add("room_code", roomCode).toComponent());
                    Channel.getPlayerChannels().put(pl.getUniqueId(), channel);
                    RoomChannelHandler.joinRoom(pl, roomCode);
                } else {
                    if (invocation.arguments()[0].equals("invite")){
                        if(!RoomChannelHandler.rooms.containsKey(new SimplePlayer(pl))){
                            pl.sendMessage(Message.getMessage("command.not-in-room").toComponent());
                        }else if(invocation.arguments().length >= 2){
                            if(UniChat.getProxy().getPlayer(invocation.arguments()[1]).isPresent()){
                                Player target = UniChat.getProxy().getPlayer(invocation.arguments()[1]).get();
                                pl.sendMessage(Message.getMessage("command.invited-others").add("player", target.getUsername()).toComponent());
                                target.sendMessage(Message.getMessage("command.invited-to-room").add("player", pl.getUsername()).add("room_code", RoomChannelHandler.rooms.get(new SimplePlayer(pl))).toComponent());
                            }
                        }else{
                            pl.sendMessage(Message.getMessage("command.invalid-arguments").toComponent());
                        }
                    }else if (RoomChannelHandler.rooms.containsValue(invocation.arguments()[0])) {
                        Channel.getPlayerChannels().put(pl.getUniqueId(), channel);
                        RoomChannelHandler.joinRoom(pl, invocation.arguments()[0]);
                        pl.sendMessage(Message.getMessage("command.joined-room").add("room_code", invocation.arguments()[0]).toComponent());
                    } else {
                        pl.sendMessage(Message.getMessage("command.room-not-found").toComponent());
                    }
                }
            }
            @Override
            public List<String> suggest(Invocation invocation) {
                if (!(invocation.source() instanceof Player)) {
                    return new ArrayList<>();
                }
                if(invocation.arguments().length == 1) return List.of("invite", "create");
                if(invocation.arguments().length == 2 && invocation.arguments()[0].equals("invite")){
                    return UniChat.getProxy().getAllPlayers().stream().map(Player::getUsername).toList();
                }
                return new ArrayList<>();
            }
        };
    }

    public static void joinRoom(Player player, String room) {
        rooms.put(new SimplePlayer(player), room);
    }
    public static void leaveRoom(Player player) {
        rooms.remove(new SimplePlayer(player));
    }

    public static List<SimplePlayer> getPlayersInRoom(String room) {
        return rooms.entrySet().stream().filter(entry -> entry.getValue().equals(room)).map(Map.Entry::getKey).toList();
    }
}
