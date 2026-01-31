package dev.onelili.unichat.velocity.module;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.advancements.*;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.sound.SoundCategory;
import com.github.retrooper.packetevents.protocol.sound.StaticSound;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientAdvancementTab;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSoundEffect;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAdvancements;
import com.google.common.util.concurrent.AtomicDouble;
import com.velocitypowered.api.proxy.Player;
import dev.onelili.unichat.velocity.UniChat;
import dev.onelili.unichat.velocity.util.PlayerData;
import dev.onelili.unichat.velocity.message.Message;
import dev.onelili.unichat.velocity.util.Config;
import dev.onelili.unichat.velocity.util.SimplePlayer;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.*;

public class MentionModule {
    public static @Nonnull Component mention(@Nonnull SimplePlayer player, @Nonnull Player sender) {
        try{
            BossBar bossBar = BossBar.bossBar(Message.getMessage("mention.bossbar-title").add("sender", sender.getUsername()).toComponent(), 1f, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS);
            bossBar.addViewer(player.getPlayer());
            AtomicDouble progress = new AtomicDouble(1.0d);
            UniChat.getProxy().getScheduler().buildTask(UniChat.getInstance(), task -> {
                if (progress.addAndGet(-Config.getDouble("module.mention.bossbar-progress-decrease")) <= 0) {
                    bossBar.removeViewer(player.getPlayer());
                    task.cancel();
                    return;
                }
                bossBar.progress(progress.floatValue());
            }).repeat(Duration.ofMillis(50)).schedule();
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, new WrapperPlayServerSoundEffect(
                    new StaticSound(new ResourceLocation(Config.getString("module.mention.sound-effect-id")), null),
                    SoundCategory.MASTER,
                    Objects.requireNonNull(PlayerData.getPlayerData(player.getPlayer())).toPacketPosition(),
                    (float) Config.getDouble("module.mention.sound-effect-volume"),
                    (float) Config.getDouble("module.mention.sound-effect-pitch")
            ));

            AdvancementDisplay advancementDisplay = new AdvancementDisplay(
                    Message.getMessage("mention.advancement-title").add("sender", sender.getUsername()).toComponent(),
                    Component.text(""),
                    ItemStack.builder().type(ItemTypes.END_CRYSTAL).build(),
                    AdvancementType.TASK,
                    null,
                    true,
                    false,
                    0,
                    0
            );
            ResourceLocation key = new ResourceLocation("unichat", "module/mentioned");
            List<AdvancementHolder> advancementHolders = Collections.singletonList(
                    new AdvancementHolder(
                            key,
                            new Advancement(
                                    null,
                                    advancementDisplay,
                                    List.of(List.of("tick")),
                                    true
                            )
                    ));
            Map<ResourceLocation, AdvancementProgress> advancementProgressMap = Map.of(
                    key,
                    new AdvancementProgress(Map.of("tick",
                            new AdvancementProgress.CriterionProgress(1L))));
            WrapperPlayServerUpdateAdvancements packet = new WrapperPlayServerUpdateAdvancements(
                    false,
                    advancementHolders,
                    Set.of(),
                    advancementProgressMap,
                    true
            );
            WrapperPlayServerUpdateAdvancements removePacket = new WrapperPlayServerUpdateAdvancements(
                    false,
                    List.of(),
                    Set.of(key),
                    Map.of(),
                    true
            );

            PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, removePacket);
        }catch(Exception e){
            e.printStackTrace();
        }
        return Component.text("@" + player.getName()).color(NamedTextColor.YELLOW);
    }
}
