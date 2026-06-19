package com.leclowndu93150.joyofpainting.network.packets;

import com.leclowndu93150.joyofpainting.JoyOfPainting;
import com.leclowndu93150.joyofpainting.entity.EntityEasel;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record EaselLeftPacket(int easelId) implements CustomPacketPayload {
    public static final Type<EaselLeftPacket> TYPE = new Type<>(JoyOfPainting.id("easel_left"));

    public static final StreamCodec<RegistryFriendlyByteBuf, EaselLeftPacket> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, EaselLeftPacket::easelId, EaselLeftPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(EaselLeftPacket msg, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer pl)) return;
        if (msg.easelId <= -1) return;
        Entity entity = pl.level().getEntity(msg.easelId);
        if (entity instanceof EntityEasel easel) easel.setPainter(null);
    }
}
