package dev.onelili.unichat.velocity.module;

import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemEnchantments;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.velocitypowered.api.proxy.Player;
import dev.onelili.unichat.velocity.gui.GUIData;
import dev.onelili.unichat.velocity.util.*;
import dev.onelili.unichat.velocity.message.Message;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ShowItemModule extends PatternModule {
    @Getter
    private static final TimedHashMap<UUID, GUIData> guiMap = new TimedHashMap<>();

    @Override
    public @Nonnull Component handle(@Nonnull Player sender, List<SimplePlayer> receivers) {
        PlayerData data = PlayerData.getPlayerData(sender);
        if (data == null || data.getHandItem() < 0)
            return new Message("&7[&fNone&7]").toComponent();
        ItemStack item = data.getInventory().get(data.getHandItem() + 36);
//        Logger.info(item==null?"null":item.getType().getComponents(ClientVersion.V_1_20_2).toString());
        if (item == null)
            return new Message("&7[&fNone&7]").toComponent();

        UUID uuid = UUID.randomUUID();
        item.getType().getComponents(ClientVersion.V_1_20_2);
        //Components{StaticComponentType[minecraft:enchantments]=ItemEnchantments{enchantments={}, showInTooltip=true}, StaticComponentType[minecraft:rarity]=COMMON, StaticComponentType[minecraft:max_stack_size]=64, StaticComponentType[minecraft:repair_cost]=0, StaticComponentType[minecraft:lore]=ItemLore{lines=[]}, StaticComponentType[minecraft:attribute_modifiers]=ItemAttributeModifiers{modifiers=[], showInTooltip=true}}
        guiMap.put(uuid, GUIData.ofSingleItem(item, Message.getMessage("show-item.window-title").toComponent()));
        System.out.println(uuid.toString());
        return Component.empty()
                .append(Component.text("[").color(NamedTextColor.GRAY))
                .append(
                        item.getComponentOr(ComponentTypes.CUSTOM_NAME, item.getComponentOr(ComponentTypes.ITEM_NAME, Component.text("Unknown")))
                )
                .append(Component.text("]").color(NamedTextColor.GRAY));
//                .hoverEvent(item.getComponents().);
    }
}
