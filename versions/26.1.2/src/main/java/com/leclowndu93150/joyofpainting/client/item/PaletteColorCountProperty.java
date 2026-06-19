package com.leclowndu93150.joyofpainting.client.item;

import com.leclowndu93150.joyofpainting.item.ItemPalette;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record PaletteColorCountProperty() implements RangeSelectItemModelProperty {
    public static final PaletteColorCountProperty INSTANCE = new PaletteColorCountProperty();
    public static final MapCodec<PaletteColorCountProperty> MAP_CODEC = MapCodec.unit(INSTANCE);

    @Override
    public float get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        return ItemPalette.basicColorCount(itemStack) / 16.0F;
    }

    @Override
    public MapCodec<PaletteColorCountProperty> type() {
        return MAP_CODEC;
    }
}
