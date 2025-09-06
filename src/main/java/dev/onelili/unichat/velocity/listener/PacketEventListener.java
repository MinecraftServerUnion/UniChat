package dev.onelili.unichat.velocity.listener;

import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;

import javax.annotation.Nonnull;

public class PacketEventListener extends SimplePacketListenerAbstract {
    public PacketEventListener() {
        super(PacketListenerPriority.NORMAL);
    }

    @Override
    public void onPacketPlaySend(@Nonnull PacketPlaySendEvent event) {
        switch(event.getPacketType()) {}
    }

    @Override
    public void onPacketPlayReceive(@Nonnull PacketPlayReceiveEvent event) {
        switch(event.getPacketType()) {}
    }
}
