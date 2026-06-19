package com.leclowndu93150.joyofpainting.item.crafting;

import com.leclowndu93150.joyofpainting.item.ItemPalette;
import com.leclowndu93150.joyofpainting.nbt.NbtPalette;
import com.leclowndu93150.joyofpainting.registry.ModItems;
import com.leclowndu93150.joyofpainting.registry.ModRecipeSerializers;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class RecipeFillPalette extends CustomRecipe {
    public RecipeFillPalette(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    private boolean isPalette(ItemStack stack) {
        return stack.getItem() instanceof ItemPalette;
    }

    private boolean isDye(ItemStack stack) {
        if (stack.getItem() instanceof DyeItem dyeItem) {
            int colorId = dyeItem.getDyeColor().getId();
            return colorId >= 0 && colorId < 16;
        }
        return false;
    }

    private int findPalette(CraftingContainer inv) {
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (isPalette(stack)) return i;
        }
        return -1;
    }

    @Nullable
    private ArrayList<ItemStack> findDyes(CraftingContainer inv, int paletteId) {
        ArrayList<ItemStack> dyes = new ArrayList<>();
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            if (i == paletteId) continue;
            ItemStack stack = inv.getItem(i);
            if (isDye(stack)) dyes.add(stack);
            else if (!stack.isEmpty()) return null;
        }
        return dyes;
    }

    @Override
    public boolean matches(CraftingContainer inv, Level level) {
        int paletteId = findPalette(inv);
        if (paletteId < 0) return false;
        ArrayList<ItemStack> dyes = findDyes(inv, paletteId);
        return dyes != null && !dyes.isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
        int paletteId = findPalette(inv);
        if (paletteId < 0) return ItemStack.EMPTY;
        ArrayList<ItemStack> dyes = findDyes(inv, paletteId);
        if (dyes == null || dyes.isEmpty()) return ItemStack.EMPTY;

        ItemStack inputPalette = inv.getItem(paletteId);
        byte[] basicColors = NbtPalette.getBasic(inputPalette).clone();

        for (ItemStack dye : dyes) {
            DyeColor color = ((DyeItem) dye.getItem()).getDyeColor();
            int realColorId = 15 - color.getId();
            if (basicColors[realColorId] > 0) {
                return ItemStack.EMPTY;
            }
            basicColors[realColorId] = 1;
        }

        ItemStack result = new ItemStack(ModItems.ITEM_PALETTE.get());
        NbtPalette.setBasic(result, basicColors);
        return result;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        return NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.CRAFTING_SPECIAL_PALETTE_FILLING.get();
    }
}
