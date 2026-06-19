package com.leclowndu93150.joyofpainting.client.render;

import com.leclowndu93150.joyofpainting.JoyOfPainting;
import com.leclowndu93150.joyofpainting.item.ItemCanvas;
import com.leclowndu93150.joyofpainting.registry.ModDataComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class CanvasItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation BACK_LOCATION = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/block/birch_planks.png");
    private static final ResourceLocation EMPTY_CANVAS_LOCATION = JoyOfPainting.id("textures/block/empty.png");

    private static CanvasItemRenderer INSTANCE;

    public CanvasItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    public static CanvasItemRenderer getInstance() {
        if (INSTANCE == null) INSTANCE = new CanvasItemRenderer();
        return INSTANCE;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack ms, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        if (!(stack.getItem() instanceof ItemCanvas itemCanvas)) return;
        boolean rendered = false;
        if (stack.has(ModDataComponents.CANVAS_PIXELS.get()) && RenderEntityCanvas.theInstance != null) {
            RenderEntityCanvas.Instance ins = RenderEntityCanvas.theInstance.getCanvasRendererInstance(stack, itemCanvas.getWidth(), itemCanvas.getHeight());
            if (ins != null) {
                ins.render(null, 0, 0, ms, buffer, Direction.UP, combinedLight, itemCanvas.isGlass());
                rendered = true;
            }
        }
        if (!rendered) {
            renderEmptyCanvas(ms, buffer, itemCanvas.getWidth(), itemCanvas.getHeight(), combinedLight);
        }
    }

    private void addVertex(VertexConsumer vb, Matrix4f m, PoseStack.Pose pose, double x, double y, double z, float tx, float ty, int lightmap, float nx, float ny, float nz) {
        vb.addVertex(m, (float) x, (float) y, (float) z)
                .setColor(255, 255, 255, 255)
                .setUv(tx, ty)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightmap)
                .setNormal(pose, nx, ny, nz);
    }

    private void renderEmptyCanvas(PoseStack ms, MultiBufferSource buffer, float width, float height, int packedLight) {
        final float wScale = width / 16.0f;
        final float hScale = height / 16.0f;

        ms.pushPose();
        float xOffset = Direction.UP.getStepX();
        float yOffset = Direction.UP.getStepY();
        float zOffset = Direction.UP.getStepZ();

        float f = 1.0f / 32.0f;
        ms.translate(0.75, 0.5, 0.5);
        if (wScale > 1 || hScale > 1) f /= 3.3f;
        else f /= 2.0f;
        ms.mulPose(Axis.YP.rotationDegrees(180));
        ms.scale(f, f, f);

        Matrix4f m = ms.last().pose();
        PoseStack.Pose pose = ms.last();
        VertexConsumer vb = buffer.getBuffer(RenderType.entitySolid(EMPTY_CANVAS_LOCATION));

        addVertex(vb, m, pose, 0.0F, 32.0F * hScale, -1.0F, 1.0F, 0.0F, packedLight, xOffset, yOffset, zOffset);
        addVertex(vb, m, pose, 32.0F * wScale, 32.0F * hScale, -1.0F, 0.0F, 0.0F, packedLight, xOffset, yOffset, zOffset);
        addVertex(vb, m, pose, 32.0F * wScale, 0.0F, -1.0F, 0.0F, 1.0F, packedLight, xOffset, yOffset, zOffset);
        addVertex(vb, m, pose, 0.0F, 0.0F, -1.0F, 1.0F, 1.0F, packedLight, xOffset, yOffset, zOffset);

        vb = buffer.getBuffer(RenderType.entitySolid(BACK_LOCATION));
        final float sideWidth = 1.0F / 16.0F;
        addVertex(vb, m, pose, 0.0D, 0.0D, 1.0D, 0.0F, 0.0F, packedLight, xOffset, yOffset, zOffset);
        addVertex(vb, m, pose, 32.0D * wScale, 0.0D, 1.0D, 1.0F, 0.0F, packedLight, xOffset, yOffset, zOffset);
        addVertex(vb, m, pose, 32.0D * wScale, 32.0D * hScale, 1.0D, 1.0F, 1.0F, packedLight, xOffset, yOffset, zOffset);
        addVertex(vb, m, pose, 0.0D, 32.0D * hScale, 1.0D, 0.0F, 1.0F, packedLight, xOffset, yOffset, zOffset);

        ms.popPose();
    }
}
