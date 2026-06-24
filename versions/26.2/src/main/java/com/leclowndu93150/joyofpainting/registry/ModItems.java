package com.leclowndu93150.joyofpainting.registry;

import com.leclowndu93150.joyofpainting.CanvasType;
import com.leclowndu93150.joyofpainting.JoyOfPainting;
import com.leclowndu93150.joyofpainting.item.ItemCanvas;
import com.leclowndu93150.joyofpainting.item.ItemEasel;
import com.leclowndu93150.joyofpainting.item.ItemPalette;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(JoyOfPainting.MODID);

    public static final DeferredItem<ItemPalette> ITEM_PALETTE = ITEMS.registerItem("item_palette",
            props -> new ItemPalette(props.stacksTo(1)));
    public static final DeferredItem<ItemCanvas> ITEM_CANVAS = ITEMS.registerItem("item_canvas",
            props -> new ItemCanvas(CanvasType.SMALL, false, props.stacksTo(1)));
    public static final DeferredItem<ItemCanvas> ITEM_CANVAS_LARGE = ITEMS.registerItem("item_canvas_large",
            props -> new ItemCanvas(CanvasType.LARGE, false, props.stacksTo(1)));
    public static final DeferredItem<ItemCanvas> ITEM_CANVAS_LONG = ITEMS.registerItem("item_canvas_long",
            props -> new ItemCanvas(CanvasType.LONG, false, props.stacksTo(1)));
    public static final DeferredItem<ItemCanvas> ITEM_CANVAS_TALL = ITEMS.registerItem("item_canvas_tall",
            props -> new ItemCanvas(CanvasType.TALL, false, props.stacksTo(1)));
    public static final DeferredItem<ItemCanvas> ITEM_CANVAS_GLASS = ITEMS.registerItem("item_canvas_glass",
            props -> new ItemCanvas(CanvasType.SMALL, true, props.stacksTo(1)));
    public static final DeferredItem<ItemCanvas> ITEM_CANVAS_GLASS_LARGE = ITEMS.registerItem("item_canvas_glass_large",
            props -> new ItemCanvas(CanvasType.LARGE, true, props.stacksTo(1)));
    public static final DeferredItem<ItemCanvas> ITEM_CANVAS_GLASS_LONG = ITEMS.registerItem("item_canvas_glass_long",
            props -> new ItemCanvas(CanvasType.LONG, true, props.stacksTo(1)));
    public static final DeferredItem<ItemCanvas> ITEM_CANVAS_GLASS_TALL = ITEMS.registerItem("item_canvas_glass_tall",
            props -> new ItemCanvas(CanvasType.TALL, true, props.stacksTo(1)));
    public static final DeferredItem<ItemEasel> ITEM_EASEL = ITEMS.registerItem("item_easel",
            props -> new ItemEasel(props.stacksTo(1)));

    private ModItems() {}

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
