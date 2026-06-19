package com.leclowndu93150.joyofpainting.nbt;

import com.leclowndu93150.joyofpainting.PaletteUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public final class NbtPalette {
    public static final String KEY_BASIC = "basic";
    public static final String KEY_R = "r";
    public static final String KEY_G = "g";
    public static final String KEY_B = "b";
    public static final String KEY_M = "m";
    public static final String KEY_N = "n";

    public static final int BASIC_SIZE = 16;
    public static final int CUSTOM_SIZE = 12;

    private NbtPalette() {}

    public static byte[] getBasic(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(KEY_BASIC, Tag.TAG_BYTE_ARRAY)) return new byte[BASIC_SIZE];
        byte[] raw = tag.getByteArray(KEY_BASIC);
        if (raw.length == BASIC_SIZE) return raw;
        byte[] out = new byte[BASIC_SIZE];
        System.arraycopy(raw, 0, out, 0, Math.min(raw.length, BASIC_SIZE));
        return out;
    }

    public static void setBasic(ItemStack stack, byte[] basic) {
        stack.getOrCreateTag().putByteArray(KEY_BASIC, basic);
    }

    public static int countColors(ItemStack stack) {
        byte[] basic = getBasic(stack);
        int n = 0;
        for (byte b : basic) if (b != 0) n++;
        return n;
    }

    public static boolean isFull(ItemStack stack) {
        return countColors(stack) == BASIC_SIZE;
    }

    public static PaletteUtil.CustomColor[] getCustomColors(ItemStack stack) {
        PaletteUtil.CustomColor[] out = new PaletteUtil.CustomColor[CUSTOM_SIZE];
        CompoundTag tag = stack.getTag();
        if (tag == null
                || !tag.contains(KEY_R, Tag.TAG_INT_ARRAY)
                || !tag.contains(KEY_G, Tag.TAG_INT_ARRAY)
                || !tag.contains(KEY_B, Tag.TAG_INT_ARRAY)
                || !tag.contains(KEY_M, Tag.TAG_INT_ARRAY)
                || !tag.contains(KEY_N, Tag.TAG_INT_ARRAY)) {
            for (int i = 0; i < CUSTOM_SIZE; i++) out[i] = new PaletteUtil.CustomColor();
            return out;
        }
        int[] r = tag.getIntArray(KEY_R);
        int[] g = tag.getIntArray(KEY_G);
        int[] b = tag.getIntArray(KEY_B);
        int[] m = tag.getIntArray(KEY_M);
        int[] n = tag.getIntArray(KEY_N);
        for (int i = 0; i < CUSTOM_SIZE; i++) {
            if (i < r.length && i < g.length && i < b.length && i < m.length && i < n.length) {
                out[i] = new PaletteUtil.CustomColor(r[i], g[i], b[i], m[i], n[i]);
            } else {
                out[i] = new PaletteUtil.CustomColor();
            }
        }
        return out;
    }

    public static void setCustomColors(ItemStack stack, PaletteUtil.CustomColor[] colors) {
        int[] r = new int[CUSTOM_SIZE];
        int[] g = new int[CUSTOM_SIZE];
        int[] b = new int[CUSTOM_SIZE];
        int[] m = new int[CUSTOM_SIZE];
        int[] n = new int[CUSTOM_SIZE];
        for (int i = 0; i < CUSTOM_SIZE && i < colors.length; i++) {
            PaletteUtil.CustomColor c = colors[i];
            if (c == null) continue;
            r[i] = c.totalRed;
            g[i] = c.totalGreen;
            b[i] = c.totalBlue;
            m[i] = c.totalMaximum;
            n[i] = c.numberOfColors;
        }
        CompoundTag tag = stack.getOrCreateTag();
        tag.putIntArray(KEY_R, r);
        tag.putIntArray(KEY_G, g);
        tag.putIntArray(KEY_B, b);
        tag.putIntArray(KEY_M, m);
        tag.putIntArray(KEY_N, n);
    }

    public static boolean hasCustomColors(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(KEY_R, Tag.TAG_INT_ARRAY);
    }
}
