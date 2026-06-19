package com.leclowndu93150.joyofpainting.network.packets;

import com.leclowndu93150.joyofpainting.entity.EntityEasel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class EaselLeftPacket {
    public final int easelId;

    public EaselLeftPacket(int easelId) {
        this.easelId = easelId;
    }

    public EaselLeftPacket(FriendlyByteBuf buf) {
        this.easelId = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(easelId);
    }

    public static void handle(EaselLeftPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        ServerPlayer pl = ctxSupplier.get().getSender();
        if (pl == null || msg.easelId <= -1) return;
        Entity entity = pl.level().getEntity(msg.easelId);
        if (entity instanceof EntityEasel easel) {
            easel.setPainter(null);
        }
    }
}
