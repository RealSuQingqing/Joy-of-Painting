package com.leclowndu93150.joyofpainting.network.packets;

import com.leclowndu93150.joyofpainting.client.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ExportPaintingPacket {
    public final String canvasId;

    public ExportPaintingPacket(String canvasId) {
        this.canvasId = canvasId;
    }

    public ExportPaintingPacket(FriendlyByteBuf buf) {
        this.canvasId = buf.readUtf(64);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(canvasId);
    }

    public static void handle(ExportPaintingPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handleExport(msg));
    }
}
