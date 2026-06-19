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

    public static final DeferredItem<ItemPalette> ITEM_PALETTE = ITEMS.register("item_palette",
            () -> new ItemPalette(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<ItemCanvas> ITEM_CANVAS = ITEMS.register("item_canvas",
            () -> new ItemCanvas(CanvasType.SMALL, false, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<ItemCanvas> ITEM_CANVAS_LARGE = ITEMS.register("item_canvas_large",
            () -> new ItemCanvas(CanvasType.LARGE, false, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<ItemCanvas> ITEM_CANVAS_LONG = ITEMS.register("item_canvas_long",
            () -> new ItemCanvas(CanvasType.LONG, false, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<ItemCanvas> ITEM_CANVAS_TALL = ITEMS.register("item_canvas_tall",
            () -> new ItemCanvas(CanvasType.TALL, false, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<ItemCanvas> ITEM_CANVAS_GLASS = ITEMS.register("item_canvas_glass",
            () -> new ItemCanvas(CanvasType.SMALL, true, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<ItemCanvas> ITEM_CANVAS_GLASS_LARGE = ITEMS.register("item_canvas_glass_large",
            () -> new ItemCanvas(CanvasType.LARGE, true, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<ItemCanvas> ITEM_CANVAS_GLASS_LONG = ITEMS.register("item_canvas_glass_long",
            () -> new ItemCanvas(CanvasType.LONG, true, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<ItemCanvas> ITEM_CANVAS_GLASS_TALL = ITEMS.register("item_canvas_glass_tall",
            () -> new ItemCanvas(CanvasType.TALL, true, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<ItemEasel> ITEM_EASEL = ITEMS.register("item_easel",
            () -> new ItemEasel(new Item.Properties().stacksTo(1)));

    private ModItems() {}

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
