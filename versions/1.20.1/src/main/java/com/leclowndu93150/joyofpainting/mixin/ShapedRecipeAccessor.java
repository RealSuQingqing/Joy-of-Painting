package com.leclowndu93150.joyofpainting.mixin;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ShapedRecipe.class)
public interface ShapedRecipeAccessor {
    @Accessor("width")
    int getWidth();

    @Accessor("height")
    int getHeight();

    @Accessor("recipeItems")
    NonNullList<Ingredient> getRecipeItems();

    @Accessor("result")
    ItemStack getResult();

    @Accessor("group")
    String getGroup();

    @Accessor("category")
    CraftingBookCategory getCategory();

    @Accessor("showNotification")
    boolean getShowNotification();
}
