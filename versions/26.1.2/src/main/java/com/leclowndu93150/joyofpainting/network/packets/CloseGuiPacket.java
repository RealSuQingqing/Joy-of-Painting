package com.leclowndu93150.joyofpainting.network.packets;

import com.leclowndu93150.joyofpainting.JoyOfPainting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record CloseGuiPacket() implements CustomPacketPayload {
    public static final Type<CloseGuiPacket> TYPE = new Type<>(JoyOfPainting.id("close_gui"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CloseGuiPacket> STREAM_CODEC = StreamCodec.unit(new CloseGuiPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
