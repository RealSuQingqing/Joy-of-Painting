package com.leclowndu93150.joyofpainting.item.crafting;

import com.google.gson.JsonObject;
import com.leclowndu93150.joyofpainting.mixin.ShapedRecipeAccessor;
import com.leclowndu93150.joyofpainting.nbt.NbtCanvas;
import com.leclowndu93150.joyofpainting.registry.ModRecipeSerializers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

public class RecipeTaglessShaped extends ShapedRecipe {
    public RecipeTaglessShaped(ShapedRecipe delegate) {
        super(delegate.getId(),
                ((ShapedRecipeAccessor) delegate).getGroup(),
                ((ShapedRecipeAccessor) delegate).getCategory(),
                ((ShapedRecipeAccessor) delegate).getWidth(),
                ((ShapedRecipeAccessor) delegate).getHeight(),
                ((ShapedRecipeAccessor) delegate).getRecipeItems(),
                ((ShapedRecipeAccessor) delegate).getResult(),
                ((ShapedRecipeAccessor) delegate).getShowNotification());
    }

    @Override
    public boolean matches(CraftingContainer inv, Level level) {
        if (!super.matches(inv, level)) return false;
        for (int j = 0; j < inv.getContainerSize(); ++j) {
            ItemStack slot = inv.getItem(j);
            if (!slot.isEmpty() && NbtCanvas.hasPixels(slot)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
        ItemStack result = super.assemble(inv, registryAccess);
        if (result.isEmpty()) return ItemStack.EMPTY;
        for (int j = 0; j < inv.getContainerSize(); ++j) {
            ItemStack slot = inv.getItem(j);
            if (!slot.isEmpty() && NbtCanvas.hasPixels(slot)) {
                return ItemStack.EMPTY;
            }
        }
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.CRAFTING_TAGLESS_SHAPED.get();
    }

    public static class Serializer implements RecipeSerializer<RecipeTaglessShaped> {
        @Override
        public RecipeTaglessShaped fromJson(ResourceLocation id, JsonObject json) {
            ShapedRecipe delegate = RecipeSerializer.SHAPED_RECIPE.fromJson(id, json);
            return new RecipeTaglessShaped(delegate);
        }

        @Override
        public RecipeTaglessShaped fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            ShapedRecipe delegate = RecipeSerializer.SHAPED_RECIPE.fromNetwork(id, buf);
            return new RecipeTaglessShaped(delegate);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, RecipeTaglessShaped recipe) {
            RecipeSerializer.SHAPED_RECIPE.toNetwork(buf, recipe);
        }
    }
}
