package com.leclowndu93150.joyofpainting.client.gui;

import com.leclowndu93150.joyofpainting.CanvasSides;
import com.leclowndu93150.joyofpainting.CanvasType;
import com.leclowndu93150.joyofpainting.PaletteUtil;
import com.leclowndu93150.joyofpainting.client.sound.BrushSound;
import com.leclowndu93150.joyofpainting.entity.EntityEasel;
import com.leclowndu93150.joyofpainting.item.ItemCanvas;
import com.leclowndu93150.joyofpainting.nbt.NbtCanvas;
import com.leclowndu93150.joyofpainting.network.JoyOfPaintingNetwork;
import com.leclowndu93150.joyofpainting.network.packets.CanvasMiniUpdatePacket;
import com.leclowndu93150.joyofpainting.network.packets.CanvasUpdatePacket;
import com.leclowndu93150.joyofpainting.network.packets.EaselLeftPacket;
import com.leclowndu93150.joyofpainting.network.packets.PaletteUpdatePacket;
import com.leclowndu93150.joyofpainting.registry.ModSounds;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import static org.lwjgl.glfw.GLFW.GLFW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_HIDDEN;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_NORMAL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_O;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

public class GuiCanvasEdit extends BasePalette {
    private double canvasX;
    private double canvasY;
    private static final double[] canvasXs = {-1000, -1000, -1000, -1000};
    private static final double[] canvasYs = {-1000, -1000, -1000, -1000};
    private final int canvasWidth;
    private final int canvasHeight;
    private int brushMeterX;
    private int brushMeterY;
    private int brushOpacityMeterX;
    private int brushOpacityMeterY;
    private final int canvasPixelScale;
    private final int canvasPixelWidth;
    private final int canvasPixelHeight;
    private int brushSize = 0;
    private boolean touchedCanvas = false;
    private boolean undoStarted = false;
    private boolean gettingSigned;
    private boolean isCarryingCanvas;
    private Button buttonSign;
    private Button buttonCancel;
    private Button buttonFinalize;
    private int updateCount;
    private BrushSound brushSound = null;
    private static final int CANVAS_HOLDER_HEIGHT = 10;
    private static int brushOpacitySetting = 0;
    private static final float[] brushOpacities = {1.f, 0.75f, 0.5f, 0.25f};
    private static boolean showHelp = false;
    private final Set<Integer> draggedPoints = new HashSet<>();

    private final Player editingPlayer;
    private final CanvasType canvasType;
    private final boolean glass;
    private boolean sidesActive;
    private int[] sidePixels;
    private int sidesToggleX;
    private int sidesToggleY;
    private static final int SIDES_TOGGLE_SIZE = 8;
    private boolean isSigned = false;
    private int[] pixels;
    private String canvasTitle = "";
    private final String canvasId;
    private int version = 0;
    private final EntityEasel easel;
    private int timeSinceLastUpdate = 0;
    private boolean skippedUpdate = false;

    private static final Vec2[] outlinePoss1 = {
            new Vec2(0.f, 199.0f),
            new Vec2(12.f, 199.0f),
            new Vec2(34.f, 199.0f),
            new Vec2(76.f, 199.0f),
    };

    private static final Vec2[] outlinePoss2 = {
            new Vec2(128.f, 199.0f),
            new Vec2(135.f, 199.0f),
            new Vec2(147.f, 199.0f),
            new Vec2(169.f, 199.0f),
    };

    private static final int MAX_UNDO_LENGTH = 16;
    private final Deque<int[]> undoStack = new ArrayDeque<>(MAX_UNDO_LENGTH);

    public GuiCanvasEdit(Player player, ItemStack canvasStack, ItemStack paletteStack, Component title, CanvasType canvasType, EntityEasel easel) {
        this(player, canvasStack, paletteStack, title, canvasType,
                canvasStack.getItem() instanceof ItemCanvas ic && ic.isGlass(), easel);
    }

    public GuiCanvasEdit(Player player, ItemStack canvasStack, ItemStack paletteStack, Component title, CanvasType canvasType, boolean glass, EntityEasel easel) {
        super(title, paletteStack);
        updateCount = 0;

        this.canvasType = canvasType;
        this.glass = glass;
        this.canvasPixelScale = canvasType == CanvasType.SMALL ? 10 : 5;
        this.canvasPixelWidth = CanvasType.getWidth(canvasType);
        this.canvasPixelHeight = CanvasType.getHeight(canvasType);
        int canvasPixelArea = canvasPixelHeight * canvasPixelWidth;
        this.canvasWidth = this.canvasPixelWidth * this.canvasPixelScale;
        this.canvasHeight = this.canvasPixelHeight * this.canvasPixelScale;
        this.easel = easel;

        this.editingPlayer = player;
        String stackCanvasId = NbtCanvas.getName(canvasStack);
        if (NbtCanvas.hasPixels(canvasStack) && !stackCanvasId.isEmpty()) {
            this.pixels = NbtCanvas.getPixels(canvasStack);
            this.canvasId = stackCanvasId;
            this.version = NbtCanvas.getVersion(canvasStack);
            if (this.version <= 0) this.version = 1;
            this.canvasTitle = NbtCanvas.getTitle(canvasStack);
            this.isSigned = !canvasTitle.isEmpty();
        } else {
            this.pixels = new int[canvasPixelArea];
            Arrays.fill(this.pixels, glass ? 0 : BASIC_COLORS[15].rgbVal());
            this.canvasId = ItemCanvas.generateName(player);
        }
        this.sidesActive = NbtCanvas.getSidesActive(canvasStack);
        if (NbtCanvas.hasSidePixels(canvasStack)) {
            int[] saved = NbtCanvas.getSidePixels(canvasStack);
            if (saved.length == CanvasSides.count(canvasType)) {
                this.sidePixels = saved;
            }
        }
    }

