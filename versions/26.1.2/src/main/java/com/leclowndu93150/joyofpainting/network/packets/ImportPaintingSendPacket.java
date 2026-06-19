package com.leclowndu93150.joyofpainting.network.packets;

import com.leclowndu93150.joyofpainting.JoyOfPainting;
import com.leclowndu93150.joyofpainting.command.CommandImport;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ImportPaintingSendPacket(CompoundTag tag) implements CustomPacketPayload {
    public static final Type<ImportPaintingSendPacket> TYPE = new Type<>(JoyOfPainting.id("import_painting_send"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ImportPaintingSendPacket> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.COMPOUND_TAG, ImportPaintingSendPacket::tag, ImportPaintingSendPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ImportPaintingSendPacket msg, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer pl)) return;
        if (msg.tag == null) return;
        CommandImport.doImport(msg.tag, pl);
    }
}
