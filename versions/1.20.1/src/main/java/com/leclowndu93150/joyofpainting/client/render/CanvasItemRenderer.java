package com.leclowndu93150.joyofpainting.client.render;

import com.leclowndu93150.joyofpainting.JoyOfPainting;
import com.leclowndu93150.joyofpainting.item.ItemCanvas;
import com.leclowndu93150.joyofpainting.nbt.NbtCanvas;
import com.mojang.blaze3d.systems.RenderSystem;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class CanvasItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation BACK_LOCATION = new ResourceLocation("minecraft", "textures/block/birch_planks.png");
    private static final ResourceLocation EMPTY_CANVAS_LOCATION = JoyOfPainting.id("textures/block/empty.png");
    private static final ResourceLocation GLASS_FRAME_LOCATION = new ResourceLocation("minecraft", "textures/block/glass.png");
    private static final int GLASS_INVENTORY_TINT = 0xFFDCE8FF;

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
        if (NbtCanvas.hasPixels(stack) && RenderEntityCanvas.theInstance != null) {
            RenderEntityCanvas.Instance ins = RenderEntityCanvas.theInstance.getCanvasRendererInstance(stack, itemCanvas.getWidth(), itemCanvas.getHeight());
            if (ins != null) {
                int tint = itemCanvas.isGlass() && displayContext == ItemDisplayContext.GUI ? GLASS_INVENTORY_TINT : -1;
                ins.render(null, 0, 0, ms, buffer, Direction.UP, combinedLight, itemCanvas.isGlass(), tint);
                rendered = true;
            }
        }
        if (!rendered) {
            renderEmptyCanvas(ms, buffer, itemCanvas.getWidth(), itemCanvas.getHeight(), combinedLight, itemCanvas.isGlass());
        }
    }

    private void addVertex(VertexConsumer vb, Matrix4f m, PoseStack.Pose pose, double x, double y, double z, float tx, float ty, int lightmap, float nx, float ny, float nz) {
        vb.vertex(m, (float) x, (float) y, (float) z)
                .color(255, 255, 255, 255)
                .uv(tx, ty)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(lightmap)
                .normal(pose.normal(), nx, ny, nz)
                .endVertex();
    }

    private void addVertex(VertexConsumer vb, Matrix4f m, PoseStack.Pose pose, double x, double y, double z, float tx, float ty, int lightmap, float nx, float ny, float nz, int rgba) {
        int a = (rgba >>> 24) & 0xFF;
        int r = (rgba >>> 16) & 0xFF;
        int g = (rgba >>> 8) & 0xFF;
        int b = rgba & 0xFF;
        vb.vertex(m, (float) x, (float) y, (float) z)
                .color(r, g, b, a)
                .uv(tx, ty)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(lightmap)
                .normal(pose.normal(), nx, ny, nz)
                .endVertex();
    }

    private void renderEmptyCanvas(PoseStack ms, MultiBufferSource buffer, float width, float height, int packedLight, boolean glass) {
        final float wScale = width / 16.0f;
        final float hScale = height / 16.0f;

        ms.pushPose();
        float xOffset = Direction.UP.getStepX();
        float yOffset = Direction.UP.getStepY();
        float zOffset = Direction.UP.getStepZ();

        float f = 1.0f / 32.0f;
        ms.translate(0.75, 0.5, 0.5);
        if (wScale > 1 || hScale > 1) {
            f /= 3.3f;
        } else {
            f /= 2.0f;
        }
        ms.mulPose(Axis.YP.rotationDegrees(180));
        ms.scale(f, f, f);

        RenderSystem.setShaderTexture(0, EMPTY_CANVAS_LOCATION);
        Matrix4f m = ms.last().pose();
        PoseStack.Pose pose = ms.last();
        float w32 = 32.0F * wScale;
        float h32 = 32.0F * hScale;

        if (glass) {
            renderGlassFrame(buffer, m, pose, w32, h32, packedLight);
            ms.popPose();
            return;
        }

        VertexConsumer vb = buffer.getBuffer(RenderType.entitySolid(EMPTY_CANVAS_LOCATION));

        addVertex(vb, m, pose, 0.0F, h32, -1.0F, 1.0F, 0.0F, packedLight, xOffset, yOffset, zOffset);
        addVertex(vb, m, pose, w32, h32, -1.0F, 0.0F, 0.0F, packedLight, xOffset, yOffset, zOffset);
        addVertex(vb, m, pose, w32, 0.0F, -1.0F, 0.0F, 1.0F, packedLight, xOffset, yOffset, zOffset);
        addVertex(vb, m, pose, 0.0F, 0.0F, -1.0F, 1.0F, 1.0F, packedLight, xOffset, yOffset, zOffset);

        vb = buffer.getBuffer(RenderType.entitySolid(BACK_LOCATION));
        final float sideWidth = 1.0F / 16.0F;
        RenderSystem.setShaderTexture(0, BACK_LOCATION);
        addVertex(vb, m, pose, 0.0D, 0.0D, 1.0D, 0.0F, 0.0F, packedLight, xOffset, yOffset, zOffset);
        addVertex(vb, m, pose, w32, 0.0D, 1.0D, 1.0F, 0.0F, packedLight, xOffset, yOffset, zOffset);
        addVertex(vb, m, pose, w32, h32, 1.0D, 1.0F, 1.0F, packedLight, xOffset, yOffset, zOffset);
        addVertex(vb, m, pose, 0.0D, h32, 1.0D, 0.0F, 1.0F, packedLight, xOffset, yOffset, zOffset);

        addVertex(vb, m, pose, 0.0D, 0.0D, 1.0D, sideWidth, 0.0F, packedLight, xOffset, yOffset, zOffset);
        addVertex(vb, m, pose, 0.0D, h32, 1.0D, sideWidth, 1.0F, packedLight, xOffset, yOffset, zOffset);
        addVertex(vb, m, pose, 0.0D, h32, -1.0D, 0.0F, 1.0F, packedLight, xOffset, yOffset, zOffset);
        addVertex(vb, m, pose, 0.0D, 0.0D, -1.0D, 0.0F, 0.0F, packedLight, xOffset, yOffset, zOffset);

        addVertex(vb, m, pose, 0.0D, h32, 1.0F, 0.0F, 0.0F, packedLight, xOffset, yOffset, zOffset);
        addVertex(vb, m, pose, w32, h32, 1.0F, 1.0F, 0.0F, packedLight, xOffset, yOffset, zOffset);
        addVertex(vb, m, pose, w32, h32, -1.0F, 1.0F, sideWidth, packedLight, xOffset, yOffset, zOffset);
        addVertex(vb, m, pose, 0.0D, h32, -1.0F, 0.0F, sideWidth, packedLight, xOffset, yOffset, zOffset);

        addVertex(vb, m, pose, w32, 0.0D, -1.0F, 0.0F, 0.0F, packedLight, xOffset, yOffset, zOffset);
        addVertex(vb, m, pose, w32, h32, -1.0F, 0.0F, 1.0F, packedLight, xOffset, yOffset, zOffset);
        addVertex(vb, m, pose, w32, h32, 1.0F, sideWidth, 1.0F, packedLight, xOffset, yOffset, zOffset);
        addVertex(vb, m, pose, w32, 0.0D, 1.0F, sideWidth, 0.0F, packedLight, xOffset, yOffset, zOffset);

        addVertex(vb, m, pose, 0.0D, 0.0D, -1.0F, 0.0F, 1.0F, packedLight, xOffset, yOffset, zOffset);
        addVertex(vb, m, pose, w32, 0.0D, -1.0F, 1.0F, 1.0F, packedLight, xOffset, yOffset, zOffset);
        addVertex(vb, m, pose, w32, 0.0D, 1.0F, 1.0F, 1.0F - sideWidth, packedLight, xOffset, yOffset, zOffset);
        addVertex(vb, m, pose, 0.0D, 0.0D, 1.0F, 0.0F, 1.0F - sideWidth, packedLight, xOffset, yOffset, zOffset);

        ms.popPose();
    }

    private void renderGlassFrame(MultiBufferSource buffer, Matrix4f m, PoseStack.Pose pose, float w32, float h32, int packedLight) {
        RenderSystem.setShaderTexture(0, GLASS_FRAME_LOCATION);
        VertexConsumer vb = buffer.getBuffer(RenderType.entityTranslucent(GLASS_FRAME_LOCATION));
        double eps = 0.001D;
        float depth = 1.0F / 16.0F;
        int tint = 0x99DCE8FF;

        addVertex(vb, m, pose, eps, eps, 1.0F, 0.0F, 0.0F, packedLight, -1.0F, 0.0F, 0.0F, tint);
        addVertex(vb, m, pose, eps, h32 - eps, 1.0F, 0.0F, 1.0F, packedLight, -1.0F, 0.0F, 0.0F, tint);
        addVertex(vb, m, pose, eps, h32 - eps, -1.0F, depth, 1.0F, packedLight, -1.0F, 0.0F, 0.0F, tint);
        addVertex(vb, m, pose, eps, eps, -1.0F, depth, 0.0F, packedLight, -1.0F, 0.0F, 0.0F, tint);

        addVertex(vb, m, pose, eps, h32 - eps, 1.0F, 0.0F, 0.0F, packedLight, 0.0F, 1.0F, 0.0F, tint);
        addVertex(vb, m, pose, w32 - eps, h32 - eps, 1.0F, 1.0F, 0.0F, packedLight, 0.0F, 1.0F, 0.0F, tint);
        addVertex(vb, m, pose, w32 - eps, h32 - eps, -1.0F, 1.0F, depth, packedLight, 0.0F, 1.0F, 0.0F, tint);
        addVertex(vb, m, pose, eps, h32 - eps, -1.0F, 0.0F, depth, packedLight, 0.0F, 1.0F, 0.0F, tint);

        addVertex(vb, m, pose, w32 - eps, eps, -1.0F, 0.0F, 0.0F, packedLight, 1.0F, 0.0F, 0.0F, tint);
        addVertex(vb, m, pose, w32 - eps, h32 - eps, -1.0F, 0.0F, 1.0F, packedLight, 1.0F, 0.0F, 0.0F, tint);
        addVertex(vb, m, pose, w32 - eps, h32 - eps, 1.0F, depth, 1.0F, packedLight, 1.0F, 0.0F, 0.0F, tint);
        addVertex(vb, m, pose, w32 - eps, eps, 1.0F, depth, 0.0F, packedLight, 1.0F, 0.0F, 0.0F, tint);

        addVertex(vb, m, pose, eps, eps, -1.0F, 0.0F, 0.0F, packedLight, 0.0F, -1.0F, 0.0F, tint);
        addVertex(vb, m, pose, w32 - eps, eps, -1.0F, 1.0F, 0.0F, packedLight, 0.0F, -1.0F, 0.0F, tint);
        addVertex(vb, m, pose, w32 - eps, eps, 1.0F, 1.0F, depth, packedLight, 0.0F, -1.0F, 0.0F, tint);
        addVertex(vb, m, pose, eps, eps, 1.0F, 0.0F, depth, packedLight, 0.0F, -1.0F, 0.0F, tint);
    }
}
