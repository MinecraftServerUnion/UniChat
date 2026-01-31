package dev.onelili.unichat.velocity.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import dev.onelili.unichat.velocity.UniChat;
import dev.onelili.unichat.velocity.gui.GUIContainer;
import dev.onelili.unichat.velocity.gui.GUIData;
import dev.onelili.unichat.velocity.message.Message;
import dev.onelili.unichat.velocity.module.ShowItemModule;
import dev.onelili.unichat.velocity.util.Logger;

import java.util.List;
import java.util.UUID;

public class UniChatCommand implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        if(invocation.arguments().length == 0 || invocation.arguments()[0].equals("version")){
            invocation.source().sendMessage(new Message("<#47BFFB>UniChat v"+ UniChat.getProxy().getPluginManager().getPlugin("unichat").get().getDescription().getVersion().get()
                + " by Jason31416 & onelili").toComponent());
            return;
        }
        switch (invocation.arguments()[0]){
            case "reload" -> {
                if(invocation.source().hasPermission("unichat.admin")){
                    UniChat.reload();
                    invocation.source().sendMessage(new Message("<#47BFFB>UniChat has been reloaded.").toComponent());
                }
            }
            case "item" -> {
                if(invocation.arguments().length == 2) {
                    UUID uuid = UUID.fromString(invocation.arguments()[1]);
                    if(ShowItemModule.getGuiMap().containsKey(uuid) && invocation.source() instanceof Player player) {
                        GUIData data = ShowItemModule.getGuiMap().get(uuid);
                        GUIContainer container = GUIContainer.of(data);
                        container.open(player);
                    }
                }
            }
            default -> {
                invocation.source().sendMessage(new Message("&cUnknown unichat command.").toComponent());
            }
        }
    }
    @Override
    public List<String> suggest(Invocation invocation) {
        if(invocation.arguments().length <= 1){
            return List.of("version", "reload");
        }
        return List.of();
    }
}
