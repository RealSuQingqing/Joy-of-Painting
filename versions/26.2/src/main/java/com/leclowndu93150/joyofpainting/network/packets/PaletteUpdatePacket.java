package com.leclowndu93150.joyofpainting.network.packets;

import com.leclowndu93150.joyofpainting.JoyOfPainting;
import com.leclowndu93150.joyofpainting.PaletteUtil;
import com.leclowndu93150.joyofpainting.registry.ModDataComponents;
import com.leclowndu93150.joyofpainting.registry.ModDataComponents.PaletteCustomColors;
import com.leclowndu93150.joyofpainting.registry.ModItems;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PaletteUpdatePacket(PaletteUtil.CustomColor[] paletteColors) implements CustomPacketPayload {
    public static final Type<PaletteUpdatePacket> TYPE = new Type<>(JoyOfPainting.id("palette_update"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PaletteUpdatePacket> STREAM_CODEC = StreamCodec.of(
            (buf, msg) -> { for (PaletteUtil.CustomColor c : msg.paletteColors) c.writeToBuffer(buf); },
            buf -> {
                PaletteUtil.CustomColor[] cols = new PaletteUtil.CustomColor[12];
                for (int i = 0; i < 12; i++) cols[i] = new PaletteUtil.CustomColor(buf);
                return new PaletteUpdatePacket(cols);
            });

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PaletteUpdatePacket msg, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer pl)) return;
        ItemStack palette = pl.getMainHandItem();
        if (palette.isEmpty() || palette.getItem() != ModItems.ITEM_PALETTE.get()) {
            palette = pl.getOffhandItem();
            if (palette.isEmpty() || palette.getItem() != ModItems.ITEM_PALETTE.get()) return;
        }
        palette.set(ModDataComponents.PALETTE_CUSTOM_COLORS.get(), new PaletteCustomColors(msg.paletteColors));
    }
}
