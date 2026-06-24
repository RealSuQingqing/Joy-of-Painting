package com.leclowndu93150.joyofpainting.client.gui;

import com.leclowndu93150.joyofpainting.JoyOfPainting;
import com.leclowndu93150.joyofpainting.PaletteUtil;
import com.leclowndu93150.joyofpainting.registry.ModDataComponents;
import com.leclowndu93150.joyofpainting.registry.ModDataComponents.PaletteCustomColors;
import com.leclowndu93150.joyofpainting.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;

import static com.leclowndu93150.joyofpainting.PaletteUtil.EMPTINESS_COLOR;

public abstract class BasePalette extends Screen {
    protected static final Identifier PALETTE_TEXTURES = JoyOfPainting.id("textures/gui/palette.png");
    static final int DYE_SPRITE_X = 240;
    static final int DYE_SPRITE_SIZE = 16;
    static final int BRUSH_SPRITE_X = 0;
    static final int BRUSH_SPRITE_Y = 247;
    static final int BRUSH_SPRITE_SIZE = 9;
    static final int BRUSH_OPACITY_SPRITE_X = 196;
    static final int BRUSH_OPACITY_SPRITE_Y = 197;
    static final int BRUSH_OPACITY_SPRITE_SIZE = 14;
    static final int DROP_SPRITE_WIDTH = 6;
    static final int PALETTE_WIDTH = 157;
    static final int PALETTE_HEIGHT = 193;
    static final int COLOR_PICKER_SPRITE_X = 25;
    static final int COLOR_PICKER_SPRITE_Y = 242;
    static final int COLOR_PICKER_POS_X = 98;
    static final int COLOR_PICKER_POS_Y = 62;
    static final int COLOR_PICKER_SIZE = 14;

    static final double[] PALETTE_XS = {-1000, -1000, -1000, -1000, -1000};
    static final double[] PALETTE_YS = {-1000, -1000, -1000, -1000, -1000};
    double paletteX;
    double paletteY;
    static final PaletteUtil.Color WATER_COLOR = new PaletteUtil.Color(53, 118, 191);

    static final PaletteUtil.Color[] BASIC_COLORS = {
            new PaletteUtil.Color(0xFF1D1D21), new PaletteUtil.Color(0xFFB02E26), new PaletteUtil.Color(0xFF5E7C16),
            new PaletteUtil.Color(0xFF835432), new PaletteUtil.Color(0xFF3C44AA), new PaletteUtil.Color(0xFF8932B8),
            new PaletteUtil.Color(0xFF169C9C), new PaletteUtil.Color(0xFF9D9D97), new PaletteUtil.Color(0xFF474F52),
            new PaletteUtil.Color(0xFFF38BAA), new PaletteUtil.Color(0xFF80C71F), new PaletteUtil.Color(0xFFFED83D),
            new PaletteUtil.Color(0xFF3AB3DA), new PaletteUtil.Color(0xFFC74EBD), new PaletteUtil.Color(0xFFF9801D),
            new PaletteUtil.Color(0xFFF9FFFE)
    };
    static final Vec2[] BASIC_COLOR_CENTERS = {
            new Vec2(23.5f, 172.5f), new Vec2(18.5f, 145.5f), new Vec2(16.5f, 117.5f), new Vec2(17.5f, 89.5f),
            new Vec2(23.5f, 62.5f), new Vec2(38.5f, 39.5f), new Vec2(61.5f, 24.5f), new Vec2(87.5f, 17.5f),
            new Vec2(114.5f, 15.5f), new Vec2(44.5f, 154.5f), new Vec2(41.5f, 127.5f), new Vec2(42.5f, 100.5f),
            new Vec2(48.5f, 74.5f), new Vec2(64.5f, 52.5f), new Vec2(90.5f, 44.5f), new Vec2(117.5f, 42.5f)
    };
    static final Vec2[] CUSTOM_COLOR_CENTERS = {
            new Vec2(101.5f, 132.0f), new Vec2(113.5f, 118.0f), new Vec2(120.5f, 102.0f), new Vec2(124.5f, 084.0f),
            new Vec2(126.5f, 066.0f), new Vec2(097.5f, 152.0f), new Vec2(114.5f, 146.0f), new Vec2(127.5f, 133.0f),
            new Vec2(134.5f, 116.0f), new Vec2(139.5f, 098.0f), new Vec2(142.5f, 080.0f), new Vec2(144.5f, 062.0f),
    };
    static final Vec2 WATER_CENTER = new Vec2(140.5f, 28.f);
    static final float BASIC_COLOR_RADIUS = 11.f;
    static final float CUSTOM_COLOR_RADIUS = 6.5f;

