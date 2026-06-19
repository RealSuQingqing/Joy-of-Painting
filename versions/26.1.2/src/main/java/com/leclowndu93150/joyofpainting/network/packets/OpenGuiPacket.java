package com.leclowndu93150.joyofpainting.network.packets;

import com.leclowndu93150.joyofpainting.JoyOfPainting;
import com.leclowndu93150.joyofpainting.client.JoyOfPaintingClient;
import com.leclowndu93150.joyofpainting.entity.EntityEasel;
import com.leclowndu93150.joyofpainting.item.ItemPalette;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenGuiPacket(int easelId, boolean allowed, boolean edit, InteractionHand hand) implements CustomPacketPayload {
    public static final Type<OpenGuiPacket> TYPE = new Type<>(JoyOfPainting.id("open_gui"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenGuiPacket> STREAM_CODEC = StreamCodec.of(
            (buf, msg) -> {
                buf.writeInt(msg.easelId);
                buf.writeBoolean(msg.allowed);
                buf.writeBoolean(msg.edit);
                buf.writeByte(msg.hand.ordinal());
            },
            buf -> {
                int easelId = buf.readInt();
                boolean allowed = buf.readBoolean();
                boolean edit = buf.readBoolean();
                int ord = buf.readByte();
                InteractionHand hand = ord < InteractionHand.values().length ? InteractionHand.values()[ord] : InteractionHand.MAIN_HAND;
                return new OpenGuiPacket(easelId, allowed, edit, hand);
            });

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenGuiPacket msg, IPayloadContext ctx) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        if (!msg.allowed) {
            player.sendSystemMessage(Component.translatable("easel.deny").withStyle(ChatFormatting.RED));
            return;
        }
        Entity entity = player.level().getEntity(msg.easelId);
        if (!(entity instanceof EntityEasel easel)) return;
        ItemStack inHand = player.getItemInHand(msg.hand);
        boolean handPalette = inHand.getItem() instanceof ItemPalette;
        if (msg.edit) {
            if (handPalette) JoyOfPaintingClient.showCanvasGui(easel, inHand);
        } else {
            JoyOfPaintingClient.showCanvasGui(easel, ItemStack.EMPTY);
        }
    }
}
