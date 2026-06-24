package com.leclowndu93150.joyofpainting.item.crafting;

import com.leclowndu93150.joyofpainting.item.ItemPalette;
import com.leclowndu93150.joyofpainting.registry.ModDataComponents;
import com.leclowndu93150.joyofpainting.registry.ModItems;
import com.leclowndu93150.joyofpainting.registry.ModRecipeSerializers;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class RecipeFillPalette extends CustomRecipe {
    public static final RecipeFillPalette INSTANCE = new RecipeFillPalette();
    public static final MapCodec<RecipeFillPalette> MAP_CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, RecipeFillPalette> STREAM_CODEC = StreamCodec.unit(INSTANCE);
    public static final RecipeSerializer<RecipeFillPalette> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    public RecipeFillPalette() {}

    private boolean isPalette(ItemStack stack) {
        return stack.getItem() instanceof ItemPalette;
    }

    private boolean isDye(ItemStack stack) {
        if (!(stack.getItem() instanceof DyeItem)) return false;
        DyeColor color = stack.get(DataComponents.DYE);
        return color != null && color.getId() >= 0 && color.getId() < 16;
    }

    private int findPalette(CraftingInput inv) {
        for (int i = 0; i < inv.size(); ++i) {
            if (isPalette(inv.getItem(i))) return i;
        }
        return -1;
    }

    @Nullable
    private ArrayList<ItemStack> findDyes(CraftingInput inv, int paletteId) {
        ArrayList<ItemStack> dyes = new ArrayList<>();
        for (int i = 0; i < inv.size(); ++i) {
            if (i == paletteId) continue;
            ItemStack stack = inv.getItem(i);
            if (isDye(stack)) dyes.add(stack);
            else if (!stack.isEmpty()) return null;
        }
        return dyes;
    }

    @Override
    public boolean matches(CraftingInput inv, Level level) {
        int paletteId = findPalette(inv);
        if (paletteId < 0) return false;
        ArrayList<ItemStack> dyes = findDyes(inv, paletteId);
        return dyes != null && !dyes.isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput inv) {
        int paletteId = findPalette(inv);
        if (paletteId < 0) return ItemStack.EMPTY;
        ArrayList<ItemStack> dyes = findDyes(inv, paletteId);
        if (dyes == null || dyes.isEmpty()) return ItemStack.EMPTY;

        ItemStack input = inv.getItem(paletteId);
        ModDataComponents.PaletteBasicColors existing = input.get(ModDataComponents.PALETTE_BASIC_COLORS.get());
        byte[] basic = existing != null ? existing.colors().clone() : new byte[16];

        for (ItemStack dye : dyes) {
            DyeColor color = dye.get(DataComponents.DYE);
            if (color == null) return ItemStack.EMPTY;
            int idx = 15 - color.getId();
            if (basic[idx] > 0) return ItemStack.EMPTY;
            basic[idx] = 1;
        }

        ItemStack result = new ItemStack(ModItems.ITEM_PALETTE.get());
        result.set(ModDataComponents.PALETTE_BASIC_COLORS.get(), new ModDataComponents.PaletteBasicColors(basic));
        return result;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
        return NonNullList.withSize(inv.size(), ItemStack.EMPTY);
    }

    @Override
    public RecipeSerializer<RecipeFillPalette> getSerializer() {
        return ModRecipeSerializers.CRAFTING_SPECIAL_PALETTE_FILLING.get();
    }
}
