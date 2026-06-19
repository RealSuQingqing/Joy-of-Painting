package com.leclowndu93150.joyofpainting.network.packets;

import com.leclowndu93150.joyofpainting.CanvasSides;
import com.leclowndu93150.joyofpainting.CanvasType;
import com.leclowndu93150.joyofpainting.PaletteUtil;
import com.leclowndu93150.joyofpainting.entity.EntityEasel;
import com.leclowndu93150.joyofpainting.item.ItemCanvas;
import com.leclowndu93150.joyofpainting.item.ItemPalette;
import com.leclowndu93150.joyofpainting.nbt.NbtCanvas;
import com.leclowndu93150.joyofpainting.nbt.NbtPalette;
import com.leclowndu93150.joyofpainting.registry.ModItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CanvasUpdatePacket {
    public final int[] pixels;
    public final boolean signed;
    public final String title;
    public final String canvasId;
    public final int version;
    public final int easelId;
    public final PaletteUtil.CustomColor[] paletteColors;
    public final CanvasType canvasType;
    public final boolean sidesActive;
    public final int[] sidePixels;

    public CanvasUpdatePacket(int[] pixels, boolean signed, String title, String canvasId, int version, int easelId,
                              PaletteUtil.CustomColor[] paletteColors, CanvasType canvasType,
                              boolean sidesActive, int[] sidePixels) {
        this.pixels = pixels;
        this.signed = signed;
        this.title = title;
        this.canvasId = canvasId;
        this.version = version;
        this.easelId = easelId;
        this.paletteColors = paletteColors;
        this.canvasType = canvasType;
        this.sidesActive = sidesActive;
        this.sidePixels = sidePixels;
    }

    public CanvasUpdatePacket(FriendlyByteBuf buf) {
        PaletteUtil.CustomColor[] cols = new PaletteUtil.CustomColor[12];
        for (int i = 0; i < cols.length; i++) cols[i] = new PaletteUtil.CustomColor(buf);
        this.paletteColors = cols;
        this.easelId = buf.readInt();
        CanvasType ct = CanvasType.fromByte(buf.readByte());
        this.canvasType = ct == null ? CanvasType.SMALL : ct;
        this.version = buf.readInt();
        this.canvasId = buf.readUtf(64);
        this.title = buf.readUtf(32);
        this.signed = buf.readBoolean();
        int area = CanvasType.getHeight(canvasType) * CanvasType.getWidth(canvasType);
        this.pixels = buf.readVarIntArray(area);
        this.sidesActive = buf.readBoolean();
        this.sidePixels = buf.readVarIntArray(CanvasSides.count(canvasType));
    }

    public void encode(FriendlyByteBuf buf) {
        for (PaletteUtil.CustomColor color : paletteColors) color.writeToBuffer(buf);
        buf.writeInt(easelId);
        buf.writeByte(canvasType.ordinal());
        buf.writeInt(version);
        buf.writeUtf(canvasId);
        buf.writeUtf(title);
        buf.writeBoolean(signed);
        buf.writeVarIntArray(pixels);
        buf.writeBoolean(sidesActive);
        buf.writeVarIntArray(sidePixels == null ? new int[0] : sidePixels);
    }

    public static void handle(CanvasUpdatePacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        ServerPlayer pl = ctxSupplier.get().getSender();
        if (pl == null) return;

        ItemStack canvas;
        ItemStack palette;
        Entity entityEasel = null;

        if (msg.easelId > -1) {
            entityEasel = pl.level().getEntity(msg.easelId);
            if (!(entityEasel instanceof EntityEasel easel)) return;
            canvas = easel.getItem();
            if (!(canvas.getItem() instanceof ItemCanvas)) return;
            ItemStack main = pl.getMainHandItem();
            ItemStack off = pl.getOffhandItem();
            if (main.getItem() instanceof ItemPalette) palette = main;
            else if (off.getItem() instanceof ItemPalette) palette = off;
            else return;
        } else {
            canvas = pl.getMainHandItem();
            palette = pl.getOffhandItem();
            if (canvas.getItem() instanceof ItemPalette) {
                ItemStack tmp = canvas;
                canvas = palette;
                palette = tmp;
            }
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
        if (msg.signed) {
            NbtCanvas.setAuthor(canvas, pl.getName().getString());
            NbtCanvas.setTitle(canvas, msg.title.trim());
            NbtCanvas.setGeneration(canvas, 1);
        }

        if (!palette.isEmpty() && palette.getItem() == ModItems.ITEM_PALETTE.get()) {
            NbtPalette.setCustomColors(palette, msg.paletteColors);
        }

        if (entityEasel instanceof EntityEasel easel) {
            easel.setItem(canvas, false);
            easel.setPainter(null);
        }
    }
}
