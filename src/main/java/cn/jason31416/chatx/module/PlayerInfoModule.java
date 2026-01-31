package cn.jason31416.chatx.module;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.ServerInfo;
import cn.jason31416.chatx.message.Message;
import cn.jason31416.chatx.message.MessageLoader;
import cn.jason31416.chatx.util.SimplePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;

public class PlayerInfoModule extends PatternModule{
    @Override
    public Component handle(Player sender, List<SimplePlayer> receivers) {
        Component ret=Component.empty(), hover=Component.empty();
        ret = ret.append(Component.text("["+sender.getUsername()+"]").color(NamedTextColor.WHITE));
        for(String i: MessageLoader.getMessageConfig().getStringList("player-info")){
            hover = hover.append(new Message(i)
                    .add("ping", sender.getPing())
                    .add("server", sender.getCurrentServer().map(ServerConnection::getServerInfo).map(ServerInfo::getName).orElse("Unknown"))
                    .add("uuid", sender.getUniqueId().toString())
                    .toComponent()).appendNewline();
        }
        return ret.hoverEvent(HoverEvent.showText(hover));
    }
}
