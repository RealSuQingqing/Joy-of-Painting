package com.leclowndu93150.joyofpainting.item;

import com.leclowndu93150.joyofpainting.client.JoyOfPaintingClient;
import com.leclowndu93150.joyofpainting.registry.ModDataComponents;
import com.leclowndu93150.joyofpainting.registry.ModDataComponents.PaletteCustomColors;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

public class ItemPalette extends Item {
    public ItemPalette(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) {
            JoyOfPaintingClient.showCanvasGui(player);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    public static int basicColorCount(ItemStack stack) {
        ModDataComponents.PaletteBasicColors basic = stack.get(ModDataComponents.PALETTE_BASIC_COLORS.get());
        if (basic == null) return 0;
        int n = 0;
        for (byte b : basic.colors()) if (b > 0) n++;
        return n;
    }

    public static boolean isFull(ItemStack stack) {
        return basicColorCount(stack) == 16;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        ModDataComponents.PaletteBasicColors basic = stack.get(ModDataComponents.PALETTE_BASIC_COLORS.get());
        PaletteCustomColors custom = stack.get(ModDataComponents.PALETTE_CUSTOM_COLORS.get());
        if (basic == null && custom == null) {
            tooltip.add(Component.translatable("palette.empty").withStyle(ChatFormatting.GRAY));
            return;
        }
        if (basic != null) {
            int count = basicColorCount(stack);
            tooltip.add(Component.translatable("palette.basic_count", String.valueOf(count)).withStyle(ChatFormatting.GRAY));
        }
        if (custom != null) {
            tooltip.add(Component.translatable("palette.custom_count", String.valueOf(custom.filledCount())).withStyle(ChatFormatting.GRAY));
        }
    }
}