    boolean isPickingColor = false;
    boolean isCarryingColor = false;
    boolean isCarryingWater = false;
    boolean canvasDirty = false;
    boolean paletteDirty = false;
    PaletteUtil.Color carriedColor;
    int carriedCustomColorId = -1;
    static PaletteUtil.Color currentColor = BASIC_COLORS[0];
    final PaletteUtil.CustomColor[] customColors;
    final boolean[] basicColorFlags;
    boolean paletteComplete = false;
    boolean isCarryingPalette = false;

    BasePalette(Component title, ItemStack paletteStack) {
        super(title);
        this.basicColorFlags = new boolean[16];
        PaletteCustomColors customComp = paletteStack.get(ModDataComponents.PALETTE_CUSTOM_COLORS.get());
        if (customComp != null) {
            this.customColors = customComp.colors();
        } else {
            this.customColors = new PaletteUtil.CustomColor[12];
            for (int i = 0; i < 12; i++) this.customColors[i] = new PaletteUtil.CustomColor();
        }

        ModDataComponents.PaletteBasicColors basic = paletteStack.get(ModDataComponents.PALETTE_BASIC_COLORS.get());
        if (basic != null) {
            byte[] arr = basic.colors();
            paletteComplete = arr.length == basicColorFlags.length;
            int len = Math.min(arr.length, basicColorFlags.length);
            for (int i = 0; i < len; i++) {
                basicColorFlags[i] = arr[i] > 0;
                paletteComplete &= basicColorFlags[i];
            }
        }
    }

    protected void superRender(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTicks);

        for (int i = 0; i < basicColorFlags.length; i++) {
            int x = (int) paletteX + (int) BASIC_COLOR_CENTERS[i].x;
            int y = (int) paletteY + (int) BASIC_COLOR_CENTERS[i].y;
            int r = (int) BASIC_COLOR_RADIUS;
            if (basicColorFlags[i]) {
                graphics.fill(x - r, y - r, x + r + 1, y + r + 1, BASIC_COLORS[i].rgbVal());
                graphics.blit(RenderPipelines.GUI_TEXTURED, PALETTE_TEXTURES, x - 8, y - 8, DYE_SPRITE_X, i * DYE_SPRITE_SIZE, DYE_SPRITE_SIZE, DYE_SPRITE_SIZE, 256, 256);
            } else {
                graphics.fill(x - r, y - r, x + r + 1, y + r + 1, EMPTINESS_COLOR.rgbVal());
            }
        }

        for (int i = 0; i < customColors.length; i++) {
            int x = (int) paletteX + (int) CUSTOM_COLOR_CENTERS[i].x;
            int y = (int) paletteY + (int) CUSTOM_COLOR_CENTERS[i].y;
            graphics.fill(x - 6, y - 7, x + 7, y + 6, customColors[i].getColor().rgbVal());
        }

        graphics.blit(RenderPipelines.GUI_TEXTURED, PALETTE_TEXTURES, (int) paletteX, (int) paletteY, 0, 0, PALETTE_WIDTH, PALETTE_HEIGHT, 256, 256);

