package dev.onelili.unichat.velocity.handler;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.ProtocolPacketEvent;
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage_v1_16;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientCloseWindow;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientHeldItemChange;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerPosition;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import com.velocitypowered.api.proxy.Player;
import dev.onelili.unichat.velocity.UniChat;
import dev.onelili.unichat.velocity.channel.Channel;
import dev.onelili.unichat.velocity.gui.GUIContainer;
import dev.onelili.unichat.velocity.util.Config;
import dev.onelili.unichat.velocity.util.ItemUtils;
import dev.onelili.unichat.velocity.util.PlayerData;
import dev.onelili.unichat.velocity.util.ShitMountainException;
import lombok.SneakyThrows;
import net.kyori.adventure.text.minimessage.MiniMessage;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class PacketEventListener extends SimplePacketListenerAbstract {
    public PacketEventListener() {
        super(PacketListenerPriority.NORMAL);
    }

    @Override
    public void onPacketPlaySend(@Nonnull PacketPlaySendEvent event) {
        switch(event.getPacketType()) {
            case CHAT_MESSAGE -> {
                WrapperPlayServerChatMessage packet = new WrapperPlayServerChatMessage(event);
                try {
                    ChatMessage_v1_16 message = (ChatMessage_v1_16) packet.getMessage();
                    Optional<Player> senderOpt = UniChat.getProxy().getPlayer(message.getSenderUUID());
                    if (senderOpt.isPresent()) {
                        Player sender = senderOpt.get();
                        if(sender.getCurrentServer().isPresent()){
                            String serverid = sender.getCurrentServer().get().getServerInfo().getName();
                            Channel channel = Channel.getPlayerChannel(sender);
                            String chatMessage = MiniMessage.miniMessage().serialize(message.getChatContent());
                            if(channel == null || !channel.isPassthrough() || Config.getConfigTree().getStringList("unhandled-servers").contains(serverid)) return;
                            if(channel.getChannelConfig().getBoolean("respect-backend", true)&&sender.equals(event.getPlayer())){
                                UniChat.getProxy().getScheduler()
                                        .buildTask(UniChat.getInstance(),
                                                () -> Channel.handleChat(event.getPlayer(), channel, chatMessage))
                                        .schedule();
                            }
                            event.setCancelled(true);
                        }
                    }
                } catch (ClassCastException e){
                    UniChat.getLogger().debug("Failed to cast message in chat packet: {}", e.getMessage());
                }
            }
            case PLAYER_POSITION_AND_LOOK -> {
                WrapperPlayServerPlayerPositionAndLook packet = new WrapperPlayServerPlayerPositionAndLook(event);
                Player player = event.getPlayer();
                Objects.requireNonNull(PlayerData.getPlayerDataMap().computeIfAbsent(player.getUniqueId(), uuid -> new PlayerData()))
                        .setPosition(packet.getPosition());
            }
            case HELD_ITEM_CHANGE -> {
                WrapperPlayServerHeldItemChange packet = new WrapperPlayServerHeldItemChange(event);
                Player player = event.getPlayer();
                Objects.requireNonNull(PlayerData.getPlayerDataMap().computeIfAbsent(player.getUniqueId(), uuid -> new PlayerData()))
                        .setHandItem(packet.getSlot());
            }
//            case SET_SLOT -> {
//                listenTo(event, 1);
//                WrapperPlayServerSetSlot packet = new WrapperPlayServerSetSlot(event);
//                Player player = event.getPlayer();
//                Objects.requireNonNull(PlayerData.getPlayerDataMap().computeIfAbsent(player.getUniqueId(), uuid -> new PlayerData()))
//                        .getInventory().put(packet.getSlot(), ItemUtils.fixItem(packet.getItem()));
//            }
//            case CLOSE_WINDOW -> {
//                WrapperPlayServerCloseWindow packet = new WrapperPlayServerCloseWindow(event);
//                try {
//                    GUIContainer.getGuis().remove(GUIContainer.getGuis().stream().filter(gui -> gui.getData().windowId() == packet.getWindowId()).findFirst().orElseThrow(() -> new ShitMountainException("null")));
//                } catch(Exception ignored) {}
//            }
        }
    }

    @Override
    public void onPacketPlayReceive(@Nonnull PacketPlayReceiveEvent event) {
        switch(event.getPacketType()) {
            case PLAYER_POSITION -> {
                WrapperPlayClientPlayerPosition packet = new WrapperPlayClientPlayerPosition(event);
                Player player = event.getPlayer();
                Objects.requireNonNull(PlayerData.getPlayerDataMap().computeIfAbsent(player.getUniqueId(), uuid -> new PlayerData()))
                        .setPosition(packet.getPosition());
            }
            case HELD_ITEM_CHANGE -> {
                WrapperPlayClientHeldItemChange packet = new WrapperPlayClientHeldItemChange(event);
                Player player = event.getPlayer();
                Objects.requireNonNull(PlayerData.getPlayerDataMap().computeIfAbsent(player.getUniqueId(), uuid -> new PlayerData()))
                        .setHandItem(packet.getSlot());
            }
//            case CLICK_WINDOW -> {
//                WrapperPlayClientClickWindow packet = new WrapperPlayClientClickWindow(event);
//                Player player = event.getPlayer();
//                GUIContainer[] gui = new GUIContainer[1];
//                GUIContainer.getGuis().stream().filter(obj -> obj.getData().windowId() == packet.getWindowId()).forEach(obj -> gui[0] = obj);
//                if(gui[0] != null) {
//                    List<ItemStack> items = new ArrayList<>();
//                    for(int i = 0; i <= gui[0].getData().slots() / 9 * 9; i++)
//                        if(gui[0].getData().items().get(i) != null)
//                            items.add(gui[0].getData().items().get(i));
//                        else
//                            items.add(ItemStack.EMPTY);
//                    WrapperPlayServerWindowItems wrapper1 = new WrapperPlayServerWindowItems(
//                            packet.getWindowId(),
//                            packet.getStateId().orElse(0),
//                            items,
//                            null
//                    );
//                    PacketEvents.getAPI().getPlayerManager().sendPacket(player, wrapper1);
//                     // TODO:更新物品栏
//                }
//            }
//            case CLOSE_WINDOW -> {
//                WrapperPlayClientCloseWindow packet = new WrapperPlayClientCloseWindow(event);
//                try {
//                    GUIContainer.getGuis().remove(GUIContainer.getGuis().stream().filter(gui -> gui.getData().windowId() == packet.getWindowId()).findFirst().orElseThrow(() -> new ShitMountainException("null")));
//                } catch(Exception ignored) {}
//            }
        }
    }

    @SneakyThrows
    private void printObject(Object obj, String prefix, int depth) {
        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (depth - 1 <= 0) {
                String cont = Objects.toString(field.get(obj));
                if (cont.length() > 50) {
                    cont = cont.substring(0, 30) + "..." + cont.substring(cont.length() - 20);
                }
                System.out.println(prefix+"- " + field.getName() + " : " + cont);
            } else {
                System.out.println(prefix+"- " + field.getName() + " :");
                printObject(field.get(obj), prefix + "  ", depth - 1);
            }
        }
    }

    private void listenTo(ProtocolPacketEvent event, int depth) { // to debug
        try {
            com.github.retrooper.packetevents.wrapper.PacketWrapper<?> packet = null;
            for(Constructor<?> i : event.getPacketType().getWrapperClass().getConstructors()){
                if(i.getParameterCount()==1 && i.getParameterTypes()[0].isAssignableFrom(event.getClass())){
                    packet = (com.github.retrooper.packetevents.wrapper.PacketWrapper<?>) i.newInstance(event);
                    break;
                }
            }
            System.out.println(event.getClass().getSimpleName() + "("+ ((Player)event.getPlayer()).getUsername() +"): "+event.getPacketType());
            if(packet != null && depth > 0) printObject(packet, "  ", depth);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