    private void ensureSidePixels() {
        if (this.sidePixels == null || this.sidePixels.length != CanvasSides.count(this.canvasType)) {
            this.sidePixels = CanvasSides.defaultPixels(this.canvasType, this.glass);
        }
    }

    private int getSidePixel(int index) {
        return (this.sidePixels != null && index >= 0 && index < this.sidePixels.length)
                ? this.sidePixels[index]
                : CanvasSides.DEFAULT_COLOR;
    }

    private int sideMargin() {
        return this.sidesActive ? this.canvasPixelScale : 0;
    }

    private int sideIndexFor(int col, int row) {
        int w = this.canvasPixelWidth;
        int h = this.canvasPixelHeight;
        if (row == -1 && col >= 0 && col < w) return CanvasSides.topOffset() + col;
        if (row == h && col >= 0 && col < w) return CanvasSides.bottomOffset(canvasType) + col;
        if (col == -1 && row >= 0 && row < h) return CanvasSides.leftOffset(canvasType) + row;
        if (col == w && row >= 0 && row < h) return CanvasSides.rightOffset(canvasType) + row;
        return -1;
    }

    private boolean overSide(int mouseX, int mouseY) {
        if (!this.sidesActive) return false;
        int col = Math.floorDiv(mouseX - (int) canvasX, canvasPixelScale);
        int row = Math.floorDiv(mouseY - (int) canvasY, canvasPixelScale);
        return sideIndexFor(col, row) >= 0;
    }

    private boolean inSidesToggle(int x, int y) {
        return x >= sidesToggleX && x < sidesToggleX + SIDES_TOGGLE_SIZE
                && y >= sidesToggleY && y < sidesToggleY + SIDES_TOGGLE_SIZE;
    }

    private void drawSideRects(GuiGraphics g) {
        int scale = canvasPixelScale;
        int cx = (int) canvasX;
        int cy = (int) canvasY;
        for (int k = 0; k < canvasPixelWidth; k++) {
            int x = cx + k * scale;
            if (glass) {
                int colTop = ((k - 1) & 1) == 0 ? 0xFFBFBFBF : 0xFF7F7F7F;
                int colBot = ((k + canvasPixelHeight) & 1) == 0 ? 0xFFBFBFBF : 0xFF7F7F7F;
                g.fill(x, cy - scale, x + scale, cy, colTop);
                g.fill(x, cy + canvasHeight, x + scale, cy + canvasHeight + scale, colBot);
            }
            g.fill(x, cy - scale, x + scale, cy, getSidePixel(CanvasSides.topOffset() + k));
            g.fill(x, cy + canvasHeight, x + scale, cy + canvasHeight + scale, getSidePixel(CanvasSides.bottomOffset(canvasType) + k));
        }
        for (int i = 0; i < canvasPixelHeight; i++) {
            int y = cy + i * scale;
            if (glass) {
                int colL = ((i - 1) & 1) == 0 ? 0xFFBFBFBF : 0xFF7F7F7F;
                int colR = ((i + canvasPixelWidth) & 1) == 0 ? 0xFFBFBFBF : 0xFF7F7F7F;
                g.fill(cx - scale, y, cx, y + scale, colL);
                g.fill(cx + canvasWidth, y, cx + canvasWidth + scale, y + scale, colR);
            }
            g.fill(cx - scale, y, cx, y + scale, getSidePixel(CanvasSides.leftOffset(canvasType) + i));
            g.fill(cx + canvasWidth, y, cx + canvasWidth + scale, y + scale, getSidePixel(CanvasSides.rightOffset(canvasType) + i));
        }
    }

    private void drawSidesToggle(GuiGraphics g) {
        int x = sidesToggleX, y = sidesToggleY, s = SIDES_TOGGLE_SIZE;
        g.fill(x - 1, y - 1, x + s + 1, y + s + 1, 0xFF000000);
        g.fill(x, y, x + s, y + s, sidesActive ? 0xFF66CCFF : 0xFFB0B0B0);
        int edge = sidesActive ? 0xFFFFFFFF : 0xFF808080;
        g.fill(x + 1, y + 1, x + s - 1, y + 2, edge);
        g.fill(x + 1, y + s - 2, x + s - 1, y + s - 1, edge);
        g.fill(x + 1, y + 2, x + 2, y + s - 2, edge);
        g.fill(x + s - 2, y + 2, x + s - 1, y + s - 2, edge);
    }

