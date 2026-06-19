package com.leclowndu93150.joyofpainting.registry;

import com.leclowndu93150.joyofpainting.JoyOfPainting;
import com.leclowndu93150.joyofpainting.item.crafting.RecipeCanvasCloning;
import com.leclowndu93150.joyofpainting.item.crafting.RecipeFillPalette;
import com.leclowndu93150.joyofpainting.item.crafting.RecipeTaglessShaped;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, JoyOfPainting.MODID);

    public static final RegistryObject<RecipeSerializer<RecipeFillPalette>> CRAFTING_SPECIAL_PALETTE_FILLING = SERIALIZERS.register(
            "crafting_special_palette_filling", () -> new SimpleCraftingRecipeSerializer<>(RecipeFillPalette::new));
    public static final RegistryObject<RecipeSerializer<RecipeCanvasCloning>> CRAFTING_SPECIAL_CANVAS_CLONING = SERIALIZERS.register(
            "crafting_special_canvas_cloning", () -> new SimpleCraftingRecipeSerializer<>(RecipeCanvasCloning::new));
    public static final RegistryObject<RecipeSerializer<RecipeTaglessShaped>> CRAFTING_TAGLESS_SHAPED = SERIALIZERS.register(
            "crafting_tagless_shaped", RecipeTaglessShaped.Serializer::new);

    private ModRecipeSerializers() {}

    public static void register(IEventBus bus) {
        SERIALIZERS.register(bus);
    }
}
