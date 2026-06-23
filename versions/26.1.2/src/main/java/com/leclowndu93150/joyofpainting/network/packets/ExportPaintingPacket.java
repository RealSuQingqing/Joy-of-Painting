package com.leclowndu93150.joyofpainting.network.packets;

import com.leclowndu93150.joyofpainting.JoyOfPainting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ExportPaintingPacket(String canvasId) implements CustomPacketPayload {
    public static final Type<ExportPaintingPacket> TYPE = new Type<>(JoyOfPainting.id("export_painting"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ExportPaintingPacket> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.STRING_UTF8, ExportPaintingPacket::canvasId, ExportPaintingPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
