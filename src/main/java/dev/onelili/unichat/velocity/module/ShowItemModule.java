package dev.onelili.unichat.velocity.module;

import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ShowItemModule extends PatternModule{
    @Override
    public Component handle(Player sender) {
        Component component = Component.empty()
                .append(Component.text("[").color(NamedTextColor.GRAY))
                .append(Component.text("Item").color(NamedTextColor.GOLD))
                .append(Component.text("]").color(NamedTextColor.GRAY));
        return component;
    }
}
