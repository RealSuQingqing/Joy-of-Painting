package com.leclowndu93150.joyofpainting.network.packets;

import com.leclowndu93150.joyofpainting.JoyOfPainting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;

public record OpenGuiPacket(int easelId, boolean allowed, boolean edit, InteractionHand hand) implements CustomPacketPayload {
    public static final Type<OpenGuiPacket> TYPE = new Type<>(JoyOfPainting.id("open_gui"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenGuiPacket> STREAM_CODEC = StreamCodec.of(
            (buf, msg) -> {
                buf.writeInt(msg.easelId);
                buf.writeBoolean(msg.allowed);
                buf.writeBoolean(msg.edit);
                buf.writeByte(msg.hand.ordinal());
            },
            buf -> {
                int easelId = buf.readInt();
                boolean allowed = buf.readBoolean();
                boolean edit = buf.readBoolean();
                int ord = buf.readByte();
                InteractionHand hand = ord < InteractionHand.values().length ? InteractionHand.values()[ord] : InteractionHand.MAIN_HAND;
                return new OpenGuiPacket(easelId, allowed, edit, hand);
            });

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