        if (paletteComplete) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, PALETTE_TEXTURES, (int) paletteX + COLOR_PICKER_POS_X, (int) paletteY + COLOR_PICKER_POS_Y, COLOR_PICKER_SPRITE_X, COLOR_PICKER_SPRITE_Y, COLOR_PICKER_SIZE, COLOR_PICKER_SIZE, 256, 256);
        }
    }

    protected boolean superMouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return super.mouseClicked(event, doubleClick);
    }

    protected boolean superMouseReleased(MouseButtonEvent event) {
        return super.mouseReleased(event);
    }

    protected boolean superMouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        return super.mouseDragged(event, deltaX, deltaY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int mouseX = (int) Math.round(event.x());
        int mouseY = (int) Math.round(event.y());
        int mouseButton = event.button();

        if (paletteClick(mouseX, mouseY)) {
            int x = (mouseX - (int) paletteX);
            int y = (mouseY - (int) paletteY);
            Vec2 clickVec = new Vec2(x, y);
            float sqrBasicRadius = BASIC_COLOR_RADIUS * BASIC_COLOR_RADIUS;
            float sqrCustomRadius = CUSTOM_COLOR_RADIUS * CUSTOM_COLOR_RADIUS;

            boolean didSomething = false;
            for (int i = 0; i < BASIC_COLOR_CENTERS.length; i++) {
                if (basicColorFlags[i] && sqrDist(clickVec, BASIC_COLOR_CENTERS[i]) <= sqrBasicRadius) {
                    if (mouseButton == 0) {
                        carriedColor = currentColor = BASIC_COLORS[i];
                        setCarryingColor();
                        playSound(ModSounds.MIX.get(), 0.6f);
                    }
                    didSomething = true;
                    break;
                }
            }

            if (!didSomething) {
                for (int i = 0; i < CUSTOM_COLOR_CENTERS.length; i++) {
                    if (sqrDist(clickVec, CUSTOM_COLOR_CENTERS[i]) <= sqrCustomRadius) {
                        if (mouseButton == 0 && customColors[i].getNumberOfColors() > 0) {
                            carriedColor = currentColor = customColors[i].getColor();
                            carriedCustomColorId = i;
                            setCarryingColor();
                            playSound(ModSounds.MIX.get(), 0.3f);
                        }
                        didSomething = true;
                        break;
                    }
                }
            }

            if (!didSomething && sqrDist(clickVec, WATER_CENTER) <= sqrCustomRadius && mouseButton == 0) {
                setCarryingWater();
                playSound(ModSounds.WATER.get());
                didSomething = true;
            }

            if (!didSomething && paletteComplete && !isCarryingWater && !isCarryingColor && inColorPicker(x, y) && mouseButton == 0) {
                setPickingColor();
                playSound(ModSounds.COLOR_PICKER.get());
                didSomething = true;
            }

            if (!didSomething) {
                isCarryingPalette = true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    protected boolean inColorPicker(int x, int y) {
        return x >= COLOR_PICKER_POS_X && x < COLOR_PICKER_POS_X + COLOR_PICKER_SIZE && y >= COLOR_PICKER_POS_Y && y < COLOR_PICKER_POS_Y + COLOR_PICKER_SIZE;
    }

    protected boolean inWater(int x, int y) {
        return sqrDist(new Vec2(x, y), WATER_CENTER) <= CUSTOM_COLOR_RADIUS * CUSTOM_COLOR_RADIUS;
    }

    protected void setCarryingWater() { isCarryingWater = true; isCarryingColor = false; isPickingColor = false; }
    protected void setCarryingColor() { isCarryingWater = false; isCarryingColor = true; isPickingColor = false; }
    protected void setPickingColor() { isCarryingWater = false; isCarryingColor = false; isPickingColor = true; }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        int mouseX = (int) Math.round(event.x());
        int mouseY = (int) Math.round(event.y());
        if (isCarryingColor || isCarryingWater) {
            if (paletteClick(mouseX, mouseY)) {
                float sqrCustomRadius = CUSTOM_COLOR_RADIUS * CUSTOM_COLOR_RADIUS;
                int x = (mouseX - (int) paletteX);
                int y = (mouseY - (int) paletteY);
                Vec2 clickVec = new Vec2(x, y);
                for (int i = 0; i < CUSTOM_COLOR_CENTERS.length; i++) {
                    if (sqrDist(clickVec, CUSTOM_COLOR_CENTERS[i]) <= sqrCustomRadius) {
                        PaletteUtil.CustomColor customColor = customColors[i];
                        if (isCarryingWater) {
                            customColor.reset();
                            playSound(ModSounds.WATER_DROP.get());
                        } else {
                            if (carriedCustomColorId != i) {
                                customColor.mix(carriedColor);
                                currentColor = customColor.getColor();
                                playSound(ModSounds.MIX.get());
                            }
                        }
                        paletteDirty = true;
                        break;
                    }
                }
            }
            isCarryingColor = false;
            isCarryingWater = false;
            carriedCustomColorId = -1;
        }
        isCarryingPalette = false;
        return super.mouseReleased(event);
    }

    protected void playSound(SoundInstance sound) {
        Minecraft.getInstance().getSoundManager().play(sound);
    }

    protected void playSound(SoundEvent soundEvent) {
        playSound(soundEvent, 1.0f);
    }

    protected void playSound(SoundEvent soundEvent, float volume) {
        Minecraft m = Minecraft.getInstance();
        if (m.level != null && m.player != null) {
            m.getSoundManager().play(new SimpleSoundInstance(soundEvent, SoundSource.MASTER, volume,
                    0.8f + m.level.getRandom().nextFloat() * 0.4f, m.player.getRandom(), m.player.blockPosition()));
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void extractBlurredBackground(GuiGraphicsExtractor graphics) {
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    }

    boolean paletteClick(int x, int y) {
        return x <= paletteX + PALETTE_WIDTH && x >= paletteX && y <= paletteY + PALETTE_HEIGHT && y >= paletteY;
    }

    float sqrDist(Vec2 a, Vec2 b) {
        return (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y);
    }

    protected void blitTinted(GuiGraphicsExtractor graphics, Identifier tex, int x, int y, int u, int v, int w, int h, int texW, int texH, int rgb) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, tex, x, y, u, v, w, h, texW, texH, rgb);
    }
}
