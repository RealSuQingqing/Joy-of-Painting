package com.leclowndu93150.joyofpainting.network.packets;

import com.leclowndu93150.joyofpainting.CanvasSides;
import com.leclowndu93150.joyofpainting.CanvasType;
import com.leclowndu93150.joyofpainting.entity.EntityEasel;
import com.leclowndu93150.joyofpainting.item.ItemCanvas;
import com.leclowndu93150.joyofpainting.item.ItemPalette;
import com.leclowndu93150.joyofpainting.nbt.NbtCanvas;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CanvasMiniUpdatePacket {
    public final int[] pixels;
    public final String canvasId;
    public final int version;
    public final int easelId;
    public final CanvasType canvasType;
    public final boolean sidesActive;
    public final int[] sidePixels;

    public CanvasMiniUpdatePacket(int[] pixels, String canvasId, int version, int easelId, CanvasType canvasType,
                                   boolean sidesActive, int[] sidePixels) {
        this.pixels = pixels;
        this.canvasId = canvasId;
        this.version = version;
        this.easelId = easelId;
        this.canvasType = canvasType;
        this.sidesActive = sidesActive;
        this.sidePixels = sidePixels;
    }

    public CanvasMiniUpdatePacket(FriendlyByteBuf buf) {
        this.easelId = buf.readInt();
        CanvasType ct = CanvasType.fromByte(buf.readByte());
        this.canvasType = ct == null ? CanvasType.SMALL : ct;
        this.version = buf.readInt();
        this.canvasId = buf.readUtf(64);
        int area = CanvasType.getHeight(canvasType) * CanvasType.getWidth(canvasType);
        this.pixels = buf.readVarIntArray(area);
        this.sidesActive = buf.readBoolean();
        this.sidePixels = buf.readVarIntArray(CanvasSides.count(canvasType));
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(easelId);
        buf.writeByte(canvasType.ordinal());
        buf.writeInt(version);
        buf.writeUtf(canvasId);
        buf.writeVarIntArray(pixels);
        buf.writeBoolean(sidesActive);
        buf.writeVarIntArray(sidePixels == null ? new int[0] : sidePixels);
    }

    public static void handle(CanvasMiniUpdatePacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        ServerPlayer pl = ctxSupplier.get().getSender();
        if (pl == null) return;

        ItemStack canvas;
        Entity entityEasel = null;

        if (msg.easelId > -1) {
            entityEasel = pl.level().getEntity(msg.easelId);
            if (!(entityEasel instanceof EntityEasel easel)) return;
            canvas = easel.getItem();
            if (!(canvas.getItem() instanceof ItemCanvas)) return;
        } else {
            canvas = pl.getMainHandItem();
            ItemStack palette = pl.getOffhandItem();
            if (canvas.getItem() instanceof ItemPalette) canvas = palette;
        }

        if (canvas.isEmpty() || !(canvas.getItem() instanceof ItemCanvas)) return;
        NbtCanvas.setPixels(canvas, msg.pixels);
        NbtCanvas.setName(canvas, msg.canvasId);
        NbtCanvas.setVersion(canvas, msg.version);
        NbtCanvas.setGeneration(canvas, 0);
        NbtCanvas.setSidesActive(canvas, msg.sidesActive);
        if (msg.sidePixels != null && msg.sidePixels.length > 0) {
            NbtCanvas.setSidePixels(canvas, msg.sidePixels);
        }

        if (entityEasel instanceof EntityEasel easel) {
            easel.setItem(canvas, false);
        }
    }
}
