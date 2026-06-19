package com.leclowndu93150.joyofpainting.item.crafting;

import com.leclowndu93150.joyofpainting.item.ItemCanvas;
import com.leclowndu93150.joyofpainting.registry.ModDataComponents;
import com.leclowndu93150.joyofpainting.registry.ModRecipeSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.List;

public class RecipeCanvasCloning extends CustomRecipe {
    public RecipeCanvasCloning(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput inv, Level level) {
        ItemStack orgCanvas = ItemStack.EMPTY;
        ItemStack freshCanvas = ItemStack.EMPTY;

        for (int j = 0; j < inv.size(); ++j) {
            ItemStack stack = inv.getItem(j);
            if (stack.isEmpty()) continue;
            if (!(stack.getItem() instanceof ItemCanvas ic)) continue;
            int gen = stack.getOrDefault(ModDataComponents.CANVAS_GENERATION.get(), 0);
            if (gen > 0) {
                if (!orgCanvas.isEmpty()) return false;
                if (!freshCanvas.isEmpty() && !((ItemCanvas) freshCanvas.getItem()).getCanvasType().equals(ic.getCanvasType())) return false;
                orgCanvas = stack;
            } else if (stack.get(ModDataComponents.CANVAS_GENERATION.get()) == null) {
                if (!freshCanvas.isEmpty()) return false;
                if (!orgCanvas.isEmpty() && !((ItemCanvas) orgCanvas.getItem()).getCanvasType().equals(ic.getCanvasType())) return false;
                freshCanvas = stack;
            }
        }
        return !orgCanvas.isEmpty() && !freshCanvas.isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider lookup) {
        ItemStack orgCanvas = ItemStack.EMPTY;
        ItemStack freshCanvas = ItemStack.EMPTY;

        for (int j = 0; j < inv.size(); ++j) {
            ItemStack stack = inv.getItem(j);
            if (stack.isEmpty()) continue;
            if (!(stack.getItem() instanceof ItemCanvas ic)) continue;
            int gen = stack.getOrDefault(ModDataComponents.CANVAS_GENERATION.get(), 0);
            if (gen > 0) {
                if (!orgCanvas.isEmpty()) return ItemStack.EMPTY;
                if (!freshCanvas.isEmpty() && !((ItemCanvas) freshCanvas.getItem()).getCanvasType().equals(ic.getCanvasType())) return ItemStack.EMPTY;
                orgCanvas = stack;
            } else if (stack.get(ModDataComponents.CANVAS_GENERATION.get()) == null) {
                if (!freshCanvas.isEmpty()) return ItemStack.EMPTY;
                if (!orgCanvas.isEmpty() && !((ItemCanvas) orgCanvas.getItem()).getCanvasType().equals(ic.getCanvasType())) return ItemStack.EMPTY;
                freshCanvas = stack;
            }
        }

        int gen = orgCanvas.getOrDefault(ModDataComponents.CANVAS_GENERATION.get(), 0);
        if (orgCanvas.isEmpty() || freshCanvas.isEmpty() || gen <= 0 || gen >= 3) {
            return ItemStack.EMPTY;
        }
        ItemStack result = new ItemStack(orgCanvas.getItem());
        result.set(ModDataComponents.CANVAS_GENERATION.get(), gen + 1);
        List<Integer> pixels = orgCanvas.get(ModDataComponents.CANVAS_PIXELS.get());
        if (pixels != null) result.set(ModDataComponents.CANVAS_PIXELS.get(), pixels);
        String id = orgCanvas.get(ModDataComponents.CANVAS_ID.get());
        if (id != null) result.set(ModDataComponents.CANVAS_ID.get(), id);
        Integer version = orgCanvas.get(ModDataComponents.CANVAS_VERSION.get());
        if (version != null) result.set(ModDataComponents.CANVAS_VERSION.get(), version);
        String title = orgCanvas.get(ModDataComponents.CANVAS_TITLE.get());
        if (title != null) result.set(ModDataComponents.CANVAS_TITLE.get(), title);
        String author = orgCanvas.get(ModDataComponents.CANVAS_AUTHOR.get());
        if (author != null) result.set(ModDataComponents.CANVAS_AUTHOR.get(), author);
        ItemCanvas.updateStackSize(result);
        return result;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
        NonNullList<ItemStack> remainders = NonNullList.withSize(inv.size(), ItemStack.EMPTY);
        for (int i = 0; i < remainders.size(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() instanceof ItemCanvas && stack.getOrDefault(ModDataComponents.CANVAS_GENERATION.get(), 0) > 0) {
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
