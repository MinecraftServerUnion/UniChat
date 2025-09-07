package dev.onelili.unichat.velocity.handler;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3i;
import com.velocitypowered.api.proxy.Player;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@NoArgsConstructor
@Accessors(chain = true)
@Data
public class PlayerData {
    @Getter
    private static final Map<UUID, PlayerData> playerDataMap = new ConcurrentHashMap<>();
    private Vector3d position = new Vector3d(0,0,0);
    private int handItem = -1;
    private final Map<Integer, ItemStack> inventory = new ConcurrentHashMap<>();

    public Vector3i toPacketPosition() {
        return new Vector3i((int)(position.getX() * 8), (int)(position.getY() * 8), (int)(position.getZ() * 8));
    } // This is the "packet location" of the player, which is *8 scale

    public static @Nullable PlayerData getPlayerData(@Nonnull Player player) {
        return playerDataMap.getOrDefault(player.getUniqueId(), new PlayerData());
    }
}
