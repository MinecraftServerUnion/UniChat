package dev.onelili.unichat.velocity.handler;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3i;
import com.velocitypowered.api.proxy.Player;

import java.util.Map;
import java.util.UUID;

public class PlayerData {
    public static Map<UUID, PlayerData> playerDataMap = new java.util.HashMap<>();
    public Vector3d position=new Vector3d(0,0,0);
    public ItemStack handItem=null;
    public Vector3i toPacketPosition(){
        return new Vector3i((int)(position.getX() * 8), (int)(position.getY() * 8), (int)(position.getZ() * 8));
    }// This is the "packet location" of the player, which is *8 scale
    public PlayerData(){}

    public static PlayerData getPlayerData(Player player){
        return playerDataMap.getOrDefault(player.getUniqueId(), new PlayerData());
    }
}
