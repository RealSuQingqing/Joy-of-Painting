package com.leclowndu93150.joyofpainting.nbt;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public final class NbtCanvas {
    public static final String KEY_PIXELS = "pixels";
    public static final String KEY_NAME = "name";
    public static final String KEY_VERSION = "v";
    public static final String KEY_TITLE = "title";
    public static final String KEY_AUTHOR = "author";
    public static final String KEY_GENERATION = "generation";
    public static final String KEY_SIDES_ACTIVE = "sidesActive";
    public static final String KEY_SIDE_PIXELS = "sidePixels";

    private NbtCanvas() {}

    public static boolean getSidesActive(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(KEY_SIDES_ACTIVE);
    }

    public static void setSidesActive(ItemStack stack, boolean active) {
        stack.getOrCreateTag().putBoolean(KEY_SIDES_ACTIVE, active);
    }

    public static int[] getSidePixels(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? new int[0] : tag.getIntArray(KEY_SIDE_PIXELS);
    }

    public static void setSidePixels(ItemStack stack, int[] pixels) {
        stack.getOrCreateTag().putIntArray(KEY_SIDE_PIXELS, pixels);
    }

    public static boolean hasSidePixels(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(KEY_SIDE_PIXELS, Tag.TAG_INT_ARRAY);
    }

    public static boolean hasPixels(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(KEY_PIXELS, Tag.TAG_INT_ARRAY);
    }

    public static int[] getPixels(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? new int[0] : tag.getIntArray(KEY_PIXELS);
    }

    public static void setPixels(ItemStack stack, int[] pixels) {
        stack.getOrCreateTag().putIntArray(KEY_PIXELS, pixels);
    }

    public static String getName(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? "" : tag.getString(KEY_NAME);
    }

    public static void setName(ItemStack stack, String name) {
        stack.getOrCreateTag().putString(KEY_NAME, name);
    }

    public static int getVersion(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(KEY_VERSION);
    }

    public static void setVersion(ItemStack stack, int version) {
        stack.getOrCreateTag().putInt(KEY_VERSION, version);
    }

    public static String getTitle(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? "" : tag.getString(KEY_TITLE);
    }

    public static void setTitle(ItemStack stack, String title) {
        stack.getOrCreateTag().putString(KEY_TITLE, title);
    }

    public static boolean hasTitle(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(KEY_TITLE, Tag.TAG_STRING) && !tag.getString(KEY_TITLE).isEmpty();
    }

    public static String getAuthor(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? "" : tag.getString(KEY_AUTHOR);
    }

    public static void setAuthor(ItemStack stack, String author) {
        stack.getOrCreateTag().putString(KEY_AUTHOR, author);
    }

    public static int getGeneration(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(KEY_GENERATION);
    }

    public static void setGeneration(ItemStack stack, int generation) {
        stack.getOrCreateTag().putInt(KEY_GENERATION, generation);
    }

    public static boolean hasGeneration(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(KEY_GENERATION, Tag.TAG_INT);
    }
}
