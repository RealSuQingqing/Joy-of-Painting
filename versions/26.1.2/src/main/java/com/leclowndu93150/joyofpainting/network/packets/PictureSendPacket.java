package com.leclowndu93150.joyofpainting.network.packets;

import com.leclowndu93150.joyofpainting.JoyOfPainting;
import com.leclowndu93150.joyofpainting.entity.EntityCanvas;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PictureSendPacket(String canvasId, int version, int[] pixels, boolean sidesActive, int[] sidePixels) implements CustomPacketPayload {
    public static final Type<PictureSendPacket> TYPE = new Type<>(JoyOfPainting.id("picture_send"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PictureSendPacket> STREAM_CODEC = StreamCodec.of(
            (buf, msg) -> {
                buf.writeUtf(msg.canvasId);
                buf.writeInt(msg.version);
                buf.writeVarIntArray(msg.pixels);
                buf.writeBoolean(msg.sidesActive);
                buf.writeVarIntArray(msg.sidePixels == null ? new int[0] : msg.sidePixels);
            },
            buf -> new PictureSendPacket(buf.readUtf(64), buf.readInt(), buf.readVarIntArray(1024), buf.readBoolean(), buf.readVarIntArray(128)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PictureSendPacket msg, IPayloadContext ctx) {
        EntityCanvas.PICTURES.put(msg.canvasId, new EntityCanvas.Picture(msg.version, msg.pixels, msg.sidesActive, msg.sidePixels == null ? new int[0] : msg.sidePixels));
        EntityCanvas.PICTURE_REQUESTS.remove(msg.canvasId);
    }
}
