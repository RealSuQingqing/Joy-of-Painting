package com.leclowndu93150.joyofpainting.client;

import com.leclowndu93150.joyofpainting.JoyOfPainting;
import com.leclowndu93150.joyofpainting.client.gui.GuiCanvasEdit;
import com.leclowndu93150.joyofpainting.client.gui.GuiCanvasView;
import com.leclowndu93150.joyofpainting.client.gui.GuiPalette;
import com.leclowndu93150.joyofpainting.client.item.PaletteColorCountProperty;
import com.leclowndu93150.joyofpainting.client.render.CanvasSpecialRenderer;
import com.leclowndu93150.joyofpainting.client.render.EaselModel;
import com.leclowndu93150.joyofpainting.client.render.RenderEntityCanvas;
import com.leclowndu93150.joyofpainting.client.render.RenderEntityEasel;
import com.leclowndu93150.joyofpainting.entity.EntityEasel;
import com.leclowndu93150.joyofpainting.item.ItemCanvas;
import com.leclowndu93150.joyofpainting.item.ItemPalette;
import com.leclowndu93150.joyofpainting.registry.ModDataComponents;
import com.leclowndu93150.joyofpainting.registry.ModEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterRangeSelectItemModelPropertyEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;

@EventBusSubscriber(modid = JoyOfPainting.MODID, value = Dist.CLIENT)
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
        if (minecraft.player != null && !minecraft.player.getGameProfile().id().equals(player.getGameProfile().id())) return;

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
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.CANVAS.get(), RenderEntityCanvas::new);
        event.registerEntityRenderer(ModEntities.EASEL.get(), RenderEntityEasel::new);
    }

    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(EASEL_MAIN_LAYER, EaselModel::createBodyLayer);
        event.registerLayerDefinition(EASEL_CANVAS_LAYER, EaselModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void onRegisterRangeSelectProperties(RegisterRangeSelectItemModelPropertyEvent event) {
        event.register(JoyOfPainting.id("palette_color_count"), PaletteColorCountProperty.MAP_CODEC);
    }

    @SubscribeEvent
    public static void onRegisterSpecialModelRenderers(RegisterSpecialModelRendererEvent event) {
        event.register(JoyOfPainting.id("canvas"), CanvasSpecialRenderer.Unbaked.MAP_CODEC);
    }

}
