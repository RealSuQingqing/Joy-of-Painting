package com.leclowndu93150.joyofpainting.client.gui;

import com.leclowndu93150.joyofpainting.CanvasSides;
import com.leclowndu93150.joyofpainting.CanvasType;
import com.leclowndu93150.joyofpainting.entity.EntityEasel;
import com.leclowndu93150.joyofpainting.item.ItemCanvas;
import com.leclowndu93150.joyofpainting.registry.ModDataComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class GuiCanvasView extends Screen {
    private int canvasX;
    private int canvasY = 40;
    private final int canvasWidth;
    private final int canvasPixelScale;
    private final int canvasPixelWidth;
    private final int canvasPixelHeight;
    private final CanvasType canvasType;

    private int[] pixels;
    private int[] sidePixels;
    private boolean sidesActive;
    private boolean glass;
    private String authorName = "";
    private String canvasTitle = "";
    private int generation = 0;
    private final EntityEasel easel;
    private final Player player;

    public GuiCanvasView(ItemStack canvasStack, Component title, CanvasType canvasType, EntityEasel easel) {
        super(title);
        this.canvasType = canvasType;
        this.canvasPixelScale = canvasType == CanvasType.SMALL ? 10 : 5;
        this.canvasPixelWidth = CanvasType.getWidth(canvasType);
        this.canvasPixelHeight = CanvasType.getHeight(canvasType);
        this.canvasWidth = this.canvasPixelWidth * this.canvasPixelScale;
        this.easel = easel;
        this.player = Minecraft.getInstance().player;

        List<Integer> stackPixels = canvasStack.get(ModDataComponents.CANVAS_PIXELS.get());
        if (stackPixels != null) {
            this.pixels = stackPixels.stream().mapToInt(Integer::intValue).toArray();
            this.authorName = canvasStack.getOrDefault(ModDataComponents.CANVAS_AUTHOR.get(), "");
            this.canvasTitle = canvasStack.getOrDefault(ModDataComponents.CANVAS_TITLE.get(), "");
            this.generation = canvasStack.getOrDefault(ModDataComponents.CANVAS_GENERATION.get(), 0);
        }
        this.glass = canvasStack.getItem() instanceof ItemCanvas ic && ic.isGlass();
        this.sidesActive = canvasStack.getOrDefault(ModDataComponents.CANVAS_SIDES_ACTIVE.get(), false);
        List<Integer> sideList = canvasStack.get(ModDataComponents.CANVAS_SIDE_PIXELS.get());
        if (sideList != null && sideList.size() == CanvasSides.count(canvasType)) {
            this.sidePixels = sideList.stream().mapToInt(Integer::intValue).toArray();
        }
    }

    @Override
    public void init() {
        canvasX = (this.width - canvasWidth) / 2;
        if (canvasType.equals(CanvasType.LONG)) canvasY += 40;
    }

    private int getPixelAt(int x, int y) {
        return this.pixels == null ? 0xFFF9FFFE : this.pixels[y * canvasPixelWidth + x];
    }

    private int getSidePixel(int idx) {
        return (sidePixels != null && idx >= 0 && idx < sidePixels.length) ? sidePixels[idx] : CanvasSides.DEFAULT_COLOR;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float f) {
        if (glass) {
            for (int i = 0; i < canvasPixelHeight; i++) {
                for (int j = 0; j < canvasPixelWidth; j++) {
                    int x = canvasX + j * canvasPixelScale;
                    int y = canvasY + i * canvasPixelScale;
                    int color = ((i + j) & 1) == 0 ? 0xFFBFBFBF : 0xFF7F7F7F;
                    guiGraphics.fill(x, y, x + canvasPixelScale, y + canvasPixelScale, color);
                }
            }
        }
        for (int i = 0; i < canvasPixelHeight; i++) {
            for (int j = 0; j < canvasPixelWidth; j++) {
                int x = canvasX + j * canvasPixelScale;
                int y = canvasY + i * canvasPixelScale;
                guiGraphics.fill(x, y, x + canvasPixelScale, y + canvasPixelScale, getPixelAt(j, i));
            }
        }
        if (sidesActive) {
            int scale = canvasPixelScale;
            for (int k = 0; k < canvasPixelWidth; k++) {
                int x = canvasX + k * scale;
                guiGraphics.fill(x, canvasY - scale, x + scale, canvasY, getSidePixel(CanvasSides.topOffset() + k));
                guiGraphics.fill(x, canvasY + canvasPixelHeight * scale, x + scale, canvasY + canvasPixelHeight * scale + scale, getSidePixel(CanvasSides.bottomOffset(canvasType) + k));
            }
            for (int i = 0; i < canvasPixelHeight; i++) {
                int y = canvasY + i * scale;
                guiGraphics.fill(canvasX - scale, y, canvasX, y + scale, getSidePixel(CanvasSides.leftOffset(canvasType) + i));
                guiGraphics.fill(canvasX + canvasWidth, y, canvasX + canvasWidth + scale, y + scale, getSidePixel(CanvasSides.rightOffset(canvasType) + i));
            }
        }

        if (generation > 0 && !canvasTitle.isEmpty()) {
            String title = canvasTitle + " " + I18n.get("canvas.byAuthor", authorName);
            String gen = "(" + I18n.get("canvas.generation." + (generation - 1)) + ")";

            int titleWidth = this.font.width(title);
            int genWidth = this.font.width(gen);

            float titleX = (canvasX + (canvasWidth - titleWidth) / 2.0f);
            float genX = (canvasX + (canvasWidth - genWidth) / 2.0f);
            float minX = Math.min(genX, titleX);
            float maxX = Math.max(genX + genWidth, titleX + titleWidth);

            guiGraphics.fill((int) (minX - 10), canvasY - 30, (int) (maxX + 10), canvasY - 4, 0xFFEEEEEE);
            guiGraphics.drawString(font, title, (int) titleX, canvasY - 25, 0xFF111111, false);
            guiGraphics.drawString(font, gen, (int) genX, canvasY - 14, 0xFF444444, false);
        }
    }

    @Override
    public void tick() {
        if (easel != null && player != null && (easel.getItem().isEmpty() || easel.isRemoved() || easel.distanceToSqr(player) > 64)) {
            this.onClose();
        }
        super.tick();
    }
}
