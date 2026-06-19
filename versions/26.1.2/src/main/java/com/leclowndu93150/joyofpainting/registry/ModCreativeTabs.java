package com.leclowndu93150.joyofpainting.registry;

import com.leclowndu93150.joyofpainting.JoyOfPainting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Arrays;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, JoyOfPainting.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> PAINT_TAB = TABS.register("paint_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.joyofpainting.paint_tab"))
                    .icon(() -> new ItemStack(ModItems.ITEM_PALETTE.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.ITEM_PALETTE.get());

                        ItemStack fullPalette = new ItemStack(ModItems.ITEM_PALETTE.get());
                        byte[] basic = new byte[16];
                        Arrays.fill(basic, (byte) 1);
                        fullPalette.set(ModDataComponents.PALETTE_BASIC_COLORS.get(), new ModDataComponents.PaletteBasicColors(basic));
                        output.accept(fullPalette);

                        output.accept(ModItems.ITEM_CANVAS.get());
                        output.accept(ModItems.ITEM_CANVAS_LONG.get());
                        output.accept(ModItems.ITEM_CANVAS_TALL.get());
                        output.accept(ModItems.ITEM_CANVAS_LARGE.get());
                        output.accept(ModItems.ITEM_CANVAS_GLASS.get());
                        output.accept(ModItems.ITEM_CANVAS_GLASS_LONG.get());
                        output.accept(ModItems.ITEM_CANVAS_GLASS_TALL.get());
                        output.accept(ModItems.ITEM_CANVAS_GLASS_LARGE.get());
                        output.accept(ModItems.ITEM_EASEL.get());
                    })
                    .build());

    private ModCreativeTabs() {}

    public static void register(IEventBus bus) {
        TABS.register(bus);
    }
}
