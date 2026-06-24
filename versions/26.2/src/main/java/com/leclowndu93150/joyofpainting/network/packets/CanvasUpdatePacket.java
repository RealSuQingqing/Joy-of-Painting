package com.leclowndu93150.joyofpainting.network.packets;

import com.leclowndu93150.joyofpainting.CanvasSides;
import com.leclowndu93150.joyofpainting.CanvasType;
import com.leclowndu93150.joyofpainting.JoyOfPainting;
import com.leclowndu93150.joyofpainting.PaletteUtil;
import com.leclowndu93150.joyofpainting.entity.EntityEasel;
import com.leclowndu93150.joyofpainting.item.ItemCanvas;
import com.leclowndu93150.joyofpainting.item.ItemPalette;
import com.leclowndu93150.joyofpainting.registry.ModDataComponents;
import com.leclowndu93150.joyofpainting.registry.ModDataComponents.PaletteCustomColors;
import com.leclowndu93150.joyofpainting.registry.ModItems;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Arrays;

public record CanvasUpdatePacket(int[] pixels, boolean signed, String title, String canvasId, int version, int easelId,
                                 PaletteUtil.CustomColor[] paletteColors, CanvasType canvasType,
                                 boolean sidesActive, int[] sidePixels) implements CustomPacketPayload {
    public static final Type<CanvasUpdatePacket> TYPE = new Type<>(JoyOfPainting.id("canvas_update"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CanvasUpdatePacket> STREAM_CODEC = StreamCodec.of(
            CanvasUpdatePacket::encode, CanvasUpdatePacket::decode);

    private static void encode(RegistryFriendlyByteBuf buf, CanvasUpdatePacket msg) {
        for (PaletteUtil.CustomColor color : msg.paletteColors) color.writeToBuffer(buf);
        buf.writeInt(msg.easelId);
        buf.writeByte(msg.canvasType.ordinal());
        buf.writeInt(msg.version);
        buf.writeUtf(msg.canvasId);
        buf.writeUtf(msg.title);
        buf.writeBoolean(msg.signed);
        buf.writeVarIntArray(msg.pixels);
        buf.writeBoolean(msg.sidesActive);
        buf.writeVarIntArray(msg.sidePixels == null ? new int[0] : msg.sidePixels);
    }

    private static CanvasUpdatePacket decode(RegistryFriendlyByteBuf buf) {
        PaletteUtil.CustomColor[] cols = new PaletteUtil.CustomColor[12];
        for (int i = 0; i < 12; i++) cols[i] = new PaletteUtil.CustomColor(buf);
        int easelId = buf.readInt();
        CanvasType ct = CanvasType.fromByte(buf.readByte());
        if (ct == null) ct = CanvasType.SMALL;
        int version = buf.readInt();
        String canvasId = buf.readUtf(64);
        String title = buf.readUtf(32);
        boolean signed = buf.readBoolean();
        int area = CanvasType.getHeight(ct) * CanvasType.getWidth(ct);
        int[] pixels = buf.readVarIntArray(area);
        boolean sidesActive = buf.readBoolean();
        int[] sidePixels = buf.readVarIntArray(CanvasSides.count(ct));
        return new CanvasUpdatePacket(pixels, signed, title, canvasId, version, easelId, cols, ct, sidesActive, sidePixels);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CanvasUpdatePacket msg, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer pl)) return;
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
        canvas.set(ModDataComponents.CANVAS_PIXELS.get(), Arrays.stream(msg.pixels).boxed().toList());
        canvas.set(ModDataComponents.CANVAS_ID.get(), msg.canvasId);
        canvas.set(ModDataComponents.CANVAS_VERSION.get(), msg.version);
        canvas.remove(ModDataComponents.CANVAS_GENERATION.get());
        canvas.set(ModDataComponents.CANVAS_SIDES_ACTIVE.get(), msg.sidesActive);
        if (msg.sidePixels != null && msg.sidePixels.length > 0) {
            canvas.set(ModDataComponents.CANVAS_SIDE_PIXELS.get(), Arrays.stream(msg.sidePixels).boxed().toList());
        }
        if (msg.signed) {
            canvas.set(ModDataComponents.CANVAS_AUTHOR.get(), pl.getName().getString());
            canvas.set(ModDataComponents.CANVAS_TITLE.get(), msg.title.trim());
            canvas.set(ModDataComponents.CANVAS_GENERATION.get(), 1);
        }
        ItemCanvas.updateStackSize(canvas);

        if (!palette.isEmpty() && palette.getItem() == ModItems.ITEM_PALETTE.get()) {
            palette.set(ModDataComponents.PALETTE_CUSTOM_COLORS.get(), new PaletteCustomColors(msg.paletteColors));
        }

        if (entityEasel instanceof EntityEasel easel) {
            easel.setItem(canvas, false);
            easel.setPainter(null);
        }
    }
}
