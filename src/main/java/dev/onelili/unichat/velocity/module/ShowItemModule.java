package dev.onelili.unichat.velocity.module;

import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemLore;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.velocitypowered.api.proxy.Player;
import dev.onelili.unichat.velocity.gui.GUISession;
import dev.onelili.unichat.velocity.handler.PlayerData;
import dev.onelili.unichat.velocity.message.Message;
import dev.onelili.unichat.velocity.util.ItemUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

import javax.annotation.Nonnull;
import java.util.List;

public class ShowItemModule extends PatternModule {
    @Override
    public @Nonnull Component handle(@Nonnull Player sender, boolean doProcess) {
        PlayerData data = PlayerData.getPlayerData(sender);
        if (data == null || data.getHandItem() < 0)
            return new Message("&7[&fNone&7]").toComponent();
        ItemStack item = data.getInventory().get(data.getHandItem() + 36);
        if (item == null)
            return new Message("&7[&fNone&7]").toComponent();

        sender.sendMessage(item.getComponentOr(ComponentTypes.CUSTOM_NAME, Component.text("No custom name")));
        item.getComponentOr(ComponentTypes.LORE, new ItemLore(List.of())).getLines().forEach(sender::sendMessage);



        return Component.empty()
                .append(Component.text("[").color(NamedTextColor.GRAY))
                .append(
                        Component.text("test")
                                .hoverEvent(HoverEvent.showText(Component.text("点击查看详情")))
                )
                .append(Component.text("]").color(NamedTextColor.GRAY));
    }
}
