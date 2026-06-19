package com.leclowndu93150.joyofpainting.network.packets;

import com.leclowndu93150.joyofpainting.command.CommandImport;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ImportPaintingSendPacket {
    public final CompoundTag tag;

    public ImportPaintingSendPacket(CompoundTag tag) {
        this.tag = tag;
    }

    public ImportPaintingSendPacket(FriendlyByteBuf buf) {
        this.tag = buf.readNbt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeNbt(tag);
    }

    public static void handle(ImportPaintingSendPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        ServerPlayer pl = ctxSupplier.get().getSender();
        if (pl == null || msg.tag == null) return;
        CommandImport.doImport(msg.tag, pl);
    }
}
