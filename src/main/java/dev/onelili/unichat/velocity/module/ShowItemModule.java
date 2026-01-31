package dev.onelili.unichat.velocity.module;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemEnchantments;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.enchantment.Enchantment;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.util.adventure.NbtTagHolder;
import com.velocitypowered.api.proxy.Player;
import dev.onelili.unichat.velocity.UniChat;
import dev.onelili.unichat.velocity.gui.GUIData;
import dev.onelili.unichat.velocity.util.*;
import dev.onelili.unichat.velocity.message.Message;
import lombok.Getter;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.NBTComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.DataComponentValue;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ShowItemModule extends PatternModule {
    @Getter
    private static final TimedHashMap<UUID, GUIData> guiMap = new TimedHashMap<>(180*1000L);

    @Override
    public @Nonnull Component handle(@Nonnull Player sender, List<SimplePlayer> receivers) {
        PlayerData data = PlayerData.getPlayerData(sender);
        if (data == null || data.getHandItem() < 0)
            return new Message("&7[&fNone&7]").toComponent();
        ItemStack item = data.getInventory().get(data.getHandItem() + 27);
//        Logger.info(item==null?"null":item.getType().getComponents(ClientVersion.V_1_20_2).toString());
        if (item == null || item == ItemStack.EMPTY)
            return new Message("&7[&fNone&7]").toComponent();

        UUID uuid = UUID.randomUUID();
        //Components{StaticComponentType[minecraft:enchantments]=ItemEnchantments{enchantments={}, showInTooltip=true}, StaticComponentType[minecraft:rarity]=COMMON, StaticComponentType[minecraft:max_stack_size]=64, StaticComponentType[minecraft:repair_cost]=0, StaticComponentType[minecraft:lore]=ItemLore{lines=[]}, StaticComponentType[minecraft:attribute_modifiers]=ItemAttributeModifiers{modifiers=[], showInTooltip=true}}
        if(!receivers.isEmpty()) {
            guiMap.put(uuid, GUIData.ofSingleItem(item, Message.getMessage("show-item.window-title").toComponent()));
//            System.out.println(uuid.toString());
        }
        Component hoverText = Component.empty();
        Component nameComponent;
        if(item.getComponent(ComponentTypes.CUSTOM_NAME).isPresent()){
            nameComponent = item.getComponent(ComponentTypes.CUSTOM_NAME).get().decorate(TextDecoration.ITALIC);
        }else{
            nameComponent = item.getComponentOr(ComponentTypes.ITEM_NAME, Component.text("Unknown"));
        }
        if(!item.getEnchantments().isEmpty()) nameComponent = nameComponent.color(NamedTextColor.AQUA);
        hoverText = hoverText.append(nameComponent);
        for(Enchantment enchantment : item.getEnchantments()) {
            Component line = Component.empty().color(NamedTextColor.GRAY).append(Component.translatable("enchantment."+enchantment.getType().getName().getNamespace()+"."+enchantment.getType().getName().getKey()))
                    .append(Component.text(" "))
                    .append(Component.translatable("enchantment.level."+enchantment.getLevel()));
            hoverText = hoverText.appendNewline().append(line);
        }
        if(item.getComponent(ComponentTypes.LORE).isPresent()) {
            for(var i: item.getComponent(ComponentTypes.LORE).get().getLines()){
                hoverText = hoverText.appendNewline().append(i.applyFallbackStyle(Style.style(NamedTextColor.DARK_PURPLE, TextDecoration.ITALIC)));
            }
        }

        return Component.empty()
                .append(Component.text("[").color(NamedTextColor.GRAY))
                .append(
                        nameComponent
                )
                .append(Component.text("]").color(NamedTextColor.GRAY))
                .hoverEvent(HoverEvent.showText(hoverText))
                .clickEvent(!receivers.isEmpty()?ClickEvent.runCommand("unichat item "+uuid.toString()):ClickEvent.callback(a->{}));
//                .hoverEvent(item.getComponents().);
    }
}
