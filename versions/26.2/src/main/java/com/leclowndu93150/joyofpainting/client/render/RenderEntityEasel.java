package com.leclowndu93150.joyofpainting.client.render;

import com.google.common.collect.Lists;
import com.leclowndu93150.joyofpainting.JoyOfPainting;
import com.leclowndu93150.joyofpainting.client.JoyOfPaintingClient;
import com.leclowndu93150.joyofpainting.client.render.state.EntityEaselRenderState;
import com.leclowndu93150.joyofpainting.entity.EntityEasel;
import com.leclowndu93150.joyofpainting.item.ItemCanvas;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.joml.Quaternionf;
import javax.annotation.Nullable;

import java.util.List;

public class RenderEntityEasel extends EntityRenderer<EntityEasel, EntityEaselRenderState> implements RenderLayerParent<EntityEaselRenderState, EaselModel> {
    protected final EaselModel model;
    protected final List<RenderLayer<EntityEaselRenderState, EaselModel>> layers = Lists.newArrayList();
    private static final Identifier WOOD_TEXTURE = JoyOfPainting.id("textures/block/birch_long.png");

    public RenderEntityEasel(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.model = new EaselModel(ctx.bakeLayer(JoyOfPaintingClient.EASEL_MAIN_LAYER));
        this.layers.add(new EaselCanvasLayer(this));
    }

    @Override
    public EaselModel getModel() {
        return this.model;
    }

    @Override
    public EntityEaselRenderState createRenderState() {
        return new EntityEaselRenderState();
    }

    @Override
    public void extractRenderState(EntityEasel entity, EntityEaselRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.yRot = entity.getYRot();
        ItemStack stack = entity.getItem();
        state.canvasItem = stack;
        if (stack.getItem() instanceof ItemCanvas itemCanvas) {
            state.hasCanvas = true;
            state.canvasType = itemCanvas.getCanvasType();
            state.canvasGlass = itemCanvas.isGlass();
            state.canvasWidth = itemCanvas.getWidth();
            state.canvasHeight = itemCanvas.getHeight();
        } else {
            state.hasCanvas = false;
            state.canvasType = null;
        }
    }

    @Override
    public void submit(EntityEaselRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.yRot));
        poseStack.mulPose(new Quaternionf().rotationXYZ((float) Math.PI, 0, 0));
        poseStack.translate(0, -1.5, 0);

        submitNodeCollector.submitModel(this.model, state, poseStack, WOOD_TEXTURE, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);

        for (RenderLayer<EntityEaselRenderState, EaselModel> layer : layers) {
            layer.submit(poseStack, submitNodeCollector, state.lightCoords, state, state.yRot, 0);
        }

        poseStack.popPose();
        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    @Override
    protected boolean shouldShowName(EntityEasel easel, double distanceToCameraSq) {
        HitResult result = Minecraft.getInstance().hitResult;
        if (result instanceof EntityHitResult ehr
                && ehr.getEntity() == easel
                && !easel.getItem().isEmpty()
                && ItemCanvas.hasTitle(easel.getItem())) {
            float range = easel.isDiscrete() ? 32.0F : 64.0F;
            return distanceToCameraSq < range * range;
        }
        return false;
    }

    @Override
    protected @Nullable Component getNameTag(EntityEasel entity) {
        ItemStack stack = entity.getItem();
        if (!stack.isEmpty() && ItemCanvas.hasTitle(stack)) {
            return ItemCanvas.getFullLabel(stack);
        }
        return super.getNameTag(entity);
    }

    @Override
    protected void submitNameDisplay(EntityEaselRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0, -0.5, 0);
        super.submitNameDisplay(state, poseStack, submitNodeCollector, camera);
        poseStack.popPose();
    }
}
