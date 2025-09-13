package dev.onelili.unichat.velocity.util;

import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemLore;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.NBTList;
import com.github.retrooper.packetevents.protocol.nbt.NBTString;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import javax.annotation.Nonnull;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemUtils {
    public static @Nonnull ItemStack fixItem(@Nonnull ItemStack item) {
        NBTCompound nbt = item.getNBT();
        if(nbt != null) {
            if(nbt.getTagOrNull("display") instanceof NBTCompound display && display.getTagOrNull("Name") instanceof NBTString name && item.getComponent(ComponentTypes.CUSTOM_NAME).isEmpty())
                item.setComponent(ComponentTypes.CUSTOM_NAME, GsonComponentSerializer.gson().deserialize(name.getValue()));
            if(nbt.getTagOrNull("display") instanceof NBTCompound display && display.getTagOrNull("Lore") instanceof NBTList<?> lore && (item.getComponent(ComponentTypes.LORE).isEmpty() || item.getComponent(ComponentTypes.LORE).get().getLines().isEmpty()))
                item.setComponent(ComponentTypes.LORE, new ItemLore(
                        lore.getTags().stream().map(tag -> {
                            if(tag instanceof NBTString loreLine)
                                return GsonComponentSerializer.gson().deserialize(loreLine.getValue());
                            else
                                return null;
                        }).filter(Objects::nonNull).toList()
                ));
        }
        return item;
    }
}
