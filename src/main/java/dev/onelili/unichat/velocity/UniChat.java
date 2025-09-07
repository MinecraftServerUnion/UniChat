package dev.onelili.unichat.velocity;

import com.github.retrooper.packetevents.PacketEvents;
import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyReloadEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.onelili.unichat.velocity.channel.Channel;
import dev.onelili.unichat.velocity.handler.EventListener;
import dev.onelili.unichat.velocity.handler.PacketEventListener;
import dev.onelili.unichat.velocity.message.MessageLoader;
import dev.onelili.unichat.velocity.module.PatternModule;
import dev.onelili.unichat.velocity.util.Config;
import io.github.retrooper.packetevents.velocity.factory.VelocityPacketEventsBuilder;
import lombok.Getter;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.UUID;

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
    public static UniChat instance;
    @Getter
    private static ProxyServer proxy;
    @Getter
    private static Logger logger;
    @Getter
    private static File dataDirectory;

    @Inject
    public UniChat(@Nonnull ProxyServer proxy, @Nonnull Logger logger, @Nonnull @DataDirectory Path dataDirectory) {
        instance = this;
        UniChat.proxy = proxy;
        UniChat.logger = logger;
        UniChat.dataDirectory = dataDirectory.toFile();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Subscribe
    public void onProxyInitialization(@Nonnull ProxyInitializeEvent event) {
        proxy.getEventManager().register(this, new EventListener());
        Config.init();
        MessageLoader.initialize();
        PacketEvents.setAPI(VelocityPacketEventsBuilder.build(proxy, proxy.getPluginManager().fromInstance(this).orElseThrow(), logger, dataDirectory.toPath()));
        PacketEvents.getAPI().getSettings().checkForUpdates(false);
        PacketEvents.getAPI().load();
        PacketEvents.getAPI().getEventManager().registerListener(new PacketEventListener());
        PacketEvents.getAPI().init();

        PatternModule.registerDefaults();

        Channel.loadChannels();
    }

    @Subscribe
    public void onProxyReload(@Nonnull ProxyReloadEvent event) {
        Config.reload();
        for(Channel channel : Channel.getChannels().values()){
            if(channel.getHandler() != null)
                channel.getHandler().destroy();
        }
        Channel.getChannels().clear();
        Channel.loadChannels();
        for(UUID uuid : new HashSet<>(Channel.getPlayerChannels().keySet())){
            Channel.getPlayerChannels().put(uuid, Channel.defaultChannel);
        }
        logger.info("UniChat reloaded!");
    }

    @Subscribe
    public void onProxyShutdown(@Nonnull ProxyShutdownEvent event) {
        PacketEvents.getAPI().terminate();
    }
}
