package com.leclowndu93150.joyofpainting.network.packets;

import com.leclowndu93150.joyofpainting.JoyOfPainting;
import com.leclowndu93150.joyofpainting.command.CommandExport;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ExportPaintingPacket(String canvasId) implements CustomPacketPayload {
    public static final Type<ExportPaintingPacket> TYPE = new Type<>(JoyOfPainting.id("export_painting"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ExportPaintingPacket> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.STRING_UTF8, ExportPaintingPacket::canvasId, ExportPaintingPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ExportPaintingPacket msg, IPayloadContext ctx) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (CommandExport.doExport(mc.player, msg.canvasId)) {
            mc.player.sendSystemMessage(Component.translatable("joyofpainting.export.success", msg.canvasId).withStyle(ChatFormatting.GREEN));
        } else {
            mc.player.sendSystemMessage(Component.translatable("joyofpainting.export.fail", msg.canvasId).withStyle(ChatFormatting.RED));
        }
    }
}
