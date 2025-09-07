package dev.onelili.unichat.velocity.gui;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerCloseWindow;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenWindow;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems;
import com.velocitypowered.api.proxy.Player;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@NoArgsConstructor
@Data
@Accessors(chain = true, fluent = true)
public class GUISession {
    private int windowId = new Random().nextInt();
    private int stateId = new Random().nextInt();
    private int slots = 27;
    private Player player;
    @Nullable
    private Component title = null;
    private Map<Integer, ItemStack> inventory = new ConcurrentHashMap<>();

    public void close() {
        WrapperPlayServerCloseWindow wrapper = new WrapperPlayServerCloseWindow();
        wrapper.setWindowId(windowId);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, wrapper);
    }
    public void open() {
        Component windowTitle = title == null ? Component.empty() : title;
        if(player != null && slots / 9 - 1 <= 5 || slots / 9 - 1 >= 0) {
            WrapperPlayServerOpenWindow wrapper = new WrapperPlayServerOpenWindow(
                    windowId,
                    slots / 9 - 1,
                    windowTitle
            );
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, wrapper);

            List<ItemStack> items = new ArrayList<>();
            for(int i = 0; i <= slots / 9 * 9; i++)
                if(inventory.get(i) != null)
                    items.add(inventory.get(i));
                else
                    items.add(ItemStack.EMPTY);
            WrapperPlayServerWindowItems wrapper1 = new WrapperPlayServerWindowItems(
                    windowId,
                    stateId,
                    items,
                    null
            );
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, wrapper1);
        }
    }
}
