package com.leclowndu93150.joyofpainting.client.render;

import com.google.common.collect.Lists;
import com.leclowndu93150.joyofpainting.JoyOfPainting;
import com.leclowndu93150.joyofpainting.client.JoyOfPaintingClient;
import com.leclowndu93150.joyofpainting.entity.EntityEasel;
import com.leclowndu93150.joyofpainting.item.ItemCanvas;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class RenderEntityEasel extends EntityRenderer<EntityEasel> implements RenderLayerParent<EntityEasel, EaselModel> {
    protected final EaselModel model;
    protected final List<RenderLayer<EntityEasel, EaselModel>> layers = Lists.newArrayList();
    private static final ResourceLocation WOOD_TEXTURE = JoyOfPainting.id("textures/block/birch_long.png");

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
    public ResourceLocation getTextureLocation(EntityEasel entity) {
        return WOOD_TEXTURE;
    }

    @Override
    public void render(EntityEasel entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getYRot()));
        this.model.setupAnim(entity, 0, 0, 0, 0, 0);
        poseStack.mulPose(new Quaternionf().rotationXYZ((float) Math.PI, 0, 0));
        poseStack.translate(0, -1.5, 0);

        RenderType renderType = this.model.renderType(WOOD_TEXTURE);
        VertexConsumer vc = buffer.getBuffer(renderType);
        int overlay = OverlayTexture.pack(OverlayTexture.u(0), OverlayTexture.v(false));
        this.model.renderToBuffer(poseStack, vc, packedLight, overlay, 1.0F, 1.0F, 1.0F, 1.0F);

        for (RenderLayer<EntityEasel, EaselModel> layer : layers) {
            layer.render(poseStack, buffer, packedLight, entity, 0, 0, partialTicks, 0, 0, 0);
        }

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    protected boolean shouldShowName(EntityEasel easel) {
        HitResult result = Minecraft.getInstance().hitResult;
        if (result instanceof EntityHitResult entityHitResult && Minecraft.renderNames()
                && entityHitResult.getEntity() == easel
                && !easel.getItem().isEmpty()
                && ItemCanvas.hasTitle(easel.getItem())) {
            double dist = this.entityRenderDispatcher.distanceToSqr(easel);
            float range = easel.isDiscrete() ? 32.0F : 64.0F;
            return dist < range * range;
        }
        return false;
    }

    @Override
    protected void renderNameTag(EntityEasel entity, Component displayName, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0, -0.5, 0);
        super.renderNameTag(entity, ItemCanvas.getFullLabel(entity.getItem()), poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}
