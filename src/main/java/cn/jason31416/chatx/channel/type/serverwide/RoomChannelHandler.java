package cn.jason31416.chatx.channel.type.serverwide;

import cn.jason31416.chatx.handler.PunishmentHandler;
import cn.jason31416.chatx.util.TimeUtil;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import cn.jason31416.chatx.ChatX;
import cn.jason31416.chatx.channel.Channel;
import cn.jason31416.chatx.channel.type.ServerWideChannelHandler;
import cn.jason31416.chatx.message.Message;
import cn.jason31416.chatx.util.SimplePlayer;
import net.kyori.adventure.text.Component;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class RoomChannelHandler extends ServerWideChannelHandler {
    public static Map<SimplePlayer, String> rooms = new ConcurrentHashMap<>();

    private final Channel channel;

    public RoomChannelHandler(@Nonnull Channel channel) {
        this.channel = channel;
    }

    @Override
    public List<SimplePlayer> getReceivers(SimplePlayer sender) {
        return getPlayersInRoom(rooms.get(sender));
    }

    @Override
    public Component getPrefix(String text, SimplePlayer sender) {
        return new Message(text)
                .add("player", sender.getName())
                .add("room_code", rooms.get(sender)).toComponent();
    }

    @Override
    public Channel getChannel() {
        return channel;
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
                if(invocation.source() instanceof Player sender) {
                    long timeMuted= PunishmentHandler.fetchMuted(new SimplePlayer(sender));
                    if(timeMuted!=-1){
                        sender.sendMessage(Message.getMessage("chat.player-is-muted").add("time_left", TimeUtil.displayMillis(timeMuted-System.currentTimeMillis())).toComponent());
                        return;
                    }
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
                            if(ChatX.getProxy().getPlayer(invocation.arguments()[1]).isPresent()){
                                Player target = ChatX.getProxy().getPlayer(invocation.arguments()[1]).get();
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
                    return ChatX.getProxy().getAllPlayers().stream().map(Player::getUsername).toList();
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
