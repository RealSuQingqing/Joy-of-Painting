package com.leclowndu93150.joyofpainting.registry;

import com.leclowndu93150.joyofpainting.JoyOfPainting;
import com.leclowndu93150.joyofpainting.PaletteUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public final class ModDataComponents {
    public static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, JoyOfPainting.MODID);

    public static final Supplier<DataComponentType<List<Integer>>> CANVAS_PIXELS = COMPONENTS.registerComponentType(
            "canvas_pixels", b -> b
                    .persistent(Codec.list(Codec.INT))
                    .networkSynchronized(intListStreamCodec()));

    public static final Supplier<DataComponentType<Integer>> CANVAS_VERSION = COMPONENTS.registerComponentType(
            "canvas_version", b -> b.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final Supplier<DataComponentType<String>> CANVAS_ID = COMPONENTS.registerComponentType(
            "canvas_id", b -> b.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));

    public static final Supplier<DataComponentType<String>> CANVAS_TITLE = COMPONENTS.registerComponentType(
            "canvas_title", b -> b.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));

    public static final Supplier<DataComponentType<String>> CANVAS_AUTHOR = COMPONENTS.registerComponentType(
            "canvas_author", b -> b.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));

    public static final Supplier<DataComponentType<Integer>> CANVAS_GENERATION = COMPONENTS.registerComponentType(
            "canvas_generation", b -> b.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final Supplier<DataComponentType<Boolean>> CANVAS_SIDES_ACTIVE = COMPONENTS.registerComponentType(
            "canvas_sides_active", b -> b.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    public static final Supplier<DataComponentType<List<Integer>>> CANVAS_SIDE_PIXELS = COMPONENTS.registerComponentType(
            "canvas_side_pixels", b -> b.persistent(Codec.list(Codec.INT)).networkSynchronized(intListStreamCodec()));

    public static final Supplier<DataComponentType<PaletteBasicColors>> PALETTE_BASIC_COLORS = COMPONENTS.registerComponentType(
            "palette_basic_colors", b -> b
                    .persistent(PaletteBasicColors.CODEC)
                    .networkSynchronized(PaletteBasicColors.STREAM_CODEC));

    public static final Supplier<DataComponentType<PaletteCustomColors>> PALETTE_CUSTOM_COLORS = COMPONENTS.registerComponentType(
            "palette_custom_colors", b -> b
                    .persistent(PaletteCustomColors.CODEC)
                    .networkSynchronized(PaletteCustomColors.STREAM_CODEC));

    private ModDataComponents() {}

    public static void register(IEventBus bus) {
        COMPONENTS.register(bus);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <B extends ByteBuf> StreamCodec<B, List<Integer>> intListStreamCodec() {
        StreamCodec codec = ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list());
        return (StreamCodec<B, List<Integer>>) codec;
    }

    public record PaletteBasicColors(byte[] colors) {
        public static final int SIZE = 16;

        public PaletteBasicColors {
            if (colors.length != SIZE) {
                byte[] resized = new byte[SIZE];
                System.arraycopy(colors, 0, resized, 0, Math.min(colors.length, SIZE));
                colors = resized;
            }
        }

        public static final Codec<PaletteBasicColors> CODEC = Codec.BYTE_BUFFER.xmap(
                bb -> new PaletteBasicColors(bb.array()),
                v -> ByteBuffer.wrap(v.colors));

        public static final StreamCodec<RegistryFriendlyByteBuf, PaletteBasicColors> STREAM_CODEC =
                StreamCodec.of(
                        (buf, v) -> buf.writeByteArray(v.colors),
                        buf -> new PaletteBasicColors(buf.readByteArray()));

        public int count() {
            int n = 0;
            for (byte b : colors) if (b != 0) n++;
            return n;
        }

        public boolean isFull() {
            return count() == SIZE;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            return o instanceof PaletteBasicColors other && Arrays.equals(colors, other.colors);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(colors);
        }
    }

    public record PaletteCustomColors(PaletteUtil.CustomColor[] colors) {
        public static final int COUNT = 12;

        public PaletteCustomColors {
            if (colors.length != COUNT) {
                throw new IllegalArgumentException("PaletteCustomColors must have exactly " + COUNT + " entries");
            }
        }

        public static final Codec<PaletteCustomColors> CODEC = Codec.list(Codec.INT).flatXmap(
                list -> {
                    if (list.size() != COUNT * 5) {
                        return DataResult.error(() -> "Expected " + (COUNT * 5) + " ints, got " + list.size());
                    }
                    PaletteUtil.CustomColor[] arr = new PaletteUtil.CustomColor[COUNT];
                    for (int i = 0; i < COUNT; i++) {
                        int base = i * 5;
                        arr[i] = new PaletteUtil.CustomColor(list.get(base), list.get(base + 1), list.get(base + 2), list.get(base + 3), list.get(base + 4));
                    }
                    return DataResult.success(new PaletteCustomColors(arr));
                },
                value -> {
                    List<Integer> list = new ArrayList<>(COUNT * 5);
                    for (PaletteUtil.CustomColor c : value.colors) {
                        list.add(c.totalRed);
                        list.add(c.totalGreen);
                        list.add(c.totalBlue);
                        list.add(c.totalMaximum);
                        list.add(c.numberOfColors);
                    }
                    return DataResult.success(list);
                });

        public static final StreamCodec<RegistryFriendlyByteBuf, PaletteCustomColors> STREAM_CODEC =
                StreamCodec.of(
                        (buf, value) -> {
                            for (PaletteUtil.CustomColor c : value.colors) {
                                c.writeToBuffer(buf);
                            }
                        },
                        buf -> {
                            PaletteUtil.CustomColor[] arr = new PaletteUtil.CustomColor[COUNT];
                            for (int i = 0; i < COUNT; i++) arr[i] = new PaletteUtil.CustomColor(buf);
                            return new PaletteCustomColors(arr);
                        });

        public static PaletteCustomColors empty() {
            PaletteUtil.CustomColor[] arr = new PaletteUtil.CustomColor[COUNT];
            for (int i = 0; i < COUNT; i++) arr[i] = new PaletteUtil.CustomColor();
            return new PaletteCustomColors(arr);
        }

        public int filledCount() {
            int n = 0;
            for (PaletteUtil.CustomColor c : colors) {
                if (c.numberOfColors > 0) n++;
            }
            return n;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PaletteCustomColors other)) return false;
            return Arrays.equals(colors, other.colors);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(colors);
        }
    }
}