    private void toggleSides() {
        this.sidesActive = !this.sidesActive;
        if (this.sidesActive) ensureSidePixels();
        this.canvasDirty = true;
        updateCanvasPos(0, 0);
        playSound(ModSounds.MIX.get(), 0.5f);
        if (easel != null) updateCanvas(false);
    }

    @Override
    public void init() {
        if (minecraft == null) return;
        canvasX = canvasXs[canvasType.ordinal()];
        canvasY = canvasYs[canvasType.ordinal()];
        paletteX = PALETTE_XS[canvasType.ordinal()];
        paletteY = PALETTE_YS[canvasType.ordinal()];
        if (canvasX == -1000 || canvasY == -1000 || paletteX == -1000 || paletteY == -1000) {
            resetPositions();
        }
        updateCanvasPos(0, 0);
        updatePalettePos(0, 0);

        Window window = minecraft.getWindow();
        GLFW.glfwSetInputMode(window.getWindow(), GLFW_CURSOR, GLFW_CURSOR_HIDDEN);

        int x = window.getGuiScaledWidth() - 120;
        int y = window.getGuiScaledHeight() - 30;
        this.buttonSign = this.addRenderableWidget(Button.builder(Component.translatable("canvas.signButton"), button -> {
            if (!isSigned) {
                gettingSigned = true;
                resetPositions();
                updateButtons();
                GLFW.glfwSetInputMode(window.getWindow(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
            }
        }).bounds(x, y, 98, 20).build());
        this.buttonFinalize = this.addRenderableWidget(Button.builder(Component.translatable("canvas.finalizeButton"), button -> {
            if (!isSigned) {
                canvasDirty = true;
                isSigned = true;
                if (minecraft != null) minecraft.setScreen(null);
            }
        }).bounds((int) canvasX - 100, 100, 98, 20).build());
        this.buttonCancel = this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> {
            if (!isSigned) {
                gettingSigned = false;
                updateButtons();
                GLFW.glfwSetInputMode(window.getWindow(), GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
            }
        }).bounds((int) canvasX - 100, 130, 98, 20).build());

        x = (int) (window.getGuiScaledWidth() * 0.95) - 21;
        y = (int) (window.getGuiScaledHeight() * 0.05);
        this.addRenderableWidget(new ToggleHelpButton(x, y, 21, 21, 197, 0, 21,
                PALETTE_TEXTURES, 256, 256, button -> showHelp = !showHelp, Tooltip.create(Component.translatable("canvas.help.toggleHelp"))));

        updateButtons();
    }

    private void updateButtons() {
        if (!this.isSigned) {
            this.buttonSign.visible = !this.gettingSigned;
            this.buttonCancel.visible = this.gettingSigned;
            this.buttonFinalize.visible = this.gettingSigned;
            this.buttonFinalize.active = !this.canvasTitle.trim().isEmpty();
            this.buttonFinalize.setX((int) canvasX - 100);
            this.buttonCancel.setX((int) canvasX - 100);
        }
    }

    private int getPixelAt(int x, int y) {
        return this.pixels[y * canvasPixelWidth + x];
    }

    private int blendPixel(int old, PaletteUtil.Color color, float opacity, boolean erase) {
        if (glass) {
            if (erase) return 0;
            if ((old >>> 24) == 0) return color.rgbVal();
        }
        return PaletteUtil.Color.mix(color, new PaletteUtil.Color(old), opacity).rgbVal();
    }

    private void setPixelAt(int x, int y, PaletteUtil.Color color, float opacity) {
        setPixelAt(x, y, color, opacity, false);
    }

    private void setPixelAt(int x, int y, PaletteUtil.Color color, float opacity, boolean erase) {
        boolean onCanvas = x >= 0 && y >= 0 && x < canvasPixelWidth && y < canvasPixelHeight;
        int sideIndex = -1;
        if (!onCanvas) {
            if (!sidesActive) return;
            sideIndex = sideIndexFor(x, y);
            if (sideIndex < 0) return;
        }
        int key = onCanvas ? (y * canvasPixelWidth + x) : (canvasPixelWidth * canvasPixelHeight + sideIndex);
        if (draggedPoints.add(key)) {
            if (onCanvas) {
                int i = y * canvasPixelWidth + x;
                this.pixels[i] = blendPixel(this.pixels[i], color, opacity, erase);
            } else {
                ensureSidePixels();
                this.sidePixels[sideIndex] = blendPixel(this.sidePixels[sideIndex], color, opacity, erase);
            }
        }
    }

    private static final int[][][] BRUSH_OFFSETS = {
            {{0, 0}},
            {{0, 0}, {-1, 0}, {0, -1}, {-1, -1}},
            {{-1, 1}, {0, 1}, {-2, 0}, {-1, 0}, {0, 0}, {1, 0}, {-2, -1}, {-1, -1}, {0, -1}, {1, -1}, {-1, -2}, {0, -2}},
            {{-1, 2}, {0, 2}, {1, 2}, {-2, 1}, {-1, 1}, {0, 1}, {1, 1}, {2, 1}, {-2, 0}, {-1, 0}, {0, 0}, {1, 0}, {2, 0}, {-2, -1}, {-1, -1}, {0, -1}, {1, -1}, {2, -1}, {-1, -2}, {0, -2}, {1, -2}}
    };
    private static final boolean[] BRUSH_CORNER_ANCHOR = {false, true, true, false};

    private int brushAnchor(int mousePos, int origin, boolean corner) {
        int rel = mousePos - origin + (corner ? canvasPixelScale / 2 : 0);
        return Math.floorDiv(rel, canvasPixelScale);
    }

    private void setPixelsAt(int mouseX, int mouseY, PaletteUtil.Color color, int brushSize, float opacity) {
        setPixelsAt(mouseX, mouseY, color, brushSize, opacity, false);
    }

    private void setPixelsAt(int mouseX, int mouseY, PaletteUtil.Color color, int brushSize, float opacity, boolean erase) {
        boolean corner = BRUSH_CORNER_ANCHOR[brushSize];
        int anchorCol = brushAnchor(mouseX, (int) canvasX, corner);
        int anchorRow = brushAnchor(mouseY, (int) canvasY, corner);
        for (int[] off : BRUSH_OFFSETS[brushSize]) {
            setPixelAt(anchorCol + off[0], anchorRow + off[1], color, opacity, erase);
        }
    }

    private void resetPositions() {
        final int padding = 40;
        final int paletteCanvasX = (this.width - (PALETTE_WIDTH + canvasWidth + padding)) / 2;
        canvasX = paletteCanvasX + PALETTE_WIDTH + (double) padding;
        canvasY = canvasType.equals(CanvasType.LONG) ? 80 : 40;
        paletteX = paletteCanvasX;
        paletteY = 40;
    }

    @Override
    public void tick() {
        ++this.updateCount;
        ++this.timeSinceLastUpdate;
        if (easel != null) {
            if (easel.getItem().isEmpty() || easel.isRemoved() || easel.distanceToSqr(editingPlayer) > 64) {
                this.onClose();
            }
            if (skippedUpdate && timeSinceLastUpdate > 20 && canvasDirty) {
                updateCanvas(false);
                skippedUpdate = false;
            }
        }
        super.tick();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float f) {
        if (!gettingSigned) {
            super.render(guiGraphics, mouseX, mouseY, f);
        } else {
            super.superRender(guiGraphics, mouseX, mouseY, f);
        }

        int holderMargin = sideMargin();
        guiGraphics.fill((int) (canvasX + canvasWidth * 0.25), (int) canvasY - CANVAS_HOLDER_HEIGHT - holderMargin, (int) (canvasX + canvasWidth * 0.75), (int) canvasY - holderMargin, 0xffe1e1e1);

        if (glass) {
            for (int i = 0; i < canvasPixelHeight; i++) {
                for (int j = 0; j < canvasPixelWidth; j++) {
                    int x = (int) canvasX + j * canvasPixelScale;
                    int y = (int) canvasY + i * canvasPixelScale;
                    int color = ((i + j) & 1) == 0 ? 0xFFBFBFBF : 0xFF7F7F7F;
                    guiGraphics.fill(x, y, x + canvasPixelScale, y + canvasPixelScale, color);
                }
            }
        }
        for (int i = 0; i < canvasPixelHeight; i++) {
            for (int j = 0; j < canvasPixelWidth; j++) {
                int y = (int) canvasY + i * canvasPixelScale;
                int x = (int) canvasX + j * canvasPixelScale;
                guiGraphics.fill(x, y, x + canvasPixelScale, y + canvasPixelScale, getPixelAt(j, i));
            }
        }
        if (sidesActive && !gettingSigned) {
            drawSideRects(guiGraphics);
        }
        if (!gettingSigned) {
            drawSidesToggle(guiGraphics);
        }

        if (!gettingSigned) {
            for (int i = 0; i < 4; i++) {
                int y = brushMeterY + i * BRUSH_SPRITE_SIZE;
                guiGraphics.fill(brushMeterX, y, brushMeterX + 3, y + 3, currentColor.rgbVal());
            }
            guiGraphics.blit(PALETTE_TEXTURES, brushMeterX, brushMeterY + (3 - brushSize) * BRUSH_SPRITE_SIZE, 15, 246, 10, 10, 256, 256);
            guiGraphics.blit(PALETTE_TEXTURES, brushMeterX, brushMeterY, BRUSH_SPRITE_X, BRUSH_SPRITE_Y - BRUSH_SPRITE_SIZE * 3, BRUSH_SPRITE_SIZE, BRUSH_SPRITE_SIZE * 4, 256, 256);

            guiGraphics.blit(PALETTE_TEXTURES, brushOpacityMeterX, brushOpacityMeterY, BRUSH_OPACITY_SPRITE_X, BRUSH_OPACITY_SPRITE_Y, BRUSH_OPACITY_SPRITE_SIZE, BRUSH_OPACITY_SPRITE_SIZE * 4 + 3, 256, 256);
            guiGraphics.blit(PALETTE_TEXTURES, brushOpacityMeterX - 1, brushOpacityMeterY - 1 + brushOpacitySetting * (BRUSH_OPACITY_SPRITE_SIZE + 1), 212, 240, 16, 16, 256, 256);

            renderCursor(guiGraphics, mouseX, mouseY);

            if (showHelp) {
                if (inBrushMeter(mouseX, mouseY)) {
                    int selectedSize = 3 - (mouseY - brushMeterY) / BRUSH_SPRITE_SIZE;
                    if (selectedSize <= 3 && selectedSize >= 0) {
                        guiGraphics.renderTooltip(font, Component.translatable("canvas.help.brushSize", selectedSize + 1), mouseX, mouseY);
                    }
                } else if (inBrushOpacityMeter(mouseX, mouseY)) {
                    int relativeY = mouseY - brushOpacityMeterY;
                    int selectedOpacity = relativeY / (BRUSH_OPACITY_SPRITE_SIZE + 1);
                    if (selectedOpacity >= 0 && selectedOpacity <= 3) {
                        int percentage = 100 - 25 * selectedOpacity;
                        guiGraphics.renderTooltip(font, Component.translatable("canvas.help.brushOpacity", percentage), mouseX, mouseY);
                    }
                } else if (inColorPicker(mouseX - (int) paletteX, mouseY - (int) paletteY)) {
                    guiGraphics.renderComponentTooltip(font, Arrays.asList(
                            Component.translatable("canvas.help.colorPicker"),
                            Component.translatable("canvas.help.colorPicker.desc").withStyle(ChatFormatting.GRAY)), mouseX, mouseY);
                } else if (inWater(mouseX - (int) paletteX, mouseY - (int) paletteY)) {
                    guiGraphics.renderComponentTooltip(font, Arrays.asList(
                            Component.translatable("canvas.help.colorRemover"),
                            Component.translatable("canvas.help.colorRemover.desc").withStyle(ChatFormatting.GRAY)), mouseX, mouseY);
                } else if (inCanvasHolder(mouseX, mouseY)) {
                    guiGraphics.renderComponentTooltip(font, Arrays.asList(
                            Component.translatable("canvas.help.canvasHolder"),
                            Component.translatable("canvas.help.canvasHolder.desc").withStyle(ChatFormatting.GRAY)), mouseX, mouseY);
                } else if (inSidesToggle(mouseX, mouseY)) {
                    guiGraphics.renderTooltip(font, Component.translatable("canvas.help.toggleSides"), mouseX, mouseY);
                }
            }
        } else {
            drawSigning(guiGraphics);
        }
    }

    private void renderCursor(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (isCarryingColor) {
            blitTinted(guiGraphics, PALETTE_TEXTURES, mouseX - BRUSH_SPRITE_SIZE / 2, mouseY - BRUSH_SPRITE_SIZE / 2, BRUSH_SPRITE_X + BRUSH_SPRITE_SIZE, BRUSH_SPRITE_Y, DROP_SPRITE_WIDTH, BRUSH_SPRITE_SIZE, 256, 256, carriedColor.rgbVal());
        } else if (isCarryingWater) {
            blitTinted(guiGraphics, PALETTE_TEXTURES, mouseX - BRUSH_SPRITE_SIZE / 2, mouseY - BRUSH_SPRITE_SIZE / 2, BRUSH_SPRITE_X + BRUSH_SPRITE_SIZE, BRUSH_SPRITE_Y, DROP_SPRITE_WIDTH, BRUSH_SPRITE_SIZE, 256, 256, WATER_COLOR.rgbVal());
        } else if (isPickingColor) {
            drawOutline(guiGraphics, mouseX, mouseY, 0);
            blitTinted(guiGraphics, PALETTE_TEXTURES, mouseX, mouseY - COLOR_PICKER_SIZE, COLOR_PICKER_SPRITE_X, COLOR_PICKER_SPRITE_Y, COLOR_PICKER_SIZE, COLOR_PICKER_SIZE, 256, 256, PaletteUtil.Color.WHITE.rgbVal());
        } else {
            drawOutline(guiGraphics, mouseX, mouseY, brushSize);
            guiGraphics.fill(mouseX, mouseY, mouseX + 3, mouseY + 3, currentColor.rgbVal());
            int trueBrushY = BRUSH_SPRITE_Y - BRUSH_SPRITE_SIZE * brushSize;
            guiGraphics.blit(PALETTE_TEXTURES, mouseX, mouseY, BRUSH_SPRITE_X, trueBrushY, BRUSH_SPRITE_SIZE, BRUSH_SPRITE_SIZE, 256, 256);
        }
    }

    private void drawOutline(GuiGraphics guiGraphics, int mouseX, int mouseY, int brushSize) {
        if (inCanvas(mouseX, mouseY)) {
            int x = 0;
            int y = 0;
            int outlineSize = 0;
            int pixelHalf = canvasPixelScale / 2;
            if (brushSize == 0) {
                x = ((mouseX - (int) canvasX) / canvasPixelScale) * canvasPixelScale + (int) canvasX - 1;
                y = ((mouseY - (int) canvasY) / canvasPixelScale) * canvasPixelScale + (int) canvasY - 1;
                outlineSize = canvasPixelScale + 2;
            }
            if (brushSize == 1) {
                x = (((mouseX - (int) canvasX + pixelHalf) / canvasPixelScale) - 1) * canvasPixelScale + (int) canvasX - 1;
                y = (((mouseY - (int) canvasY + pixelHalf) / canvasPixelScale) - 1) * canvasPixelScale + (int) canvasY - 1;
                outlineSize = canvasPixelScale * 2 + 2;
            }
            if (brushSize == 2) {
                x = (((mouseX - (int) canvasX + pixelHalf) / canvasPixelScale) - 2) * canvasPixelScale + (int) canvasX - 1;
                y = (((mouseY - (int) canvasY + pixelHalf) / canvasPixelScale) - 2) * canvasPixelScale + (int) canvasY - 1;
                outlineSize = canvasPixelScale * 4 + 2;
            }
            if (brushSize == 3) {
                x = (((mouseX - (int) canvasX) / canvasPixelScale) - 2) * canvasPixelScale + (int) canvasX - 1;
                y = (((mouseY - (int) canvasY) / canvasPixelScale) - 2) * canvasPixelScale + (int) canvasY - 1;
                outlineSize = canvasPixelScale * 5 + 2;
            }
            Vec2 textureVec = canvasPixelScale == 10 ? outlinePoss1[brushSize] : outlinePoss2[brushSize];
            blitTinted(guiGraphics, PALETTE_TEXTURES, x, y, (int) textureVec.x, (int) textureVec.y, outlineSize, outlineSize, 256, 256, 0xFF4D4D4D);
        }
    }

    private void drawSigning(GuiGraphics guiGraphics) {
        int i = (int) canvasX;
        int j = (int) canvasY;
        guiGraphics.fill(i + 10, j + 10, i + 150, j + 150, 0xFFEEEEEE);
        String s = this.canvasTitle;

        if (!this.isSigned) {
            if (this.updateCount / 6 % 2 == 0) {
                s = s + ChatFormatting.BLACK + "_";
            } else {
                s = s + ChatFormatting.GRAY + "_";
            }
        }
        String s1 = I18n.get("canvas.editTitle");
        int k = this.font.width(s1);
        guiGraphics.drawString(this.font, s1, (int) (i + 26 + (116 - k) / 2.0f), j + 16 + 16, 0, false);
        int l = this.font.width(s);
        guiGraphics.drawString(this.font, s, (int) (i + 26 + (116 - l) / 2.0f), j + 48, 0, false);
        String s2 = I18n.get("canvas.byAuthor", this.editingPlayer.getName().getString());
        int i1 = this.font.width(s2);
        guiGraphics.drawString(this.font, ChatFormatting.DARK_GRAY + s2, (int) (i + 26 + (116 - i1) / 2.0f), j + 48 + 10, 0, false);
        guiGraphics.drawWordWrap(this.font, Component.translatable("canvas.finalizeWarning"), i + 26, j + 80, 116, 0);
    }

    private void playBrushSound() {
        brushSound = new BrushSound();
        playSound(brushSound);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.gettingSigned) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (!this.canvasTitle.isEmpty()) {
                    this.canvasTitle = this.canvasTitle.substring(0, this.canvasTitle.length() - 1);
                    this.updateButtons();
                }
            } else if (keyCode == GLFW.GLFW_KEY_ENTER && !this.canvasTitle.isEmpty()) {
                canvasDirty = true;
                this.isSigned = true;
                if (this.minecraft != null) {
                    this.minecraft.setScreen(null);
                }
            }
            return true;
        } else {
            if (keyCode == GLFW.GLFW_KEY_Z && (modifiers & GLFW.GLFW_MOD_CONTROL) == GLFW.GLFW_MOD_CONTROL) {
                if (!undoStack.isEmpty()) {
                    pixels = undoStack.pop();
                    canvasDirty = true;
                    if (easel != null) {
                        updateCanvas(false);
                    }
                }
                return true;
            }
            if (keyCode == GLFW_KEY_O) {
                brushOpacitySetting += 1;
                if (brushOpacitySetting >= 4) brushOpacitySetting = 0;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    private static boolean isAllowedChatCharacter(char c) {
        return c != 167 && c >= ' ' && c != 127;
    }

    @Override
    public boolean charTyped(char typedChar, int modifiers) {
        super.charTyped(typedChar, modifiers);
        if (!this.isSigned) {
            if (this.gettingSigned && this.canvasTitle.length() < 16 && isAllowedChatCharacter(typedChar)) {
                this.canvasTitle = this.canvasTitle + typedChar;
                this.updateButtons();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double posX, double posY, double scrollDelta) {
        int mouseX = (int) Math.floor(posX);
        int mouseY = (int) Math.floor(posY);
        if (!gettingSigned && scrollDelta != 0.d) {
            if (inBrushOpacityMeter(mouseX, mouseY)) {
                final int maxBrushOpacity = 3;
                brushOpacitySetting += scrollDelta < 0 ? 1 : -1;
                if (brushOpacitySetting > maxBrushOpacity) brushOpacitySetting = 0;
                else if (brushOpacitySetting < 0) brushOpacitySetting = maxBrushOpacity;
                return true;
            } else {
                final int maxBrushSize = 3;
                brushSize += scrollDelta > 0 ? 1 : -1;
                if (brushSize > maxBrushSize) brushSize = 0;
                else if (brushSize < 0) brushSize = maxBrushSize;
                return true;
            }
        }
        return super.mouseScrolled(posX, posY, scrollDelta);
    }

    @Override
    public boolean mouseClicked(double posX, double posY, int mouseButton) {
        if (gettingSigned) {
            return super.superMouseClicked(posX, posY, mouseButton);
        }

        int mouseX = (int) Math.floor(posX);
        int mouseY = (int) Math.floor(posY);

        undoStarted = true;
        touchedCanvas = false;
        if (undoStack.size() >= MAX_UNDO_LENGTH) {
            undoStack.removeLast();
        }
        undoStack.push(pixels.clone());

        if (inSidesToggle(mouseX, mouseY)) {
            toggleSides();
            return super.superMouseClicked(mouseX, mouseY, mouseButton);
        }
        if (inPaintable(mouseX, mouseY)) {
            if (isPickingColor) {
                Integer color = cellColorAt(mouseX, mouseY);
                if (color != null) {
                    carriedColor = new PaletteUtil.Color(color);
                    setCarryingColor();
                    playSound(ModSounds.COLOR_PICKER_SUCK.get());
                }
            } else {
                clickedCanvas(mouseX, mouseY, mouseButton);
                playBrushSound();
            }
            return super.superMouseClicked(mouseX, mouseY, mouseButton);
        }

        if (inBrushMeter(mouseX, mouseY)) {
            int selectedSize = 3 - (mouseY - brushMeterY) / BRUSH_SPRITE_SIZE;
            if (selectedSize <= 3 && selectedSize >= 0) {
                brushSize = selectedSize;
            }
            return super.superMouseClicked(mouseX, mouseY, mouseButton);
        }
        if (inBrushOpacityMeter(mouseX, mouseY)) {
            int relativeY = mouseY - brushOpacityMeterY;
            int selectedOpacity = relativeY / (BRUSH_OPACITY_SPRITE_SIZE + 1);
            if (selectedOpacity >= 0 && selectedOpacity <= 3) {
                brushOpacitySetting = selectedOpacity;
            }
            return super.superMouseClicked(mouseX, mouseY, mouseButton);
        }
        if (inCanvasHolder(mouseX, mouseY)) {
            isCarryingCanvas = true;
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private void clickedCanvas(int mouseX, int mouseY, int mouseButton) {
        touchedCanvas = true;
        boolean erase = mouseButton == GLFW_MOUSE_BUTTON_RIGHT;
        PaletteUtil.Color color = erase ? PaletteUtil.Color.WHITE : currentColor;
        float opacity = erase ? 1.0f : brushOpacities[brushOpacitySetting];
        setPixelsAt(mouseX, mouseY, color, brushSize, opacity, erase);
        canvasDirty = true;
    }

    @Override
    public boolean mouseReleased(double posX, double posY, int mouseButton) {
        isCarryingCanvas = false;
        if (gettingSigned) {
            return super.superMouseReleased(posX, posY, mouseButton);
        }
        draggedPoints.clear();
        if (undoStarted && !touchedCanvas) {
            undoStarted = false;
            undoStack.removeFirst();
        }
        if (brushSound != null) {
            brushSound.stopSound();
        }
        if (easel != null) {
            updateCanvas(false);
        }
        return super.mouseReleased(posX, posY, mouseButton);
    }

    @Override
    public boolean mouseDragged(double posX, double posY, int mouseButton, double deltaX, double deltaY) {
        if (gettingSigned) {
            return super.superMouseDragged(posX, posY, mouseButton, deltaX, deltaY);
        }
        if (!isCarryingColor && !isCarryingWater && !isPickingColor && !isCarryingPalette && !isCarryingCanvas) {
            int mouseX = (int) Math.floor(posX);
            int mouseY = (int) Math.floor(posY);
            if (inPaintable(mouseX, mouseY)) {
                clickedCanvas(mouseX, mouseY, mouseButton);
            }
            if (brushSound != null) {
                brushSound.refreshFade();
            }
        } else if (isCarryingCanvas) {
            updateCanvasPos(deltaX, deltaY);
            return super.superMouseDragged(posX, posY, mouseButton, deltaX, deltaY);
        } else if (isCarryingPalette) {
            boolean ret = super.mouseDragged(posX, posY, mouseButton, deltaX, deltaY);
            updatePalettePos(deltaX, deltaY);
            return ret;
        }
        return super.mouseDragged(posX, posY, mouseButton, deltaX, deltaY);
    }

    private void updateCanvasPos(double deltaX, double deltaY) {
        canvasX += deltaX;
        canvasY += deltaY;
        int margin = sideMargin();
        brushMeterX = (int) canvasX + canvasWidth + 2 + margin;
        brushMeterY = (int) canvasY + canvasHeight / 2 + 30;
        brushOpacityMeterX = (int) canvasX + canvasWidth + 2 + margin;
        brushOpacityMeterY = (int) canvasY;
        sidesToggleX = (int) (canvasX + canvasWidth * 0.25) - SIDES_TOGGLE_SIZE - 3;
        sidesToggleY = (int) canvasY - SIDES_TOGGLE_SIZE - 1 - margin;
        canvasXs[canvasType.ordinal()] = canvasX;
        canvasYs[canvasType.ordinal()] = canvasY;
    }

    private void updatePalettePos(double deltaX, double deltaY) {
        paletteX += deltaX;
        paletteY += deltaY;
        PALETTE_XS[canvasType.ordinal()] = paletteX;
        PALETTE_YS[canvasType.ordinal()] = paletteY;
    }

    private boolean inCanvas(int x, int y) {
        return x < canvasX + canvasWidth && x >= canvasX && y < canvasY + canvasHeight && y >= canvasY;
    }

    private boolean inPaintable(int x, int y) {
        return inCanvas(x, y) || overSide(x, y);
    }

    private Integer cellColorAt(int mouseX, int mouseY) {
        int col = Math.floorDiv(mouseX - (int) canvasX, canvasPixelScale);
        int row = Math.floorDiv(mouseY - (int) canvasY, canvasPixelScale);
        if (col >= 0 && col < canvasPixelWidth && row >= 0 && row < canvasPixelHeight) {
            return pixels[row * canvasPixelWidth + col];
        }
        if (sidesActive) {
            int idx = sideIndexFor(col, row);
            if (idx >= 0) return getSidePixel(idx);
        }
        return null;
    }

    private boolean inCanvasHolder(int x, int y) {
        int m = sideMargin();
        return x < canvasX + canvasWidth * 0.75 && x >= canvasX + canvasWidth * 0.25 && y < canvasY - m && y >= canvasY - CANVAS_HOLDER_HEIGHT - m;
    }

    private boolean inBrushMeter(int x, int y) {
        return x < brushMeterX + BRUSH_SPRITE_SIZE && x >= brushMeterX && y < brushMeterY + BRUSH_SPRITE_SIZE * 4 && y >= brushMeterY;
    }

    private boolean inBrushOpacityMeter(int x, int y) {
        return x < brushOpacityMeterX + BRUSH_OPACITY_SPRITE_SIZE && x >= brushOpacityMeterX && y < brushOpacityMeterY + BRUSH_OPACITY_SPRITE_SIZE * 4 + 3 && y >= brushOpacityMeterY;
    }

    @Override
    public void removed() {
        updateCanvas(true);
        if (minecraft != null) {
            GLFW.glfwSetInputMode(minecraft.getWindow().getWindow(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        }
    }

    private void updateCanvas(boolean closing) {
        int[] sideData = sidePixels == null ? new int[0] : sidePixels;
        if (closing) {
            if (canvasDirty) {
                version++;
                int easelId = easel == null ? -1 : easel.getId();
                JoyOfPaintingNetwork.CHANNEL.sendToServer(new CanvasUpdatePacket(pixels, isSigned, canvasTitle, canvasId, version, easelId, customColors, canvasType, sidesActive, sideData));
            } else {
                if (easel != null) {
                    JoyOfPaintingNetwork.CHANNEL.sendToServer(new EaselLeftPacket(easel.getId()));
                }
                if (paletteDirty) {
                    JoyOfPaintingNetwork.CHANNEL.sendToServer(new PaletteUpdatePacket(customColors));
                }
            }
        } else {
            if (canvasDirty) {
                if (timeSinceLastUpdate < 10) {
                    skippedUpdate = true;
                } else {
                    version++;
                    JoyOfPaintingNetwork.CHANNEL.sendToServer(new CanvasMiniUpdatePacket(pixels, canvasId, version, easel.getId(), canvasType, sidesActive, sideData));
                    canvasDirty = false;
                    timeSinceLastUpdate = 0;
                }
            }
        }
    }

    public static class ToggleHelpButton extends Button {
        protected final ResourceLocation resourceLocation;
        protected final int xTexStart;
        protected final int yTexStart;
        protected final int yDiffText;
        protected final int texWidth;
        protected final int texHeight;

        public ToggleHelpButton(int x, int y, int width, int height, int xTexStart, int yTexStart, int yDiffText, ResourceLocation texture, int texWidth, int texHeight, OnPress onClick, Tooltip tooltip) {
            super(x, y, width, height, Component.empty(), onClick, Button.DEFAULT_NARRATION);
            this.texWidth = texWidth;
            this.texHeight = texHeight;
            this.xTexStart = xTexStart;
            this.yTexStart = yTexStart;
            this.yDiffText = yDiffText;
            this.resourceLocation = texture;
            setTooltip(tooltip);
        }

        protected void postRender() {
            GlStateManager._enableDepthTest();
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            RenderSystem.setShaderTexture(0, this.resourceLocation);
            GlStateManager._disableDepthTest();
            int yTexStartNew = this.yTexStart;
            if (this.isHovered) {
                yTexStartNew += this.yDiffText;
            }
            int xTexStartNew = this.xTexStart + (showHelp ? 0 : this.width);
            guiGraphics.blit(resourceLocation, this.getX(), this.getY(), xTexStartNew, yTexStartNew, this.width, this.height, this.texWidth, this.texHeight);
            postRender();
        }
    }
}
