package dev.onelili.unichat.velocity.handler;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.ProtocolPacketEvent;
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.ConnectionState;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage_v1_16;
import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.item.HashedStack;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.*;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import com.velocitypowered.api.proxy.Player;
import dev.onelili.unichat.velocity.UniChat;
import dev.onelili.unichat.velocity.channel.Channel;
import dev.onelili.unichat.velocity.gui.GUIContainer;
import dev.onelili.unichat.velocity.util.Config;
import dev.onelili.unichat.velocity.util.PlayerData;
import dev.onelili.unichat.velocity.util.ShitMountainException;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

import static com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Server.*;
import static com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Client.*;

public class PacketEventListener extends SimplePacketListenerAbstract {
    private static boolean enableDebugPacketLogging = false;
    private static int debugLoggingDepth = 1;
    public static List<PacketTypeCommon> filtered = List.of(
            ENTITY_MOVEMENT,
            ENTITY_RELATIVE_MOVE,
            PacketType.Play.Client.KEEP_ALIVE,
            PacketType.Play.Server.KEEP_ALIVE,
            PLAYER_POSITION,
            PLAYER_POSITION_AND_ROTATION,
            CLIENT_TICK_END,
            TIME_UPDATE,
            CHUNK_DATA,
            ENTITY_HEAD_LOOK,
            ENTITY_POSITION_SYNC,
            SPAWN_ENTITY,
            BLOCK_ACTION
    );
    public PacketEventListener() {
        super(PacketListenerPriority.NORMAL);
    }
    @Override
    public void onPacketPlaySend(@Nonnull PacketPlaySendEvent event) {
        if(event.getPlayer()==null||((Player) event.getPlayer()).getCurrentServer().isEmpty()||event.getConnectionState()== ConnectionState.LOGIN) return;
        if(!filtered.contains(event.getPacketType())&&enableDebugPacketLogging) listenTo(event, debugLoggingDepth);
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
            case SET_SLOT -> { // Not problematic
//                listenTo(event, 1);
                WrapperPlayServerSetSlot packet = new WrapperPlayServerSetSlot(event);
                Player player = event.getPlayer();
                var playerData = Objects.requireNonNull(PlayerData.getPlayerDataMap().computeIfAbsent(player.getUniqueId(), uuid -> new PlayerData()));
                int slot=packet.getSlot();
                int additionalSize=playerData.getInventoryPreSizes().get(packet.getWindowId());
                if(slot<additionalSize){
                    playerData.getTopInventory().put(slot, packet.getItem());
                }else {
                    playerData.getInventory().put(slot - additionalSize, packet.getItem());
                }
            }
            case WINDOW_ITEMS -> { // Not problematic
                WrapperPlayServerWindowItems packet = new WrapperPlayServerWindowItems(event);
                Player player = event.getPlayer();
                var playerData = Objects.requireNonNull(PlayerData.getPlayerDataMap().computeIfAbsent(player.getUniqueId(), uuid -> new PlayerData()));
//                System.out.println("WINDOW ITEMS: "+packet.getWindowId()+" sizeof "+packet.getItems().size());
                if(packet.getItems().size()>=36) {
                    for (int i = 0; i < 36; i++) {
                        playerData.getInventory().put(i, packet.getItems().get(i+(packet.getItems().size() - 36-(packet.getWindowId()==0?1:0))));
                    }
                    playerData.getTopInventory().clear();
                    for(int i=0;i<packet.getItems().size()-36-(packet.getWindowId()==0?1:0);i++) {
                        playerData.getTopInventory().put(i, packet.getItems().get(i));
                    }
                }
                if(packet.getWindowId()==0) {
                    playerData.getInventoryPreSizes().put(packet.getWindowId(), 9);
                }else {
                    playerData.getInventoryPreSizes().put(packet.getWindowId(), packet.getItems().size() - 36);
                }
//                System.out.println(packet.getWindowId()+": "+packet.getItems().size());
            }
            case CLOSE_WINDOW -> {
                WrapperPlayServerCloseWindow packet = new WrapperPlayServerCloseWindow(event);
                try {
                    GUIContainer.getGuis().remove(GUIContainer.getGuis().stream().filter(gui -> gui.getData().windowId() == packet.getWindowId()).findFirst().orElseThrow(() -> new ShitMountainException("null")));
                } catch(Exception ignored) {}
            }
        }
    }

    @Override
    public void onPacketPlayReceive(@Nonnull PacketPlayReceiveEvent event) {
        if(event.getPlayer()==null||((Player) event.getPlayer()).getCurrentServer().isEmpty()||event.getConnectionState()== ConnectionState.LOGIN) return;
        if(!filtered.contains(event.getPacketType())&&enableDebugPacketLogging) listenTo(event, debugLoggingDepth);
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
            case CREATIVE_INVENTORY_ACTION -> { // not problematic
                WrapperPlayClientCreativeInventoryAction packet = new WrapperPlayClientCreativeInventoryAction(event);
                Player player = event.getPlayer();
                var playerData = Objects.requireNonNull(PlayerData.getPlayerDataMap().computeIfAbsent(player.getUniqueId(), uuid -> new PlayerData()));
                int additionalSize=playerData.getInventoryPreSizes().get(0);
                playerData.getInventory().put(packet.getSlot()-additionalSize, packet.getItemStack());
            }
            case PLAYER_DIGGING -> { // not problematic
                WrapperPlayClientPlayerDigging packet = new WrapperPlayClientPlayerDigging(event);
                Player player = event.getPlayer();
                var playerData = Objects.requireNonNull(PlayerData.getPlayerDataMap().computeIfAbsent(player.getUniqueId(), uuid -> new PlayerData()));
                var mainhand = playerData.getInventory().get(playerData.getHandItem() + 27);
                if(mainhand == null) return;
                if(packet.getAction() == DiggingAction.DROP_ITEM){
                    if(mainhand.getAmount()-1<=0) playerData.getInventory().remove(playerData.getHandItem() + 27);
                    else mainhand.setAmount(mainhand.getAmount()-1);
                }else if(packet.getAction() == DiggingAction.DROP_ITEM_STACK){
                    playerData.getInventory().remove(playerData.getHandItem() + 27);
                }
            }
            case CLICK_WINDOW -> { // yes problematic
                listenTo(event, debugLoggingDepth);
                WrapperPlayClientClickWindow packet = new WrapperPlayClientClickWindow(event);
                Player player = event.getPlayer();
                var playerData = Objects.requireNonNull(PlayerData.getPlayerDataMap().computeIfAbsent(player.getUniqueId(), uuid -> new PlayerData()));
                GUIContainer[] gui = new GUIContainer[1];
                GUIContainer.getGuis().stream().filter(obj -> obj.getData().windowId() == packet.getWindowId()).forEach(obj -> gui[0] = obj);
                if(gui[0] != null) {
                    event.setCancelled(true);
                    List<ItemStack> items = new ArrayList<>();
                    int guiSize=gui[0].getData().slots() / 9 * 9;
                    for(int i = 0; i < guiSize; i++) {
                        if (gui[0].getData().items().get(i) != null)
                            items.add(gui[0].getData().items().get(i));
                        else
                            items.add(ItemStack.EMPTY);
                    }
//                    for(int i=0;i<36;i++){
//                        items.add(playerData.getInventory().getOrDefault(i, ItemStack.EMPTY));
//                    }
                    WrapperPlayServerWindowItems wrapper1 = new WrapperPlayServerWindowItems(
                            packet.getWindowId(),
                            packet.getStateId().orElse(0),
                            items,
                            null
                    );
                    PacketEvents.getAPI().getPlayerManager().sendPacket(player, wrapper1);

                    if(packet.getHashedSlots()!=null) {
                        packet.getHashedSlots().forEach((k, v)->{
                            if(k>=guiSize){
                                PacketEvents.getAPI().getPlayerManager().sendPacket(player, new WrapperPlayServerSetSlot(
                                        packet.getWindowId(),
                                        packet.getStateId().orElse(0),
                                        k,
                                        playerData.getInventory().getOrDefault(k-guiSize, ItemStack.EMPTY)
                                ));
                            }
                        });
                    }
                }else { // When dealing with actions here, items loses NBT
                    System.out.print(packet.getWindowId() + ", " + playerData.getInventoryPreSizes().get(packet.getWindowId()) + ": (");
                    packet.getHashedSlots().forEach((i, v) -> {
                                v.ifPresent(hashedStack -> System.out.print(i + ":" + MiniMessage.miniMessage().serialize(hashedStack.asItemStack().getComponentOr(ComponentTypes.CUSTOM_NAME, hashedStack.asItemStack().getComponentOr(ComponentTypes.ITEM_NAME, Component.text("null")))) + ", "));
                            }
                    );
                    System.out.println(")");

                    int additionalSize = playerData.getInventoryPreSizes().get(packet.getWindowId());

                    if (packet.getCarriedHashedStack().isPresent()) {
                        System.out.println(MiniMessage.miniMessage().serialize(packet.getCarriedHashedStack().get().asItemStack().getComponentOr(ComponentTypes.CUSTOM_NAME, packet.getCarriedHashedStack().get().asItemStack().getComponentOr(ComponentTypes.ITEM_NAME, Component.text("null")))));
                    }

                    // This is to fix the missing NBT in this packet
                    // In exactly one packet, the placed slots MUST come from the cursor, and the cursor MUST come from slot clicked
                    // todo: Except the QUICK_MOVE action, which must be dealt independently

                    ItemStack template;
                    if(packet.getSlot()<additionalSize) {
                        template = playerData.getTopInventory().getOrDefault(packet.getSlot(), ItemStack.EMPTY).copy();
                    }else{
                        template = playerData.getInventory().getOrDefault(packet.getSlot()-additionalSize, ItemStack.EMPTY).copy();
                    }

                    if(template!=ItemStack.EMPTY&&packet.getWindowClickType()== WrapperPlayClientClickWindow.WindowClickType.QUICK_MOVE){
                        playerData.setCursor(template.copy());
                    }

                    if (packet.getHashedSlots() != null) {
                        packet.getHashedSlots().forEach((key, value) -> {
                            ItemStack stack;
                            if(value.isPresent()){
                                assert playerData.getCursor()!=null;
                                stack = playerData.getCursor().copy();
                                stack.setAmount(value.get().getCount());
                            }else{
                                stack = ItemStack.EMPTY;
                            }
                            if (key < additionalSize) {
//                                System.out.println("Value " + key + " is outside of container");
                                playerData.getTopInventory().put(key, stack);
                            }else {
//                                System.out.println("Putting " + value.get().asItemStack() + " to " + (key - additionalSize));
                                playerData.getInventory().put(key - additionalSize, stack);
                            }
                        });
                    }

                    // Update cursor status
                    if(packet.getCarriedHashedStack().isPresent() &&
                            template!=ItemStack.EMPTY) { // Empty check to exclude when right clicking to partially deposit
                        var cloned = template.copy();
                        cloned.setAmount(packet.getCarriedHashedStack().get().getCount());
                        playerData.setCursor(cloned);
                    }else if(packet.getCarriedHashedStack().isPresent() &&template==ItemStack.EMPTY) {
                        var cloned = playerData.getCursor().copy();
                        cloned.setAmount(packet.getCarriedHashedStack().get().getCount());
                        playerData.setCursor(cloned);
                    }else if(packet.getCarriedHashedStack().isEmpty()){
                        playerData.setCursor(null);
                    }
                }
            }
            case CLOSE_WINDOW -> {
                WrapperPlayClientCloseWindow packet = new WrapperPlayClientCloseWindow(event);
                try {
                    if(packet.getWindowId()!=0) {
                        var playerData = PlayerData.getPlayerData(event.getPlayer());

                        playerData.getInventoryPreSizes().remove(packet.getWindowId());
                    }
                    GUIContainer.getGuis().remove(GUIContainer.getGuis().stream().filter(gui -> gui.getData().windowId() == packet.getWindowId()).findFirst().orElseThrow(() -> new ShitMountainException("null")));
                } catch(Exception ignored) {}
            }
        }
    }

    @SneakyThrows
    private void printObject(Object obj, String prefix, int depth) {
        for (Field field : obj.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                if (depth - 1 <= 0) {
                    String cont = Objects.toString(field.get(obj));
                    if (cont.length() > 50) {
                        cont = cont.substring(0, 30) + "..." + cont.substring(cont.length() - 20);
                    }
                    System.out.println(prefix + "- " + field.getName() + " : " + cont);
                } else {
                    System.out.println(prefix + "- " + field.getName() + " :");
                    printObject(field.get(obj), prefix + "  ", depth - 1);
                }
            }catch(Exception ignored){}
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
