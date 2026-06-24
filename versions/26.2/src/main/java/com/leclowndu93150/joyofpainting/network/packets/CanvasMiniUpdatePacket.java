package com.leclowndu93150.joyofpainting.network.packets;

import com.leclowndu93150.joyofpainting.CanvasSides;
import com.leclowndu93150.joyofpainting.CanvasType;
import com.leclowndu93150.joyofpainting.JoyOfPainting;
import com.leclowndu93150.joyofpainting.entity.EntityEasel;
import com.leclowndu93150.joyofpainting.item.ItemCanvas;
import com.leclowndu93150.joyofpainting.item.ItemPalette;
import com.leclowndu93150.joyofpainting.registry.ModDataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Arrays;

public record CanvasMiniUpdatePacket(int[] pixels, String canvasId, int version, int easelId, CanvasType canvasType,
                                     boolean sidesActive, int[] sidePixels) implements CustomPacketPayload {
    public static final Type<CanvasMiniUpdatePacket> TYPE = new Type<>(JoyOfPainting.id("canvas_mini_update"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CanvasMiniUpdatePacket> STREAM_CODEC = StreamCodec.of(
            CanvasMiniUpdatePacket::encode, CanvasMiniUpdatePacket::decode);

    private static void encode(RegistryFriendlyByteBuf buf, CanvasMiniUpdatePacket msg) {
        buf.writeInt(msg.easelId);
        buf.writeByte(msg.canvasType.ordinal());
        buf.writeInt(msg.version);
        buf.writeUtf(msg.canvasId);
        buf.writeVarIntArray(msg.pixels);
        buf.writeBoolean(msg.sidesActive);
        buf.writeVarIntArray(msg.sidePixels == null ? new int[0] : msg.sidePixels);
    }

    private static CanvasMiniUpdatePacket decode(RegistryFriendlyByteBuf buf) {
        int easelId = buf.readInt();
        CanvasType ct = CanvasType.fromByte(buf.readByte());
        if (ct == null) ct = CanvasType.SMALL;
        int version = buf.readInt();
        String canvasId = buf.readUtf(64);
        int area = CanvasType.getHeight(ct) * CanvasType.getWidth(ct);
        int[] pixels = buf.readVarIntArray(area);
        boolean sidesActive = buf.readBoolean();
        int[] sidePixels = buf.readVarIntArray(CanvasSides.count(ct));
        return new CanvasMiniUpdatePacket(pixels, canvasId, version, easelId, ct, sidesActive, sidePixels);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CanvasMiniUpdatePacket msg, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer pl)) return;
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
        canvas.set(ModDataComponents.CANVAS_PIXELS.get(), Arrays.stream(msg.pixels).boxed().toList());
        canvas.set(ModDataComponents.CANVAS_ID.get(), msg.canvasId);
        canvas.set(ModDataComponents.CANVAS_VERSION.get(), msg.version);
        canvas.remove(ModDataComponents.CANVAS_GENERATION.get());
        canvas.set(ModDataComponents.CANVAS_SIDES_ACTIVE.get(), msg.sidesActive);
        if (msg.sidePixels != null && msg.sidePixels.length > 0) {
            canvas.set(ModDataComponents.CANVAS_SIDE_PIXELS.get(), Arrays.stream(msg.sidePixels).boxed().toList());
        }

        if (entityEasel instanceof EntityEasel easel) {
            easel.setItem(canvas, false);
        }
    }
}
