package com.leclowndu93150.joyofpainting.network.packets;

import com.leclowndu93150.joyofpainting.entity.EntityCanvas;
import com.leclowndu93150.joyofpainting.network.JoyOfPaintingNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class PictureRequestPacket {
    public final String canvasId;

    public PictureRequestPacket(String canvasId) {
        this.canvasId = canvasId;
    }

    public PictureRequestPacket(FriendlyByteBuf buf) {
        this.canvasId = buf.readUtf(64);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(canvasId);
    }

    public static void handle(PictureRequestPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        ServerPlayer pl = ctxSupplier.get().getSender();
        if (pl == null) return;
        EntityCanvas.Picture picture = EntityCanvas.PICTURES.get(msg.canvasId);
        if (picture != null && picture.pixels() != null) {
            JoyOfPaintingNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> pl),
                    new PictureSendPacket(msg.canvasId, picture.version(), picture.pixels(),
                            picture.sidesActive(),
                            picture.sidePixels() == null ? new int[0] : picture.sidePixels()));
        }
    }
}
