package com.leclowndu93150.joyofpainting.client.render;

import com.leclowndu93150.joyofpainting.registry.ModDataComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class CanvasSpecialRenderer implements SpecialModelRenderer<CanvasSpecialRenderer.Args> {
    private final int width;
    private final int height;
    private final boolean glass;

    public CanvasSpecialRenderer(int width, int height, boolean glass) {
        this.width = width;
        this.height = height;
        this.glass = glass;
    }

    @Override
    public void submit(@Nullable Args args, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        if (args == null) return;
        RenderEntityCanvas renderer = RenderEntityCanvas.theInstance;
        if (renderer == null) return;
        RenderEntityCanvas.Instance instance = renderer.getInstanceForArgs(
                args.canvasId, args.version, width, height, args.pixels, args.sidesActive, args.sidePixels);
        if (instance == null) return;

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

        final float w32 = 32.0F * (width / 16.0F);
        final float h32 = 32.0F * (height / 16.0F);
        float fit = 1.0F / Math.max(w32, h32);
        poseStack.scale(fit, fit, fit);

        poseStack.translate(-w32 / 2.0F, -h32 / 2.0F, 0.0F);

        instance.submitGeometry(poseStack, submitNodeCollector, lightCoords, glass, -1, false);
        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        output.accept(new Vector3f(-0.5F, -0.5F, -0.0625F));
        output.accept(new Vector3f(0.5F, 0.5F, 0.0625F));
    }

    @Override
    public @Nullable Args extractArgument(ItemStack stack) {
        String canvasId = stack.getOrDefault(ModDataComponents.CANVAS_ID.get(), "");
        if (canvasId.isEmpty()) return null;
        int version = stack.getOrDefault(ModDataComponents.CANVAS_VERSION.get(), 1);
        if (version <= 0) version = 1;
        boolean sidesActive = stack.getOrDefault(ModDataComponents.CANVAS_SIDES_ACTIVE.get(), false);
        List<Integer> sideList = stack.get(ModDataComponents.CANVAS_SIDE_PIXELS.get());
        int[] sidePixels = sideList == null ? new int[0] : sideList.stream().mapToInt(Integer::intValue).toArray();
        List<Integer> pixelList = stack.get(ModDataComponents.CANVAS_PIXELS.get());
        int[] pixels = pixelList == null ? new int[0] : pixelList.stream().mapToInt(Integer::intValue).toArray();
        return new Args(canvasId, version, sidesActive, sidePixels, pixels);
    }

    public record Args(String canvasId, int version, boolean sidesActive, int[] sidePixels, int[] pixels) {
    }

    public record Unbaked(int width, int height, boolean glass) implements SpecialModelRenderer.Unbaked<Args> {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Codec.INT.fieldOf("width").forGetter(Unbaked::width),
                Codec.INT.fieldOf("height").forGetter(Unbaked::height),
                Codec.BOOL.optionalFieldOf("glass", false).forGetter(Unbaked::glass)
        ).apply(i, Unbaked::new));

        @Override
        public SpecialModelRenderer<Args> bake(SpecialModelRenderer.BakingContext context) {
            return new CanvasSpecialRenderer(width, height, glass);
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
