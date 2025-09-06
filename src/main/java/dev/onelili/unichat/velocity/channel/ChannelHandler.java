package dev.onelili.unichat.velocity.channel;

import com.velocitypowered.api.proxy.Player;
import dev.onelili.unichat.velocity.util.SimplePlayer;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

public interface ChannelHandler {
    void handle(@Nonnull SimplePlayer player, @Nonnull String message);

    List<Player> recipients(@Nonnull SimplePlayer player);

    default void destroy() {}
}
