package com.leclowndu93150.joyofpainting.network.packets;

import com.leclowndu93150.joyofpainting.client.JoyOfPaintingClient;
import com.leclowndu93150.joyofpainting.entity.EntityEasel;
import com.leclowndu93150.joyofpainting.item.ItemPalette;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient(msg));
    }

    private static void handleClient(OpenGuiPacket msg) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        if (!msg.allowed) {
            player.displayClientMessage(Component.translatable("easel.deny").withStyle(ChatFormatting.RED), false);
            return;
        }
        Entity entity = player.level().getEntity(msg.easelId);
        if (!(entity instanceof EntityEasel easel)) return;
        ItemStack inHand = player.getItemInHand(msg.hand);
        boolean handHoldsPalette = inHand.getItem() instanceof ItemPalette;
        if (msg.edit) {
            if (handHoldsPalette) {
                JoyOfPaintingClient.showCanvasGui(easel, inHand);
            }
        } else {
            JoyOfPaintingClient.showCanvasGui(easel, ItemStack.EMPTY);
        }
    }
}
