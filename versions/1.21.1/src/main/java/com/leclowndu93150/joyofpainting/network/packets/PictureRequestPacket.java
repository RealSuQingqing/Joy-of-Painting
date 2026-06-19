package com.leclowndu93150.joyofpainting.network.packets;

import com.leclowndu93150.joyofpainting.JoyOfPainting;
import com.leclowndu93150.joyofpainting.entity.EntityCanvas;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PictureRequestPacket(String canvasId) implements CustomPacketPayload {
    public static final Type<PictureRequestPacket> TYPE = new Type<>(JoyOfPainting.id("picture_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PictureRequestPacket> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.STRING_UTF8, PictureRequestPacket::canvasId, PictureRequestPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PictureRequestPacket msg, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer pl)) return;
        EntityCanvas.Picture picture = EntityCanvas.PICTURES.get(msg.canvasId);
        if (picture != null && picture.pixels() != null) {
            PacketDistributor.sendToPlayer(pl, new PictureSendPacket(
                    msg.canvasId, picture.version(), picture.pixels(),
                    picture.sidesActive(),
                    picture.sidePixels() == null ? new int[0] : picture.sidePixels()));
        }
    }
}
