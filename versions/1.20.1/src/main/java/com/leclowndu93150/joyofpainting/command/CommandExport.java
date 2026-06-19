package com.leclowndu93150.joyofpainting.command;

import com.leclowndu93150.joyofpainting.item.ItemCanvas;
import com.leclowndu93150.joyofpainting.nbt.NbtCanvas;
import com.leclowndu93150.joyofpainting.network.JoyOfPaintingNetwork;
import com.leclowndu93150.joyofpainting.network.packets.ExportPaintingPacket;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;

import java.io.File;
import java.io.IOException;

public class CommandExport {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("paintexport")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(p -> paintExport(p.getSource(), StringArgumentType.getString(p, "name"))))
        );
    }

    private static int paintExport(CommandSourceStack stack, String name) {
        Entity commander = stack.getEntity();
        if (!(commander instanceof ServerPlayer player)) return 0;
        JoyOfPaintingNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ExportPaintingPacket(name));
        return 1;
    }

    public static boolean doExport(Player player, String name) {
        String dir = "paintings";
        String filename = name + ".paint";
        String filepath = dir + "/" + filename;
        File directory = new File(dir);
        if (!directory.exists()) directory.mkdir();

        for (ItemStack s : player.getHandSlots()) {
            if (!(s.getItem() instanceof ItemCanvas canvasItem)) continue;
            if (!NbtCanvas.hasPixels(s)) continue;
            String canvasId = NbtCanvas.getName(s);
            if (canvasId.isEmpty()) continue;
            try {
                int version = NbtCanvas.getVersion(s);
                if (version <= 0) version = 1;
                int generation = NbtCanvas.getGeneration(s);
                String title = NbtCanvas.hasTitle(s) ? NbtCanvas.getTitle(s) : null;
                String author = title != null ? NbtCanvas.getAuthor(s) : null;

                CompoundTag tag = new CompoundTag();
                tag.putIntArray("pixels", NbtCanvas.getPixels(s));
                tag.putByte("ct", (byte) canvasItem.getCanvasType().ordinal());
                if (title != null && author != null && !author.isEmpty()) {
                    tag.putString("title", title);
                    tag.putString("author", author);
                    tag.putString("name", canvasId);
                    tag.putInt("v", version);
                    tag.putInt("generation", generation);
                }
                NbtIo.write(tag, new File(filepath));
                return true;
            } catch (IOException ignored) {
            }
        }
        return false;
    }
}
