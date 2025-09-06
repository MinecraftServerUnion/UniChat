package dev.onelili.unichat.velocity.util;

import com.velocitypowered.api.proxy.Player;
import dev.onelili.unichat.velocity.message.Message;
import net.kyori.adventure.bossbar.BossBar;

import javax.annotation.Nonnull;
import java.util.UUID;

public class SimplePlayer {
    public final Player player;
    public SimplePlayer(@Nonnull Player player){
        this.player = player;
    }

    public String getName() {
        return player.getUsername();
    }

    public UUID getPlayerUUID() {
        return player.getUniqueId();
    }

    public void sendMessage(Message message) {
        player.sendMessage(message.toComponent());
    }

    public void finishLogin() {
        throw new UnsupportedOperationException();
    }

    public void kick(Message reason) {
        player.disconnect(reason.toComponent());
    }

    public void sendActionBar(Message message) {
        player.sendActionBar(message.toComponent());
    }

    public void showBossbar(BossBar bossBar) {
        player.showBossBar(bossBar);
    }

    public void hideBossbar(BossBar bossBar) {
        player.hideBossBar(bossBar);
    }

    public boolean hasPermission(String permission) {
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
