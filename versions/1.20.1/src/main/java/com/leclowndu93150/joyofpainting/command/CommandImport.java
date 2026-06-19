package com.leclowndu93150.joyofpainting.command;

import com.leclowndu93150.joyofpainting.CanvasType;
import com.leclowndu93150.joyofpainting.item.ItemCanvas;
import com.leclowndu93150.joyofpainting.nbt.NbtCanvas;
import com.leclowndu93150.joyofpainting.nbt.NbtPalette;
import com.leclowndu93150.joyofpainting.network.JoyOfPaintingNetwork;
import com.leclowndu93150.joyofpainting.network.packets.ImportPaintingPacket;
import com.leclowndu93150.joyofpainting.registry.ModItems;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;

public class CommandImport {
    private static final String IMPORT_FAIL_BROKEN_FILE_KEY = "joyofpainting.import.fail.5";
    private static final String TAG_AUTHOR = "author";
    private static final String TAG_TITLE = "title";
    private static final String TAG_GENERATION = "generation";

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("paintimport")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(p -> paintImport(p.getSource(), StringArgumentType.getString(p, "name"))))
        );
    }

    private static int paintImport(CommandSourceStack stack, String name) {
        try {
            ServerPlayer player = stack.getPlayerOrException();
            JoyOfPaintingNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ImportPaintingPacket(name));
        } catch (CommandSyntaxException e) {
            return 0;
        }
        return 1;
    }

    public static void doImport(CompoundTag tag, ServerPlayer player) {
        if (tag == null) {
            notifyBrokenPaintFile(player);
            return;
        }
        if (!tag.contains("ct", Tag.TAG_BYTE)) {
            notifyBrokenPaintFile(player);
            return;
        }
        boolean hasAuthor = tag.contains(TAG_AUTHOR, Tag.TAG_STRING);
        boolean hasTitle = tag.contains(TAG_TITLE, Tag.TAG_STRING);
        if (hasAuthor != hasTitle) {
            notifyBrokenPaintFile(player);
            return;
        }
        if (hasTitle && tag.getString(TAG_TITLE).length() > 16) {
            tag.putString(TAG_TITLE, tag.getString(TAG_TITLE).substring(0, 16));
        }
        if (hasAuthor && tag.getString(TAG_AUTHOR).length() > 16) {
            tag.putString(TAG_AUTHOR, tag.getString(TAG_AUTHOR).substring(0, 16));
        }
        if (tag.contains(TAG_TITLE)) {
            if (!tag.contains("name", Tag.TAG_STRING)) {
                notifyBrokenPaintFile(player);
                return;
            }
            String name = tag.getString("name");
            if (!name.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}_\\d+$")) {
                notifyBrokenPaintFile(player);
                return;
            }
            if (!tag.contains("v", Tag.TAG_INT)) {
                tag.putInt("v", 1);
            }
        } else {
            tag.putString("name", ItemCanvas.generateName(player));
            tag.putInt("v", 1);
            tag.remove(TAG_GENERATION);
        }

        byte canvasType = tag.getByte("ct");
        CanvasType importedCanvasType = CanvasType.fromByte(canvasType);
        if (importedCanvasType == null) {
            notifyBrokenPaintFile(player);
            return;
        }
        tag.remove("ct");
        if (tag.getInt(TAG_GENERATION) > 0) {
            tag.putInt(TAG_GENERATION, tag.getInt(TAG_GENERATION) + 1);
        }

        ItemStack itemStack;
        boolean doAddItem = false;
        if (player.isCreative()) {
            switch (importedCanvasType) {
                case SMALL -> itemStack = new ItemStack(ModItems.ITEM_CANVAS.get());
                case LONG -> itemStack = new ItemStack(ModItems.ITEM_CANVAS_LONG.get());
                case TALL -> itemStack = new ItemStack(ModItems.ITEM_CANVAS_TALL.get());
                case LARGE -> itemStack = new ItemStack(ModItems.ITEM_CANVAS_LARGE.get());
                default -> {
                    return;
                }
            }
            doAddItem = true;
        } else {
            ItemStack mainHand = player.getMainHandItem();
            ItemStack offHand = player.getOffhandItem();
            if (!(mainHand.getItem() instanceof ItemCanvas) || NbtCanvas.hasPixels(mainHand) || !NbtCanvas.getName(mainHand).isEmpty()) {
                player.sendSystemMessage(Component.translatable("joyofpainting.import.fail.1").withStyle(ChatFormatting.RED));
                return;
            }
            if (((ItemCanvas) mainHand.getItem()).getCanvasType() != importedCanvasType) {
                Component typeName = ModItems.ITEM_CANVAS.get().getName(ItemStack.EMPTY);
                switch (importedCanvasType) {
                    case LONG -> typeName = ModItems.ITEM_CANVAS_LONG.get().getName(ItemStack.EMPTY);
                    case TALL -> typeName = ModItems.ITEM_CANVAS_TALL.get().getName(ItemStack.EMPTY);
                    case LARGE -> typeName = ModItems.ITEM_CANVAS_LARGE.get().getName(ItemStack.EMPTY);
                    default -> {}
                }
                player.sendSystemMessage(Component.translatable("joyofpainting.import.fail.2", typeName).withStyle(ChatFormatting.RED));
                return;
            }
            if (!NbtPalette.isFull(offHand)) {
                player.sendSystemMessage(Component.translatable("joyofpainting.import.fail.3").withStyle(ChatFormatting.RED));
                return;
            }
            itemStack = mainHand;
        }

        NbtCanvas.setVersion(itemStack, tag.getInt("v"));
        NbtCanvas.setName(itemStack, tag.getString("name"));
        NbtCanvas.setPixels(itemStack, tag.getIntArray("pixels"));
        NbtCanvas.setGeneration(itemStack, tag.getInt(TAG_GENERATION));
        if (tag.contains(TAG_TITLE, Tag.TAG_STRING) && tag.contains(TAG_AUTHOR, Tag.TAG_STRING)) {
            NbtCanvas.setTitle(itemStack, tag.getString(TAG_TITLE));
            NbtCanvas.setAuthor(itemStack, tag.getString(TAG_AUTHOR));
        }
        if (doAddItem) {
            player.addItem(itemStack);
        }

        player.sendSystemMessage(Component.translatable("joyofpainting.import.success").withStyle(ChatFormatting.GREEN));
    }

    private static void notifyBrokenPaintFile(ServerPlayer player) {
        player.sendSystemMessage(Component.translatable(IMPORT_FAIL_BROKEN_FILE_KEY).withStyle(ChatFormatting.RED));
    }
}
