package com.leclowndu93150.joyofpainting.network.packets;

import com.leclowndu93150.joyofpainting.JoyOfPainting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ImportPaintingPacket(String canvasId) implements CustomPacketPayload {
    public static final Type<ImportPaintingPacket> TYPE = new Type<>(JoyOfPainting.id("import_painting"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ImportPaintingPacket> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.STRING_UTF8, ImportPaintingPacket::canvasId, ImportPaintingPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
