package dev.onelili.unichat.velocity.gui;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
@Accessors(chain = true, fluent = true)
@Builder
public class GUIData {
    @Builder.Default
    private final int windowId = new Random().nextInt(Byte.MAX_VALUE);
    @Builder.Default
    private final int stateId = new Random().nextInt(Short.MAX_VALUE, Integer.MAX_VALUE);
    @Builder.Default
    private Component title = Component.empty();
    @Builder.Default
    private int slots = 27;
    @Builder.Default
    private Map<Integer, ItemStack> items = new ConcurrentHashMap<>();

    public static @Nonnull GUIData ofSingleItem(@Nonnull ItemStack item, @Nullable Component title) {
        Map<Integer, ItemStack> items = new ConcurrentHashMap<>();
        for(int i = 0; i < 27; i++)
            if(i == 13)
                items.put(i, item);
            else
                items.put(i, ItemStack.builder().type(ItemTypes.GRAY_STAINED_GLASS_PANE).build());
        return builder()
                .title(Optional.ofNullable(title).orElse(Component.empty()))
                .slots(27)
                .items(items)
                .build();
    }
}
