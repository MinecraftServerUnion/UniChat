package dev.onelili.unichat.velocity.module;

import com.velocitypowered.api.proxy.Player;
import dev.onelili.unichat.velocity.UniChat;
import dev.onelili.unichat.velocity.channel.Channel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class PatternModule {
    public static final Map<String, PatternModule> modules = new ConcurrentHashMap<>();

    public abstract Component handle(Player sender);

    public static void registerDefaults() {
        modules.put("item", new ShowItemModule());
        modules.put("i", new ShowItemModule());
    }

    public static @Nonnull Component handleMessage(@Nonnull Player sender, @Nonnull String message) {
        StringBuilder current = new StringBuilder();
        Component result = Component.empty();
        outerFor: for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);
            if(c == '@'){
                if(Channel.getPlayerChannel(sender) != null) {
                    List<Player> players = new ArrayList<>(UniChat.getProxy().getAllPlayers());
                    players.sort(Comparator.comparingInt((Player p) -> p.getUsername().length()));
                    outer:
                    for (Player p : players) {
                        for (int j = 0; j < p.getUsername().length(); j++) {
                            if (i + j + 1 >= message.length() || message.charAt(i + j + 1) != p.getUsername().charAt(j)) {
                                continue outer;
                            }
                        }
                        result = result.append(Component.text(current.toString()));
                        result = result.append(MentionModule.mention(p, sender));
                        current = new StringBuilder();
                        i=i+p.getUsername().length();
                        continue outerFor;
                    }
                }
            } else if(c == '[') {
                result = result.append(Component.text(current.toString()));
                current = new StringBuilder();
                continue;
            } else if(c == ']') {
                String moduleName = current.toString();
                if(modules.containsKey(moduleName)){
                    result = result.append(modules.get(moduleName).handle(sender));
                }else{
                    result = result.append(Component.text("[" + moduleName + "]"));
                }
                current.append(c);
                current = new StringBuilder();
                continue;
            }
            current.append(c);
        }
        if(!current.isEmpty()){
            result = result.append(Component.text(current.toString()));
        }
        return result.color(NamedTextColor.WHITE);
    }
}
