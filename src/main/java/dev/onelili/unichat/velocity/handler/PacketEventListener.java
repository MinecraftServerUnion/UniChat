package dev.onelili.unichat.velocity.handler;

import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.ProtocolPacketEvent;
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage_v1_16;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientHeldItemChange;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerPosition;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import com.velocitypowered.api.proxy.Player;
import dev.onelili.unichat.velocity.UniChat;
import dev.onelili.unichat.velocity.channel.Channel;
import dev.onelili.unichat.velocity.message.Message;
import dev.onelili.unichat.velocity.module.PatternModule;
import dev.onelili.unichat.velocity.util.Config;
import dev.onelili.unichat.velocity.util.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
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
                            if(channel == null || !channel.isPassthrough() || Config.getConfigTree().getStringList("unhandled-servers").contains(serverid)) return;
                            event.setCancelled(true);
                        }
                    }
                } catch (ClassCastException e){
                    UniChat.getLogger().debug("Failed to cast message in chat packet: {}", e.getMessage());
                }
            }
            case PLAYER_POSITION_AND_LOOK -> {
                WrapperPlayServerPlayerPositionAndLook wrapper = new WrapperPlayServerPlayerPositionAndLook(event);
                Player player = event.getPlayer();
                Objects.requireNonNull(PlayerData.getPlayerDataMap().computeIfAbsent(player.getUniqueId(), uuid -> new PlayerData()))
                        .setPosition(wrapper.getPosition());
            }
            case HELD_ITEM_CHANGE -> {
                WrapperPlayServerHeldItemChange wrapper = new WrapperPlayServerHeldItemChange(event);
                Player player = event.getPlayer();
                Objects.requireNonNull(PlayerData.getPlayerDataMap().computeIfAbsent(player.getUniqueId(), uuid -> new PlayerData()))
                        .setHandItem(wrapper.getSlot());
            }
            case SET_SLOT -> {
                WrapperPlayServerSetSlot wrapper = new WrapperPlayServerSetSlot(event);
                Player player = event.getPlayer();
                Objects.requireNonNull(PlayerData.getPlayerDataMap().computeIfAbsent(player.getUniqueId(), uuid -> new PlayerData()))
                        .getInventory().put(wrapper.getSlot(), wrapper.getItem());
            }
        }
    }

    @Override
    public void onPacketPlayReceive(@Nonnull PacketPlayReceiveEvent event) {
        switch(event.getPacketType()) {
            case PLAYER_POSITION -> {
                WrapperPlayClientPlayerPosition wrapper = new WrapperPlayClientPlayerPosition(event);
                Player player = event.getPlayer();
                Objects.requireNonNull(PlayerData.getPlayerDataMap().computeIfAbsent(player.getUniqueId(), uuid -> new PlayerData()))
                        .setPosition(wrapper.getPosition());
            }
            case HELD_ITEM_CHANGE -> {
                WrapperPlayClientHeldItemChange wrapper = new WrapperPlayClientHeldItemChange(event);
                Player player = event.getPlayer();
                Objects.requireNonNull(PlayerData.getPlayerDataMap().computeIfAbsent(player.getUniqueId(), uuid -> new PlayerData()))
                        .setHandItem(wrapper.getSlot());
            }
            case CHAT_MESSAGE -> {
                listenTo(event);
            }
        }
    }

    private void listenTo(ProtocolPacketEvent event) { // to debug
        try {
            com.github.retrooper.packetevents.wrapper.PacketWrapper<?> packet = null;
            for(Constructor<?> i : event.getPacketType().getWrapperClass().getConstructors()){
                if(i.getParameterCount()==1 && i.getParameterTypes()[0].isAssignableFrom(event.getClass())){
                    packet = (com.github.retrooper.packetevents.wrapper.PacketWrapper<?>) i.newInstance(event);
                    break;
                }
            }
            System.out.println(event.getClass().getSimpleName() + "("+ ((Player)event.getPlayer()).getUsername() +"): "+event.getPacketType());
            if(packet != null)
                for(Field field : packet.getClass().getDeclaredFields()){
                    field.setAccessible(true);
                    String cont = Objects.toString(field.get(packet));
                    if(cont.length() > 50){
                        cont = cont.substring(0, 30) + "..." + cont.substring(cont.length() - 20);
                    }
                    System.out.println("- " + field.getName() + " : " + cont);
                }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
