package com.leclowndu93150.joyofpainting.command;

import com.leclowndu93150.joyofpainting.CanvasType;
import com.leclowndu93150.joyofpainting.item.ItemCanvas;
import com.leclowndu93150.joyofpainting.item.ItemPalette;
import com.leclowndu93150.joyofpainting.network.packets.ImportPaintingPacket;
import com.leclowndu93150.joyofpainting.registry.ModDataComponents;
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
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Arrays;

public class CommandImport {
    private static final String IMPORT_FAIL_BROKEN_FILE_KEY = "joyofpainting.import.fail.5";
    private static final String TAG_AUTHOR = "author";
    private static final String TAG_TITLE = "title";
    private static final String TAG_GENERATION = "generation";

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("paintimport")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(p -> paintImport(p.getSource(), StringArgumentType.getString(p, "name")))));
    }

    private static int paintImport(CommandSourceStack stack, String name) {
        try {
            ServerPlayer player = stack.getPlayerOrException();
            PacketDistributor.sendToPlayer(player, new ImportPaintingPacket(name));
        } catch (CommandSyntaxException e) {
            return 0;
        }
        return 1;
    }

    public static void doImport(CompoundTag tag, ServerPlayer player) {
        if (tag == null) {
            broken(player);
            return;
        }
        if (!tag.contains("ct", Tag.TAG_BYTE)) {
            broken(player);
            return;
        }
        boolean hasAuthor = tag.contains(TAG_AUTHOR, Tag.TAG_STRING);
        boolean hasTitle = tag.contains(TAG_TITLE, Tag.TAG_STRING);
        if (hasAuthor != hasTitle) {
            broken(player);
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
                broken(player);
                return;
            }
            String name = tag.getString("name");
            if (!name.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}_\\d+$")) {
                broken(player);
                return;
            }
            if (!tag.contains("v", Tag.TAG_INT)) tag.putInt("v", 1);
        } else {
            tag.putString("name", ItemCanvas.generateName(player));
            tag.putInt("v", 1);
            tag.remove(TAG_GENERATION);
        }

        byte canvasType = tag.getByte("ct");
        CanvasType importedCanvasType = CanvasType.fromByte(canvasType);
        if (importedCanvasType == null) {
            broken(player);
            return;
        }
        tag.remove("ct");
        if (tag.getInt(TAG_GENERATION) > 0) {
            tag.putInt(TAG_GENERATION, tag.getInt(TAG_GENERATION) + 1);
        }

        ItemStack itemStack;
        boolean doAddItem = false;
        if (player.isCreative()) {
            itemStack = switch (importedCanvasType) {
                case SMALL -> new ItemStack(ModItems.ITEM_CANVAS.get());
                case LONG -> new ItemStack(ModItems.ITEM_CANVAS_LONG.get());
                case TALL -> new ItemStack(ModItems.ITEM_CANVAS_TALL.get());
                case LARGE -> new ItemStack(ModItems.ITEM_CANVAS_LARGE.get());
            };
            doAddItem = true;
        } else {
            ItemStack main = player.getMainHandItem();
            ItemStack off = player.getOffhandItem();
            if (!(main.getItem() instanceof ItemCanvas) || ItemCanvas.hasPixels(main) || main.has(ModDataComponents.CANVAS_ID.get())) {
                player.sendSystemMessage(Component.translatable("joyofpainting.import.fail.1").withStyle(ChatFormatting.RED));
                return;
            }
            if (((ItemCanvas) main.getItem()).getCanvasType() != importedCanvasType) {
                Component typeName = switch (importedCanvasType) {
                    case SMALL -> ModItems.ITEM_CANVAS.get().getName(ItemStack.EMPTY);
                    case LONG -> ModItems.ITEM_CANVAS_LONG.get().getName(ItemStack.EMPTY);
                    case TALL -> ModItems.ITEM_CANVAS_TALL.get().getName(ItemStack.EMPTY);
                    case LARGE -> ModItems.ITEM_CANVAS_LARGE.get().getName(ItemStack.EMPTY);
                };
                player.sendSystemMessage(Component.translatable("joyofpainting.import.fail.2", typeName).withStyle(ChatFormatting.RED));
                return;
            }
            if (!ItemPalette.isFull(off)) {
                player.sendSystemMessage(Component.translatable("joyofpainting.import.fail.3").withStyle(ChatFormatting.RED));
                return;
            }
            itemStack = main;
        }

        itemStack.set(ModDataComponents.CANVAS_VERSION.get(), tag.getInt("v"));
        itemStack.set(ModDataComponents.CANVAS_ID.get(), tag.getString("name"));
        itemStack.set(ModDataComponents.CANVAS_PIXELS.get(),
                Arrays.stream(tag.getIntArray("pixels")).boxed().toList());
        itemStack.set(ModDataComponents.CANVAS_GENERATION.get(), tag.getInt(TAG_GENERATION));
        if (tag.contains(TAG_TITLE, Tag.TAG_STRING) && tag.contains(TAG_AUTHOR, Tag.TAG_STRING)) {
            itemStack.set(ModDataComponents.CANVAS_TITLE.get(), tag.getString(TAG_TITLE));
            itemStack.set(ModDataComponents.CANVAS_AUTHOR.get(), tag.getString(TAG_AUTHOR));
        }
        ItemCanvas.updateStackSize(itemStack);
        if (doAddItem) player.addItem(itemStack);

        player.sendSystemMessage(Component.translatable("joyofpainting.import.success").withStyle(ChatFormatting.GREEN));
    }

    private static void broken(ServerPlayer player) {
        player.sendSystemMessage(Component.translatable(IMPORT_FAIL_BROKEN_FILE_KEY).withStyle(ChatFormatting.RED));
    }
}
