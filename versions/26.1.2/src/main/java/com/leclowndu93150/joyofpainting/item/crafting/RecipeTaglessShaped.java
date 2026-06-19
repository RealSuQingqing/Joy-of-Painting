package com.leclowndu93150.joyofpainting.item.crafting;

import com.leclowndu93150.joyofpainting.registry.ModDataComponents;
import com.leclowndu93150.joyofpainting.registry.ModRecipeSerializers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;

public class RecipeTaglessShaped extends ShapedRecipe {
    private final ShapedRecipePattern pattern;
    private final ItemStackTemplate result;

    public RecipeTaglessShaped(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo, ShapedRecipePattern pattern, ItemStackTemplate result) {
        super(commonInfo, bookInfo, pattern, result);
        this.pattern = pattern;
        this.result = result;
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
    public RecipeSerializer<ShapedRecipe> getSerializer() {
        return (RecipeSerializer<ShapedRecipe>) (RecipeSerializer<?>) ModRecipeSerializers.CRAFTING_TAGLESS_SHAPED.get();
    }

    public static final MapCodec<RecipeTaglessShaped> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Recipe.CommonInfo.MAP_CODEC.forGetter(r -> r.commonInfo),
            CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(r -> r.bookInfo),
            ShapedRecipePattern.MAP_CODEC.forGetter(r -> r.pattern),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.result)
    ).apply(i, RecipeTaglessShaped::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, RecipeTaglessShaped> STREAM_CODEC = StreamCodec.composite(
            Recipe.CommonInfo.STREAM_CODEC, r -> r.commonInfo,
            CraftingRecipe.CraftingBookInfo.STREAM_CODEC, r -> r.bookInfo,
            ShapedRecipePattern.STREAM_CODEC, r -> r.pattern,
            ItemStackTemplate.STREAM_CODEC, r -> r.result,
            RecipeTaglessShaped::new);

    public static final RecipeSerializer<RecipeTaglessShaped> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);
}
