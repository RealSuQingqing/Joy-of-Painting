package com.leclowndu93150.joyofpainting.item.crafting;

import com.leclowndu93150.joyofpainting.mixin.ShapedRecipeAccessor;
import com.leclowndu93150.joyofpainting.registry.ModDataComponents;
import com.leclowndu93150.joyofpainting.registry.ModRecipeSerializers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;

public class RecipeTaglessShaped extends ShapedRecipe {
    public RecipeTaglessShaped(String group, CraftingBookCategory category, ShapedRecipePattern pattern, ItemStack result, boolean showNotification) {
        super(group, category, pattern, result, showNotification);
    }

    public RecipeTaglessShaped(String group, CraftingBookCategory category, ShapedRecipePattern pattern, ItemStack result) {
        super(group, category, pattern, result);
    }

    @Override
    public boolean matches(CraftingInput inv, Level level) {
        if (!super.matches(inv, level)) return false;
        for (int j = 0; j < inv.size(); ++j) {
            ItemStack slot = inv.getItem(j);
            if (!slot.isEmpty() && slot.has(ModDataComponents.CANVAS_PIXELS.get())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.CRAFTING_TAGLESS_SHAPED.get();
    }

    public static class Serializer implements RecipeSerializer<RecipeTaglessShaped> {
        public static final MapCodec<RecipeTaglessShaped> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter(ShapedRecipe::getGroup),
                CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(ShapedRecipe::category),
                ShapedRecipePattern.MAP_CODEC.forGetter(r -> ((ShapedRecipeAccessor) r).jop$getPattern()),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(r -> ((ShapedRecipeAccessor) r).jop$getResult()),
                Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(ShapedRecipe::showNotification)
        ).apply(instance, RecipeTaglessShaped::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, RecipeTaglessShaped> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, ShapedRecipe::getGroup,
                CraftingBookCategory.STREAM_CODEC, ShapedRecipe::category,
                ShapedRecipePattern.STREAM_CODEC, r -> ((ShapedRecipeAccessor) r).jop$getPattern(),
                ItemStack.STREAM_CODEC, r -> ((ShapedRecipeAccessor) r).jop$getResult(),
                ByteBufCodecs.BOOL, ShapedRecipe::showNotification,
                RecipeTaglessShaped::new);

        @Override
        public MapCodec<RecipeTaglessShaped> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, RecipeTaglessShaped> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
