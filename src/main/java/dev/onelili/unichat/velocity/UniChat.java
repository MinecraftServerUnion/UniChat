package dev.onelili.unichat.velocity;

import com.github.retrooper.packetevents.PacketEvents;
import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.onelili.unichat.velocity.listener.EventListener;
import dev.onelili.unichat.velocity.listener.PacketEventListener;
import io.github.retrooper.packetevents.velocity.factory.VelocityPacketEventsBuilder;
import lombok.Getter;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.io.File;
import java.nio.file.Path;

@Plugin(
        id = "unichat",
        name = "UniChat",
        version = "1.0.0",
        authors = {
                "oneLiLi",
                "jason31416"
        }
)
public class UniChat {
    @Getter
    private static UniChat instance;
    @Getter
    private final ProxyServer proxy;
    @Getter
    private final Logger logger;
    @Getter
    private final File dataDirectory;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Inject
    public UniChat(@Nonnull ProxyServer proxy, @Nonnull Logger logger, @Nonnull @DataDirectory Path dataDirectory) {
        instance = this;
        this.proxy = proxy;
        this.logger = logger;
        this.dataDirectory = dataDirectory.toFile();
        this.dataDirectory.mkdirs();
    }

    @Subscribe
    public void onProxyInitialization(@Nonnull ProxyInitializeEvent event) {
        proxy.getEventManager().register(this, new EventListener());
        PacketEvents.setAPI(VelocityPacketEventsBuilder.build(proxy, proxy.getPluginManager().fromInstance(this).orElseThrow(), logger, dataDirectory.toPath()));
        PacketEvents.getAPI().load();
        PacketEvents.getAPI().getEventManager().registerListener(new PacketEventListener());
        PacketEvents.getAPI().init();
    }

    @Subscribe
    public void onProxyShutdown(@Nonnull ProxyShutdownEvent event) {
        PacketEvents.getAPI().terminate();
    }
}
