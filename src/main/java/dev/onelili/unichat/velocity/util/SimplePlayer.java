package dev.onelili.unichat.velocity.util;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import dev.onelili.unichat.velocity.message.Message;
import dev.onelili.unichat.velocity.message.MessageLoader;
import lombok.Getter;
import net.kyori.adventure.bossbar.BossBar;

import javax.annotation.Nonnull;
import java.util.UUID;

public class SimplePlayer {
    public static SimplePlayer console;
    static {
        console = new SimplePlayer(null){
            @Override
            public String getName() {
                return MessageLoader.getMessage("chat.console-name", "CONSOLE").toString();
            }
            @Override
            public UUID getPlayerUUID() {
                return null;
            }
            @Override
            public boolean hasPermission(@Nonnull String permission) {
                return true;
            }
        };
    }

    @Getter
    private final Player player;

    public ServerConnection getCurrentServer() {
        return player.getCurrentServer().orElse(null);
    }

    public SimplePlayer(Player player){
        this.player = player;
    }

    public String getName() {
        return player.getUsername();
    }

    public UUID getPlayerUUID() {
        return player.getUniqueId();
    }

    public void sendMessage(@Nonnull Message message) {
        player.sendMessage(message.toComponent());
    }

    public void finishLogin() {
        throw new UnsupportedOperationException();
    }

    public void kick(@Nonnull Message reason) {
        player.disconnect(reason.toComponent());
    }

    public void sendActionBar(@Nonnull Message message) {
        player.sendActionBar(message.toComponent());
    }

    public void showBossbar(@Nonnull BossBar bossBar) {
        player.showBossBar(bossBar);
    }

    public void hideBossbar(@Nonnull BossBar bossBar) {
        player.hideBossBar(bossBar);
    }

    public boolean hasPermission(@Nonnull String permission) {
        return player.hasPermission(permission);
    }

    public boolean isOp() {
        return player.hasPermission("unichat.admin");
    }

    public boolean equals(Object obj) {
        if(obj instanceof SimplePlayer pl) return pl.getPlayerUUID().equals(this.getPlayerUUID());
        return false;
    }
    public int hashCode(){
        return this.getPlayerUUID().hashCode();
    }
}
