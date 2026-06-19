package com.leclowndu93150.joyofpainting.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ShapedRecipe.class)
public interface ShapedRecipeAccessor {
    @Accessor("pattern")
    ShapedRecipePattern jop$getPattern();

    @Accessor("result")
    ItemStack jop$getResult();
}
