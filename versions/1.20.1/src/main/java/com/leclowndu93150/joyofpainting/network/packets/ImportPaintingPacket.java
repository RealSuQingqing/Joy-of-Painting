package com.leclowndu93150.joyofpainting.network.packets;

import com.leclowndu93150.joyofpainting.network.JoyOfPaintingNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;

public class ImportPaintingPacket {
    public final String canvasId;

    public ImportPaintingPacket(String canvasId) {
        this.canvasId = canvasId;
    }

    public ImportPaintingPacket(FriendlyByteBuf buf) {
        this.canvasId = buf.readUtf(64);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(canvasId);
    }

    public static void handle(ImportPaintingPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient(msg));
    }

    private static void handleClient(ImportPaintingPacket msg) {
        String filename = msg.canvasId + ".paint";
        String filepath = "paintings/" + filename;
        try {
            CompoundTag tag = NbtIo.read(new File(filepath));
            JoyOfPaintingNetwork.CHANNEL.sendToServer(new ImportPaintingSendPacket(tag));
        } catch (IOException e) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                player.displayClientMessage(Component.translatable("joyofpainting.import.fail.4", filepath).withStyle(ChatFormatting.RED), false);
            }
        }
    }
}
