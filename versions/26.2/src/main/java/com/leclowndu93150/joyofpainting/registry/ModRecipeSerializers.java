package com.leclowndu93150.joyofpainting.registry;

import com.leclowndu93150.joyofpainting.JoyOfPainting;
import com.leclowndu93150.joyofpainting.item.crafting.RecipeCanvasCloning;
import com.leclowndu93150.joyofpainting.item.crafting.RecipeFillPalette;
import com.leclowndu93150.joyofpainting.item.crafting.RecipeTaglessShaped;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, JoyOfPainting.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<RecipeFillPalette>> CRAFTING_SPECIAL_PALETTE_FILLING = SERIALIZERS.register(
            "crafting_special_palette_filling", () -> RecipeFillPalette.SERIALIZER);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<RecipeCanvasCloning>> CRAFTING_SPECIAL_CANVAS_CLONING = SERIALIZERS.register(
            "crafting_special_canvas_cloning", () -> RecipeCanvasCloning.SERIALIZER);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<RecipeTaglessShaped>> CRAFTING_TAGLESS_SHAPED = SERIALIZERS.register(
            "crafting_tagless_shaped", () -> RecipeTaglessShaped.SERIALIZER);

    private ModRecipeSerializers() {}

    public static void register(IEventBus bus) {
        SERIALIZERS.register(bus);
    }
}
