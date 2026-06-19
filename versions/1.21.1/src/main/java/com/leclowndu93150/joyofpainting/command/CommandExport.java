package com.leclowndu93150.joyofpainting.command;

import com.leclowndu93150.joyofpainting.item.ItemCanvas;
import com.leclowndu93150.joyofpainting.network.packets.ExportPaintingPacket;
import com.leclowndu93150.joyofpainting.registry.ModDataComponents;
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
import net.neoforged.neoforge.network.PacketDistributor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class CommandExport {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("paintexport")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(p -> paintExport(p.getSource(), StringArgumentType.getString(p, "name")))));
    }

    private static int paintExport(CommandSourceStack stack, String name) {
        Entity entity = stack.getEntity();
        if (!(entity instanceof ServerPlayer player)) return 0;
        PacketDistributor.sendToPlayer(player, new ExportPaintingPacket(name));
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
            List<Integer> pixels = s.get(ModDataComponents.CANVAS_PIXELS.get());
            String canvasId = s.get(ModDataComponents.CANVAS_ID.get());
            if (pixels == null || canvasId == null) continue;
            try {
                int version = s.getOrDefault(ModDataComponents.CANVAS_VERSION.get(), 1);
                int generation = s.getOrDefault(ModDataComponents.CANVAS_GENERATION.get(), 0);
                String title = s.get(ModDataComponents.CANVAS_TITLE.get());
                String author = s.get(ModDataComponents.CANVAS_AUTHOR.get());

                CompoundTag tag = new CompoundTag();
                tag.putIntArray("pixels", pixels.stream().mapToInt(Integer::intValue).toArray());
                tag.putByte("ct", (byte) canvasItem.getCanvasType().ordinal());
                if (title != null && author != null) {
                    tag.putString("title", title);
                    tag.putString("author", author);
                    tag.putString("name", canvasId);
                    tag.putInt("v", version);
                    tag.putInt("generation", generation);
                }
                NbtIo.write(tag, Path.of(filepath));
                return true;
            } catch (IOException ignored) {
            }
        }
        return false;
    }
}
