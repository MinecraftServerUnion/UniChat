package dev.onelili.unichat.velocity.gui;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerCloseWindow;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenWindow;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems;
import com.velocitypowered.api.proxy.Player;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GUIContainer {
    @Getter
    private static final Set<GUIContainer> guis = new HashSet<>();
    @Nonnull
    @Getter
    private GUIData data;

    public static @Nonnull GUIContainer of(@Nonnull GUIData data) {
        return new GUIContainer(data);
    }

    public void close(@Nonnull Player player) {
        guis.remove(this);
        WrapperPlayServerCloseWindow wrapper = new WrapperPlayServerCloseWindow();
        wrapper.setWindowId(data.windowId());
    }

    public void open(@Nonnull Player player) {
        guis.add(this);
        if(data.slots() / 9 - 1 <= 5 || data.slots() / 9 - 1 >= 0) {
            WrapperPlayServerOpenWindow wrapper = new WrapperPlayServerOpenWindow(
                    data.windowId(),
                    data.slots() / 9 - 1,
                    data.title()
            );
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, wrapper);

            List<ItemStack> items = new ArrayList<>();
            for(int i = 0; i <= data.slots() / 9 * 9; i++)
                if(data.items().get(i) != null)
                    items.add(data.items().get(i));
                else
                    items.add(ItemStack.EMPTY);
            WrapperPlayServerWindowItems wrapper1 = new WrapperPlayServerWindowItems(
                    data.windowId(),
                    data.stateId(),
                    items,
                    null
            );
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, wrapper1);
        }
    }
}
