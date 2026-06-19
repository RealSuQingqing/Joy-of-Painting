package com.leclowndu93150.joyofpainting.item;

import com.leclowndu93150.joyofpainting.client.JoyOfPaintingClient;
import com.leclowndu93150.joyofpainting.nbt.NbtPalette;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
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

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getTag();
        boolean hasBasic = NbtPalette.countColors(stack) > 0 || (tag != null && tag.contains(NbtPalette.KEY_BASIC));
        boolean hasCustom = NbtPalette.hasCustomColors(stack);
        if (!hasBasic && !hasCustom) {
            tooltip.add(Component.translatable("palette.empty").withStyle(ChatFormatting.GRAY));
            return;
        }
        if (hasBasic) {
            int basicCount = NbtPalette.countColors(stack);
            tooltip.add(Component.translatable("palette.basic_count", String.valueOf(basicCount)).withStyle(ChatFormatting.GRAY));
        }
        if (hasCustom) {
            int fullCount = 0;
            for (var c : NbtPalette.getCustomColors(stack)) {
                if (c.numberOfColors > 0) fullCount++;
            }
            tooltip.add(Component.translatable("palette.custom_count", String.valueOf(fullCount)).withStyle(ChatFormatting.GRAY));
        }
    }
}
