package com.leclowndu93150.joyofpainting.client.render;

import com.leclowndu93150.joyofpainting.client.render.state.EntityEaselRenderState;
import com.leclowndu93150.joyofpainting.registry.ModDataComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;

public class EaselCanvasLayer extends RenderLayer<EntityEaselRenderState, EaselModel> {
    private static final float BAR_TILT_DEGREES = 15.0F;
    private static final float MODEL_TO_BLOCK = 1.0F / 16.0F;

    public EaselCanvasLayer(RenderLayerParent<EntityEaselRenderState, EaselModel> renderer) {
        super(renderer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, EntityEaselRenderState state, float yRot, float xRot) {
        if (!state.hasCanvas || state.canvasType == null) return;
        RenderEntityCanvas canvasRenderer = RenderEntityCanvas.theInstance;
        if (canvasRenderer == null) return;
        boolean hasPixels = state.canvasItem.has(ModDataComponents.CANVAS_PIXELS.get());
        RenderEntityCanvas.Instance instance = canvasRenderer.getCanvasRendererInstance(
                state.canvasItem, state.canvasWidth, state.canvasHeight);
        if (instance == null) return;

        BarPlacement bp = barPlacementFor(state);

        poseStack.pushPose();
        poseStack.translate(0.0F, bp.centerY * MODEL_TO_BLOCK, bp.centerZ * MODEL_TO_BLOCK);
        poseStack.mulPose(Axis.XP.rotationDegrees(-BAR_TILT_DEGREES));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(MODEL_TO_BLOCK / 2.0F, -MODEL_TO_BLOCK / 2.0F, -MODEL_TO_BLOCK / 2.0F);

        final float w32 = 32.0F * (instance.pixelWidth() / 16.0F);
        final float h32 = 32.0F * (instance.pixelHeight() / 16.0F);
        poseStack.translate(-w32 / 2.0F, -h32 / 2.0F, 0.0F);

        if (hasPixels) {
            instance.submitGeometry(poseStack, submitNodeCollector, lightCoords, state.canvasGlass, -1, false);
        } else {
            instance.submitEmptyGeometry(poseStack, submitNodeCollector, lightCoords, state.canvasGlass);
        }

        poseStack.popPose();
    }

    private record BarPlacement(float centerY, float centerZ) {}

    private static BarPlacement barPlacementFor(EntityEaselRenderState state) {
        float bottomBarY;
        float topBarY;
        float bottomBarZ;
        float topBarZ;
        float yLift;
        switch (state.canvasType) {
            case LONG -> {
                bottomBarY = 13.5F; bottomBarZ = -3.25F;
                topBarY = 16.25F;   topBarZ = -4.0F;
                yLift = -4.0F;
            }
            case LARGE, TALL -> {
                bottomBarY = 16.5F; bottomBarZ = -4.0F;
                topBarY = 9.8F;     topBarZ = -2.25F;
                yLift = -2.0F;
            }
            default -> {
                bottomBarY = 16.1F; bottomBarZ = -4.0F;
                topBarY = 16.75F;   topBarZ = -4.0F;
                yLift = -2.5F;
            }
        }
        float centerY = (bottomBarY + topBarY) * 0.5F + yLift;
        float centerZ = (bottomBarZ + topBarZ) * 0.5F - 0.5F;
        return new BarPlacement(centerY, centerZ);
    }
}
