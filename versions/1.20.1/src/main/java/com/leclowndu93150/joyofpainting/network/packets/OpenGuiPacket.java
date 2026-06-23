package com.leclowndu93150.joyofpainting.network.packets;

import com.leclowndu93150.joyofpainting.client.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenGuiPacket {
    public final int easelId;
    public final boolean allowed;
    public final boolean edit;
    public final InteractionHand hand;

    public OpenGuiPacket(int easelId, boolean allowed, boolean edit, InteractionHand hand) {
        this.easelId = easelId;
        this.allowed = allowed;
        this.edit = edit;
        this.hand = hand;
    }

    public OpenGuiPacket(FriendlyByteBuf buf) {
        this.easelId = buf.readInt();
        this.allowed = buf.readBoolean();
        this.edit = buf.readBoolean();
        int ordinal = buf.readByte();
        this.hand = ordinal < InteractionHand.values().length ? InteractionHand.values()[ordinal] : InteractionHand.MAIN_HAND;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(easelId);
        buf.writeBoolean(allowed);
        buf.writeBoolean(edit);
        buf.writeByte(hand.ordinal());
    }

    public static void handle(OpenGuiPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handleOpenGui(msg));
    }
}
