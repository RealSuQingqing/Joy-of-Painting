package com.leclowndu93150.joyofpainting.network.packets;

import com.leclowndu93150.joyofpainting.PaletteUtil;
import com.leclowndu93150.joyofpainting.nbt.NbtPalette;
import com.leclowndu93150.joyofpainting.registry.ModItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PaletteUpdatePacket {
    public final PaletteUtil.CustomColor[] paletteColors;

    public PaletteUpdatePacket(PaletteUtil.CustomColor[] paletteColors) {
        this.paletteColors = paletteColors;
    }

    public PaletteUpdatePacket(FriendlyByteBuf buf) {
        PaletteUtil.CustomColor[] cols = new PaletteUtil.CustomColor[12];
        for (int i = 0; i < cols.length; i++) cols[i] = new PaletteUtil.CustomColor(buf);
        this.paletteColors = cols;
    }

    public void encode(FriendlyByteBuf buf) {
        for (PaletteUtil.CustomColor c : paletteColors) c.writeToBuffer(buf);
    }

    public static void handle(PaletteUpdatePacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        ServerPlayer pl = ctxSupplier.get().getSender();
        if (pl == null) return;
        ItemStack palette = pl.getMainHandItem();
        if (palette.isEmpty() || palette.getItem() != ModItems.ITEM_PALETTE.get()) {
            palette = pl.getOffhandItem();
            if (palette.isEmpty() || palette.getItem() != ModItems.ITEM_PALETTE.get()) return;
        }
        NbtPalette.setCustomColors(palette, msg.paletteColors);
    }
}
