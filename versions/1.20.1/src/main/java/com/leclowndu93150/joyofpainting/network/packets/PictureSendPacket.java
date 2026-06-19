package com.leclowndu93150.joyofpainting.network.packets;

import com.leclowndu93150.joyofpainting.entity.EntityCanvas;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PictureSendPacket {
    public final String canvasId;
    public final int version;
    public final int[] pixels;
    public final boolean sidesActive;
    public final int[] sidePixels;

    public PictureSendPacket(String canvasId, int version, int[] pixels, boolean sidesActive, int[] sidePixels) {
        this.canvasId = canvasId;
        this.version = version;
        this.pixels = pixels;
        this.sidesActive = sidesActive;
        this.sidePixels = sidePixels;
    }

    public PictureSendPacket(FriendlyByteBuf buf) {
        this.canvasId = buf.readUtf(64);
        this.version = buf.readInt();
        this.pixels = buf.readVarIntArray(1024);
        this.sidesActive = buf.readBoolean();
        this.sidePixels = buf.readVarIntArray(128);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(canvasId);
        buf.writeInt(version);
        buf.writeVarIntArray(pixels);
        buf.writeBoolean(sidesActive);
        buf.writeVarIntArray(sidePixels == null ? new int[0] : sidePixels);
    }

    public static void handle(PictureSendPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient(msg));
    }

    private static void handleClient(PictureSendPacket msg) {
        EntityCanvas.PICTURES.put(msg.canvasId, new EntityCanvas.Picture(msg.version, msg.pixels, msg.sidesActive, msg.sidePixels == null ? new int[0] : msg.sidePixels));
        EntityCanvas.PICTURE_REQUESTS.remove(msg.canvasId);
    }
}
