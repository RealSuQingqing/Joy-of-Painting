package com.leclowndu93150.joyofpainting.network.packets;

import com.leclowndu93150.joyofpainting.JoyOfPainting;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.io.IOException;
import java.nio.file.Path;

public record ImportPaintingPacket(String canvasId) implements CustomPacketPayload {
    public static final Type<ImportPaintingPacket> TYPE = new Type<>(JoyOfPainting.id("import_painting"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ImportPaintingPacket> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.STRING_UTF8, ImportPaintingPacket::canvasId, ImportPaintingPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ImportPaintingPacket msg, IPayloadContext ctx) {
        String filename = msg.canvasId + ".paint";
        String filepath = "paintings/" + filename;
        try {
            CompoundTag tag = NbtIo.read(Path.of(filepath));
            PacketDistributor.sendToServer(new ImportPaintingSendPacket(tag));
        } catch (IOException e) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                player.displayClientMessage(Component.translatable("joyofpainting.import.fail.4", filepath).withStyle(ChatFormatting.RED), false);
            }
        }
    }
}
