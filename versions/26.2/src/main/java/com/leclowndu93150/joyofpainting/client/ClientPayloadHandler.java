package com.leclowndu93150.joyofpainting.client;

import com.leclowndu93150.joyofpainting.command.CommandExport;
import com.leclowndu93150.joyofpainting.entity.EntityEasel;
import com.leclowndu93150.joyofpainting.item.ItemPalette;
import com.leclowndu93150.joyofpainting.network.packets.CloseGuiPacket;
import com.leclowndu93150.joyofpainting.network.packets.ExportPaintingPacket;
import com.leclowndu93150.joyofpainting.network.packets.ImportPaintingPacket;
import com.leclowndu93150.joyofpainting.network.packets.ImportPaintingSendPacket;
import com.leclowndu93150.joyofpainting.network.packets.OpenGuiPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.io.IOException;
import java.nio.file.Path;

public final class ClientPayloadHandler {
    private ClientPayloadHandler() {}

    public static void handleCloseGui(CloseGuiPacket msg, IPayloadContext ctx) {
        Minecraft.getInstance().gui.setScreen(null);
    }

    public static void handleExport(ExportPaintingPacket msg, IPayloadContext ctx) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (CommandExport.doExport(mc.player, msg.canvasId())) {
            mc.player.sendSystemMessage(Component.translatable("joyofpainting.export.success", msg.canvasId()).withStyle(ChatFormatting.GREEN));
        } else {
            mc.player.sendSystemMessage(Component.translatable("joyofpainting.export.fail", msg.canvasId()).withStyle(ChatFormatting.RED));
        }
    }

    public static void handleImport(ImportPaintingPacket msg, IPayloadContext ctx) {
        String filename = msg.canvasId() + ".paint";
        String filepath = "paintings/" + filename;
        try {
            CompoundTag tag = NbtIo.read(Path.of(filepath));
            ClientPacketDistributor.sendToServer(new ImportPaintingSendPacket(tag));
        } catch (IOException e) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                player.sendSystemMessage(Component.translatable("joyofpainting.import.fail.4", filepath).withStyle(ChatFormatting.RED));
            }
        }
    }

    public static void handleOpenGui(OpenGuiPacket msg, IPayloadContext ctx) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        if (!msg.allowed()) {
            player.sendSystemMessage(Component.translatable("easel.deny").withStyle(ChatFormatting.RED));
            return;
        }
        Entity entity = player.level().getEntity(msg.easelId());
        if (!(entity instanceof EntityEasel easel)) return;
        ItemStack inHand = player.getItemInHand(msg.hand());
        boolean handPalette = inHand.getItem() instanceof ItemPalette;
        if (msg.edit()) {
            if (handPalette) JoyOfPaintingClient.showCanvasGui(easel, inHand);
        } else {
            JoyOfPaintingClient.showCanvasGui(easel, ItemStack.EMPTY);
        }
    }
}
