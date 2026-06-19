package com.leclowndu93150.joyofpainting.client;

import com.leclowndu93150.joyofpainting.JoyOfPainting;
import com.leclowndu93150.joyofpainting.client.gui.GuiCanvasEdit;
import com.leclowndu93150.joyofpainting.client.gui.GuiCanvasView;
import com.leclowndu93150.joyofpainting.client.gui.GuiPalette;
import com.leclowndu93150.joyofpainting.client.render.CanvasItemRenderer;
import com.leclowndu93150.joyofpainting.client.render.EaselModel;
import com.leclowndu93150.joyofpainting.client.render.RenderEntityCanvas;
import com.leclowndu93150.joyofpainting.client.render.RenderEntityEasel;
import com.leclowndu93150.joyofpainting.entity.EntityEasel;
import com.leclowndu93150.joyofpainting.item.ItemCanvas;
import com.leclowndu93150.joyofpainting.item.ItemPalette;
import com.leclowndu93150.joyofpainting.registry.ModDataComponents;
import com.leclowndu93150.joyofpainting.registry.ModEntities;
import com.leclowndu93150.joyofpainting.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = JoyOfPainting.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class JoyOfPaintingClient {
    public static final ModelLayerLocation EASEL_MAIN_LAYER = new ModelLayerLocation(JoyOfPainting.id("easel"), "main");
    public static final ModelLayerLocation EASEL_CANVAS_LAYER = new ModelLayerLocation(JoyOfPainting.id("easel"), "canvas");

    private JoyOfPaintingClient() {}

    public static void showCanvasGui(EntityEasel easel, ItemStack paletteStack) {
        showCanvasGui(easel, paletteStack, Minecraft.getInstance());
    }

    public static void showCanvasGui(EntityEasel easel, ItemStack paletteStack, Minecraft minecraft) {
        ItemStack canvasStack = easel.getItem();
        if (!(canvasStack.getItem() instanceof ItemCanvas canvasItem)) return;
        if (canvasStack.getOrDefault(ModDataComponents.CANVAS_GENERATION.get(), 0) > 0 || paletteStack.isEmpty()) {
            minecraft.setScreen(new GuiCanvasView(canvasStack,
                    Component.translatable("item.joyofpainting.item_canvas"),
                    canvasItem.getCanvasType(), easel));
        } else {
            minecraft.setScreen(new GuiCanvasEdit(minecraft.player, canvasStack, paletteStack,
                    Component.translatable("item.joyofpainting.item_canvas"),
                    canvasItem.getCanvasType(), easel));
        }
    }

    public static void showCanvasGui(Player player) {
        ItemStack heldItem = player.getMainHandItem();
        ItemStack offhandItem = player.getOffhandItem();
        Minecraft minecraft = Minecraft.getInstance();
        if (heldItem.isEmpty()) return;
        if (minecraft.player != null && !minecraft.player.getGameProfile().getId().equals(player.getGameProfile().getId())) return;

        if (heldItem.getItem() instanceof ItemCanvas itemCanvas) {
            int gen = heldItem.getOrDefault(ModDataComponents.CANVAS_GENERATION.get(), 0);
            if (offhandItem.isEmpty() || !(offhandItem.getItem() instanceof ItemPalette) || gen > 0) {
                minecraft.setScreen(new GuiCanvasView(heldItem, Component.translatable("item.joyofpainting.item_canvas"), itemCanvas.getCanvasType(), null));
            } else {
                minecraft.setScreen(new GuiCanvasEdit(minecraft.player, heldItem, offhandItem, Component.translatable("item.joyofpainting.item_canvas"), itemCanvas.getCanvasType(), null));
            }
        } else if (heldItem.getItem() instanceof ItemPalette) {
            if (offhandItem.isEmpty() || !(offhandItem.getItem() instanceof ItemCanvas offCanvas)) {
                minecraft.setScreen(new GuiPalette(heldItem, Component.translatable("item.joyofpainting.item_palette")));
            } else {
                int gen = offhandItem.getOrDefault(ModDataComponents.CANVAS_GENERATION.get(), 0);
                if (gen > 0) {
                    minecraft.setScreen(new GuiCanvasView(offhandItem, Component.translatable("item.joyofpainting.item_canvas"), offCanvas.getCanvasType(), null));
                } else {
                    minecraft.setScreen(new GuiCanvasEdit(minecraft.player, offhandItem, heldItem, Component.translatable("item.joyofpainting.item_canvas"), offCanvas.getCanvasType(), null));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ClampedItemPropertyFunction drawn = (stack, level, entity, seed) ->
                    stack.has(ModDataComponents.CANVAS_PIXELS.get()) ? 1.0F : 0.0F;
            ClampedItemPropertyFunction colors = (stack, level, entity, seed) ->
                    ItemPalette.basicColorCount(stack) / 16.0F;
            registerProperty(ModItems.ITEM_CANVAS.get(), "drawn", drawn);
            registerProperty(ModItems.ITEM_CANVAS_LARGE.get(), "drawn", drawn);
            registerProperty(ModItems.ITEM_CANVAS_LONG.get(), "drawn", drawn);
            registerProperty(ModItems.ITEM_CANVAS_TALL.get(), "drawn", drawn);
            registerProperty(ModItems.ITEM_CANVAS_GLASS.get(), "drawn", drawn);
            registerProperty(ModItems.ITEM_CANVAS_GLASS_LARGE.get(), "drawn", drawn);
            registerProperty(ModItems.ITEM_CANVAS_GLASS_LONG.get(), "drawn", drawn);
            registerProperty(ModItems.ITEM_CANVAS_GLASS_TALL.get(), "drawn", drawn);
            registerProperty(ModItems.ITEM_PALETTE.get(), "colors", colors);
        });
    }

    private static void registerProperty(Item item, String name, ClampedItemPropertyFunction fn) {
        ItemProperties.register(item, JoyOfPainting.id(name), fn);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.CANVAS.get(), RenderEntityCanvas::new);
        event.registerEntityRenderer(ModEntities.EASEL.get(), RenderEntityEasel::new);
    }

    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(EASEL_MAIN_LAYER, EaselModel::createBodyLayer);
        event.registerLayerDefinition(EASEL_CANVAS_LAYER, EaselModel::createBodyLayer);
    }

}
