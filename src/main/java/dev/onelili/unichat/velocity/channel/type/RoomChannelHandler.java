package dev.onelili.unichat.velocity.channel.type;

import com.velocitypowered.api.proxy.Player;
import dev.onelili.unichat.velocity.channel.Channel;
import dev.onelili.unichat.velocity.channel.ChannelHandler;
import dev.onelili.unichat.velocity.message.Message;
import dev.onelili.unichat.velocity.module.PatternModule;
import dev.onelili.unichat.velocity.util.ShitMountainException;
import dev.onelili.unichat.velocity.util.SimplePlayer;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RoomChannelHandler implements ChannelHandler {
    public static Map<SimplePlayer, String> rooms = new ConcurrentHashMap<>();

    private final Channel channel;

    public RoomChannelHandler(@Nonnull Channel channel) {
        this.channel = channel;
    }

    @Override
    public void handle(@NotNull SimplePlayer player, @NotNull String message) {
        if(!rooms.containsKey(player)) throw new ShitMountainException("Player "+player.getName()+" is not in a room but somehow called room channel!");
        Component component = new Message(channel.getChannelConfig().getString("format"))
                .add("player", player.getName())
                .add("room_code", rooms.get(player)).toComponent().append(PatternModule.handleMessage(player.player, message));
        for(SimplePlayer p : getPlayersInRoom(rooms.get(player))){
            p.player.sendMessage(component);
        }
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
