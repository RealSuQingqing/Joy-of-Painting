package com.leclowndu93150.joyofpainting.item.crafting;

import com.leclowndu93150.joyofpainting.item.ItemCanvas;
import com.leclowndu93150.joyofpainting.nbt.NbtCanvas;
import com.leclowndu93150.joyofpainting.registry.ModRecipeSerializers;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class RecipeCanvasCloning extends CustomRecipe {
    public RecipeCanvasCloning(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer inv, Level level) {
        ItemStack orgCanvas = ItemStack.EMPTY;
        ItemStack freshCanvas = ItemStack.EMPTY;

        for (int j = 0; j < inv.getContainerSize(); ++j) {
            ItemStack stack = inv.getItem(j);
            if (stack.isEmpty()) continue;
            if (!(stack.getItem() instanceof ItemCanvas itemCanvas)) continue;
            if (NbtCanvas.getGeneration(stack) > 0) {
                if (!orgCanvas.isEmpty()) return false;
                if (!freshCanvas.isEmpty() && !((ItemCanvas) freshCanvas.getItem()).getCanvasType().equals(itemCanvas.getCanvasType())) return false;
                orgCanvas = stack;
            } else if (!NbtCanvas.hasGeneration(stack)) {
                if (!freshCanvas.isEmpty()) return false;
                if (!orgCanvas.isEmpty() && !((ItemCanvas) orgCanvas.getItem()).getCanvasType().equals(itemCanvas.getCanvasType())) return false;
                freshCanvas = stack;
            }
        }
        return !orgCanvas.isEmpty() && !freshCanvas.isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
        ItemStack orgCanvas = ItemStack.EMPTY;
        ItemStack freshCanvas = ItemStack.EMPTY;

        for (int j = 0; j < inv.getContainerSize(); ++j) {
            ItemStack stack = inv.getItem(j);
            if (stack.isEmpty()) continue;
            if (!(stack.getItem() instanceof ItemCanvas itemCanvas)) continue;
            if (NbtCanvas.getGeneration(stack) > 0) {
                if (!orgCanvas.isEmpty()) return ItemStack.EMPTY;
                if (!freshCanvas.isEmpty() && !((ItemCanvas) freshCanvas.getItem()).getCanvasType().equals(itemCanvas.getCanvasType())) return ItemStack.EMPTY;
                orgCanvas = stack;
            } else if (!NbtCanvas.hasGeneration(stack)) {
                if (!freshCanvas.isEmpty()) return ItemStack.EMPTY;
                if (!orgCanvas.isEmpty() && !((ItemCanvas) orgCanvas.getItem()).getCanvasType().equals(itemCanvas.getCanvasType())) return ItemStack.EMPTY;
                freshCanvas = stack;
            }
        }

        int gen = NbtCanvas.getGeneration(orgCanvas);
        if (!orgCanvas.isEmpty() && !freshCanvas.isEmpty() && gen > 0 && gen < 3) {
            ItemStack result = new ItemStack(orgCanvas.getItem());
            NbtCanvas.setGeneration(result, gen + 1);
            NbtCanvas.setPixels(result, NbtCanvas.getPixels(orgCanvas));
            NbtCanvas.setName(result, NbtCanvas.getName(orgCanvas));
            NbtCanvas.setVersion(result, NbtCanvas.getVersion(orgCanvas));
            NbtCanvas.setTitle(result, NbtCanvas.getTitle(orgCanvas));
            NbtCanvas.setAuthor(result, NbtCanvas.getAuthor(orgCanvas));
            return result;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        NonNullList<ItemStack> remainders = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < remainders.size(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() instanceof ItemCanvas && NbtCanvas.getGeneration(stack) > 0) {
                ItemStack copy = stack.copy();
                copy.setCount(1);
                remainders.set(i, copy);
                break;
            }
        }
        return remainders;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.CRAFTING_SPECIAL_CANVAS_CLONING.get();
    }
}
