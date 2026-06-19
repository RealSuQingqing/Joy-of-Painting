package com.leclowndu93150.joyofpainting.client.render;

import com.google.common.collect.Maps;
import com.leclowndu93150.joyofpainting.CanvasSides;
import com.leclowndu93150.joyofpainting.PaletteUtil;
import com.leclowndu93150.joyofpainting.entity.EntityCanvas;
import com.leclowndu93150.joyofpainting.nbt.NbtCanvas;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import javax.annotation.Nullable;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class RenderEntityCanvas extends EntityRenderer<EntityCanvas> {
    public static RenderEntityCanvas theInstance;
    private static final ResourceLocation BACK_LOCATION = new ResourceLocation("minecraft", "textures/block/birch_planks.png");
    private static final ResourceLocation GLASS_FRAME_LOCATION = new ResourceLocation("minecraft", "textures/block/glass.png");
    private static final int[] EMPTY_PIXELS;
    private static final int GLASS_FRAME_RGBA = 0x99DCE8FF;

    static {
        EMPTY_PIXELS = new int[1024];
        for (int i = 0; i < 1024; i++) EMPTY_PIXELS[i] = PaletteUtil.Color.WHITE.rgbVal();
    }

    private final TextureManager textureManager;
    private final Map<String, Instance> loadedCanvases = Maps.newHashMap();
    private final ResourceLocation whiteLocation;

    public RenderEntityCanvas(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.textureManager = Minecraft.getInstance().getTextureManager();
        this.whiteLocation = createWhiteTexture();
        theInstance = this;
    }

    private ResourceLocation createWhiteTexture() {
        DynamicTexture texture = new DynamicTexture(1, 1, false);
        NativeImage image = texture.getPixels();
        if (image != null) {
            image.setPixelRGBA(0, 0, 0xFFFFFFFF);
            texture.upload();
        }
        return this.textureManager.register("canvas_side_white", texture);
    }

    @Override
    public void render(EntityCanvas entity, float entityYaw, float partialTicks, PoseStack ms, MultiBufferSource buffer, int packedLight) {
        Instance instance = getCanvasRendererInstance(entity);
        instance.sidesActive = entity.hasSidesActive();
        instance.sidePixels = entity.getSidePixelsArray();
        instance.render(entity, entity.getYRot(), entity.getXRot(), ms, buffer, entity.getDirection(), packedLight, entity.isGlass());
        super.render(entity, entityYaw, partialTicks, ms, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityCanvas entity) {
        Instance instance = loadedCanvases.get(rendererKey(entity.getCanvasID(), entity.getWidth(), entity.getHeight()));
        return instance == null ? BACK_LOCATION : instance.location;
    }

    private Instance getCanvasRendererInstance(EntityCanvas canvas) {
        return getCanvasRendererInstance(canvas.getCanvasID(), canvas.getVersion(), canvas.getWidth(), canvas.getHeight());
    }

    public Instance getCanvasRendererInstance(ItemStack canvasStack, int width, int height) {
        String canvasId = NbtCanvas.getName(canvasStack);
        int version = NbtCanvas.getVersion(canvasStack);
        if (version <= 0) version = 1;
        final int v = version;
        boolean sidesActive = NbtCanvas.getSidesActive(canvasStack);
        int[] sidePixels = NbtCanvas.hasSidePixels(canvasStack) ? NbtCanvas.getSidePixels(canvasStack) : new int[0];
        EntityCanvas.PICTURES.compute(canvasId, (key, existing) -> {
            if (existing == null || existing.version() < v) {
                return new EntityCanvas.Picture(v, NbtCanvas.getPixels(canvasStack), sidesActive, sidePixels);
            }
            return existing;
        });
        Instance instance = getCanvasRendererInstance(canvasId, version, width, height);
        instance.sidesActive = sidesActive;
        instance.sidePixels = sidePixels;
        return instance;
    }

    private static String rendererKey(String canvasId, int width, int height) {
        return canvasId + "-" + width + "x" + height;
    }

    Instance getCanvasRendererInstance(String canvasId, int version, int width, int height) {
        String key = rendererKey(canvasId, width, height);
        Instance instance = this.loadedCanvases.get(key);
        if (instance == null) {
            instance = new Instance(key, canvasId, version, width, height);
            this.loadedCanvases.put(key, instance);
        } else if (instance.version < version || !instance.loaded) {
            instance.updateCanvasTexture(canvasId, version);
        }
        return instance;
    }

    private static int swapRedBlue(int argb) {
        int a = argb & 0xFF000000;
        int r = (argb >> 16) & 0xFF;
        int g = argb & 0x0000FF00;
        int b = (argb & 0xFF) << 16;
        return a | b | g | r;
    }

    @OnlyIn(Dist.CLIENT)
    public class Instance implements AutoCloseable {
        int version = 0;
        final int width;
        final int height;
        boolean loaded;
        boolean started;
        boolean sidesActive;
        int[] sidePixels = new int[0];
        public final DynamicTexture canvasTexture;
        public final ResourceLocation location;

        private Instance(String key, String canvasId, int version, int width, int height) {
            this.width = width;
            this.height = height;
            this.canvasTexture = new DynamicTexture(width, height, true);
            this.location = textureManager.register("canvas/" + key, this.canvasTexture);
            updateCanvasTexture(canvasId, version);
        }

        private void updateCanvasTexture(String canvasId, int version) {
            int[] pixels = EMPTY_PIXELS;
            if (EntityCanvas.PICTURES.containsKey(canvasId)) {
                int[] p = EntityCanvas.PICTURES.get(canvasId).pixels();
                if (p != null) {
                    pixels = p;
                    loaded = true;
                }
            }
            if (loaded || !started) {
                if (pixels.length < width * height) return;
                NativeImage image = canvasTexture.getPixels();
                if (image != null) {
                    for (int y = 0; y < height; ++y) {
                        for (int x = 0; x < width; ++x) {
                            image.setPixelRGBA(x, y, swapRedBlue(pixels[x + y * width]));
                        }
                    }
                    canvasTexture.upload();
                    this.version = version;
                    this.started = true;
                }
            }
        }

        public void render(@Nullable EntityCanvas canvas, float yaw, float pitch, PoseStack ms, MultiBufferSource buffer, Direction facing, int packedLight, boolean glass) {
            render(canvas, yaw, pitch, ms, buffer, facing, packedLight, glass, -1);
        }

        public void render(@Nullable EntityCanvas canvas, float yaw, float pitch, PoseStack ms, MultiBufferSource buffer, Direction facing, int packedLight, boolean glass, int tint) {
            final float wScale = width / 16.0f;
            final float hScale = height / 16.0f;

            ms.pushPose();
            float xOffset = facing.getStepX();
            float yOffset = facing.getStepY();
            float zOffset = facing.getStepZ();

            if (canvas != null && canvas.getRotation() > 0) {
                ms.mulPose(Axis.XP.rotationDegrees(pitch));
                ms.mulPose(Axis.YP.rotationDegrees(180.f - yaw));
                ms.mulPose(Axis.ZP.rotationDegrees(90.f * canvas.getRotation()));
                ms.mulPose(Axis.YP.rotationDegrees(-180.f + yaw));
                ms.mulPose(Axis.XP.rotationDegrees(-pitch));
            }

            float f = 1.0f / 32.0f;
            if (canvas != null) {
                if (facing.getAxis().isHorizontal()) {
                    ms.translate(zOffset * 0.5d * wScale, -0.5d * hScale, -xOffset * 0.5d * wScale);
                } else {
                    ms.translate(0.5 * wScale, 0, (yOffset > 0 ? 0.5 : -0.5) * wScale);
                }
            } else {
                ms.translate(0.75d, 0.5d, 0.5d);
                if (wScale > 1 || hScale > 1) f /= 3.3f;
                else f /= 2.0f;
            }

            ms.mulPose(Axis.XP.rotationDegrees(pitch));
            ms.mulPose(Axis.YP.rotationDegrees(180 - yaw));
            ms.scale(f, f, f);

            Matrix4f m = ms.last().pose();
            PoseStack.Pose pose = ms.last();
            float w32 = 32.0f * wScale;
            float h32 = 32.0f * hScale;

            RenderType frontType = glass ? RenderType.entityCutout(location) : RenderType.entitySolid(location);
            VertexConsumer front = buffer.getBuffer(frontType);
            addVertex(front, m, pose, 0, h32, -1, 1, 0, packedLight, 0, 0, -1, 0xFFFFFFFF);
            addVertex(front, m, pose, w32, h32, -1, 0, 0, packedLight, 0, 0, -1, 0xFFFFFFFF);
            addVertex(front, m, pose, w32, 0, -1, 0, 1, packedLight, 0, 0, -1, 0xFFFFFFFF);
            addVertex(front, m, pose, 0, 0, -1, 1, 1, packedLight, 0, 0, -1, 0xFFFFFFFF);

            if (glass) {
                VertexConsumer back = buffer.getBuffer(RenderType.entityCutout(location));
                addVertex(back, m, pose, 0, 0, 1, 1, 1, packedLight, 0, 0, 1, 0xFFFFFFFF);
                addVertex(back, m, pose, w32, 0, 1, 0, 1, packedLight, 0, 0, 1, 0xFFFFFFFF);
                addVertex(back, m, pose, w32, h32, 1, 0, 0, packedLight, 0, 0, 1, 0xFFFFFFFF);
                addVertex(back, m, pose, 0, h32, 1, 1, 0, packedLight, 0, 0, 1, 0xFFFFFFFF);
                if (tint != -1) {
                    drawGlassTint(buffer, m, pose, w32, h32, packedLight, tint);
                }
                if (sidesActive) {
                    renderPaintedSides(buffer, m, pose, w32, h32, packedLight, true);
                } else {
                    renderGlassFrame(buffer, m, pose, w32, h32, packedLight);
                }
            } else {
                VertexConsumer back = buffer.getBuffer(RenderType.entitySolid(BACK_LOCATION));
                drawWoodenSidesAndBack(back, m, pose, w32, h32, packedLight);
                renderPaintedSides(buffer, m, pose, w32, h32, packedLight, false);
            }

            ms.popPose();
        }

        private void drawWoodenSidesAndBack(VertexConsumer back, Matrix4f m, PoseStack.Pose pose, float w32, float h32, int packedLight) {
            final float sideWidth = 1.0f / 16.0f;
            addVertex(back, m, pose, 0, 0, 1, 0, 0, packedLight, 0, 0, 1, 0xFFFFFFFF);
            addVertex(back, m, pose, w32, 0, 1, 1, 0, packedLight, 0, 0, 1, 0xFFFFFFFF);
            addVertex(back, m, pose, w32, h32, 1, 1, 1, packedLight, 0, 0, 1, 0xFFFFFFFF);
            addVertex(back, m, pose, 0, h32, 1, 0, 1, packedLight, 0, 0, 1, 0xFFFFFFFF);
            if (sidesActive) return;
            addVertex(back, m, pose, 0, 0, 1, sideWidth, 0, packedLight, -1, 0, 0, 0xFFFFFFFF);
            addVertex(back, m, pose, 0, h32, 1, sideWidth, 1, packedLight, -1, 0, 0, 0xFFFFFFFF);
            addVertex(back, m, pose, 0, h32, -1, 0, 1, packedLight, -1, 0, 0, 0xFFFFFFFF);
            addVertex(back, m, pose, 0, 0, -1, 0, 0, packedLight, -1, 0, 0, 0xFFFFFFFF);
            addVertex(back, m, pose, 0, h32, 1, 0, 0, packedLight, 0, 1, 0, 0xFFFFFFFF);
            addVertex(back, m, pose, w32, h32, 1, 1, 0, packedLight, 0, 1, 0, 0xFFFFFFFF);
            addVertex(back, m, pose, w32, h32, -1, 1, sideWidth, packedLight, 0, 1, 0, 0xFFFFFFFF);
            addVertex(back, m, pose, 0, h32, -1, 0, sideWidth, packedLight, 0, 1, 0, 0xFFFFFFFF);
            addVertex(back, m, pose, w32, 0, -1, 0, 0, packedLight, 1, 0, 0, 0xFFFFFFFF);
            addVertex(back, m, pose, w32, h32, -1, 0, 1, packedLight, 1, 0, 0, 0xFFFFFFFF);
            addVertex(back, m, pose, w32, h32, 1, sideWidth, 1, packedLight, 1, 0, 0, 0xFFFFFFFF);
            addVertex(back, m, pose, w32, 0, 1, sideWidth, 0, packedLight, 1, 0, 0, 0xFFFFFFFF);
            addVertex(back, m, pose, 0, 0, -1, 0, 1, packedLight, 0, -1, 0, 0xFFFFFFFF);
            addVertex(back, m, pose, w32, 0, -1, 1, 1, packedLight, 0, -1, 0, 0xFFFFFFFF);
            addVertex(back, m, pose, w32, 0, 1, 1, 1 - sideWidth, packedLight, 0, -1, 0, 0xFFFFFFFF);
            addVertex(back, m, pose, 0, 0, 1, 0, 1 - sideWidth, packedLight, 0, -1, 0, 0xFFFFFFFF);
        }

        private void renderGlassFrame(MultiBufferSource buffer, Matrix4f m, PoseStack.Pose pose, float w32, float h32, int packedLight) {
            VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(GLASS_FRAME_LOCATION));
            double eps = 0.001D;
            float depth = 1.0F / 16.0F;
            addVertex(vc, m, pose, eps, eps, 1.0F, 0.0F, 0.0F, packedLight, -1.0F, 0.0F, 0.0F, GLASS_FRAME_RGBA);
            addVertex(vc, m, pose, eps, h32 - eps, 1.0F, 0.0F, 1.0F, packedLight, -1.0F, 0.0F, 0.0F, GLASS_FRAME_RGBA);
            addVertex(vc, m, pose, eps, h32 - eps, -1.0F, depth, 1.0F, packedLight, -1.0F, 0.0F, 0.0F, GLASS_FRAME_RGBA);
            addVertex(vc, m, pose, eps, eps, -1.0F, depth, 0.0F, packedLight, -1.0F, 0.0F, 0.0F, GLASS_FRAME_RGBA);
            addVertex(vc, m, pose, eps, h32 - eps, 1.0F, 0.0F, 0.0F, packedLight, 0.0F, 1.0F, 0.0F, GLASS_FRAME_RGBA);
            addVertex(vc, m, pose, w32 - eps, h32 - eps, 1.0F, 1.0F, 0.0F, packedLight, 0.0F, 1.0F, 0.0F, GLASS_FRAME_RGBA);
            addVertex(vc, m, pose, w32 - eps, h32 - eps, -1.0F, 1.0F, depth, packedLight, 0.0F, 1.0F, 0.0F, GLASS_FRAME_RGBA);
            addVertex(vc, m, pose, eps, h32 - eps, -1.0F, 0.0F, depth, packedLight, 0.0F, 1.0F, 0.0F, GLASS_FRAME_RGBA);
            addVertex(vc, m, pose, w32 - eps, eps, -1.0F, 0.0F, 0.0F, packedLight, 1.0F, 0.0F, 0.0F, GLASS_FRAME_RGBA);
            addVertex(vc, m, pose, w32 - eps, h32 - eps, -1.0F, 0.0F, 1.0F, packedLight, 1.0F, 0.0F, 0.0F, GLASS_FRAME_RGBA);
            addVertex(vc, m, pose, w32 - eps, h32 - eps, 1.0F, depth, 1.0F, packedLight, 1.0F, 0.0F, 0.0F, GLASS_FRAME_RGBA);
            addVertex(vc, m, pose, w32 - eps, eps, 1.0F, depth, 0.0F, packedLight, 1.0F, 0.0F, 0.0F, GLASS_FRAME_RGBA);
            addVertex(vc, m, pose, eps, eps, -1.0F, 0.0F, 0.0F, packedLight, 0.0F, -1.0F, 0.0F, GLASS_FRAME_RGBA);
            addVertex(vc, m, pose, w32 - eps, eps, -1.0F, 1.0F, 0.0F, packedLight, 0.0F, -1.0F, 0.0F, GLASS_FRAME_RGBA);
            addVertex(vc, m, pose, w32 - eps, eps, 1.0F, 1.0F, depth, packedLight, 0.0F, -1.0F, 0.0F, GLASS_FRAME_RGBA);
            addVertex(vc, m, pose, eps, eps, 1.0F, 0.0F, depth, packedLight, 0.0F, -1.0F, 0.0F, GLASS_FRAME_RGBA);
        }

        private void drawGlassTint(MultiBufferSource buffer, Matrix4f m, PoseStack.Pose pose, float w32, float h32, int packedLight, int tint) {
            VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(GLASS_FRAME_LOCATION));
            int rgba = 0x66000000 | (tint & 0x00FFFFFF);
            addVertex(vc, m, pose, 0, h32, 0, 1, 0, packedLight, 0, 0, -1, rgba);
            addVertex(vc, m, pose, w32, h32, 0, 0, 0, packedLight, 0, 0, -1, rgba);
            addVertex(vc, m, pose, w32, 0, 0, 0, 1, packedLight, 0, 0, -1, rgba);
            addVertex(vc, m, pose, 0, 0, 0, 1, 1, packedLight, 0, 0, -1, rgba);
        }

        private void renderPaintedSides(MultiBufferSource buffer, Matrix4f m, PoseStack.Pose pose, float w32, float h32, int packedLight, boolean skipTransparent) {
            if (!sidesActive || sidePixels == null || sidePixels.length == 0) return;
            VertexConsumer vc = buffer.getBuffer(RenderType.entityCutoutNoCull(whiteLocation));
            double eps = 0.001D;
            float unit = 2.0F;
            int topOffset = 0;
            int bottomOffset = this.width;
            int leftOffset = bottomOffset + this.width;
            int rightOffset = leftOffset + this.height;

            for (int k = 0; k < width; k++) {
                int c = sideColor(topOffset + k);
                if (!skipTransparent || ((c >>> 24) & 0xFF) != 0) {
                    float x0 = w32 - (k + 1) * unit;
                    float x1 = w32 - k * unit;
                    addSideVertex(vc, m, pose, x0, h32 - eps, 1.0F, c, packedLight, 0.0F, 1.0F, 0.0F);
                    addSideVertex(vc, m, pose, x1, h32 - eps, 1.0F, c, packedLight, 0.0F, 1.0F, 0.0F);
                    addSideVertex(vc, m, pose, x1, h32 - eps, -1.0F, c, packedLight, 0.0F, 1.0F, 0.0F);
                    addSideVertex(vc, m, pose, x0, h32 - eps, -1.0F, c, packedLight, 0.0F, 1.0F, 0.0F);
                }
            }
            for (int k = 0; k < width; k++) {
                int c = sideColor(bottomOffset + k);
                if (!skipTransparent || ((c >>> 24) & 0xFF) != 0) {
                    float x0 = w32 - (k + 1) * unit;
                    float x1 = w32 - k * unit;
                    addSideVertex(vc, m, pose, x0, eps, -1.0F, c, packedLight, 0.0F, -1.0F, 0.0F);
                    addSideVertex(vc, m, pose, x1, eps, -1.0F, c, packedLight, 0.0F, -1.0F, 0.0F);
                    addSideVertex(vc, m, pose, x1, eps, 1.0F, c, packedLight, 0.0F, -1.0F, 0.0F);
                    addSideVertex(vc, m, pose, x0, eps, 1.0F, c, packedLight, 0.0F, -1.0F, 0.0F);
                }
            }
            for (int i = 0; i < height; i++) {
                int c = sideColor(leftOffset + i);
                if (!skipTransparent || ((c >>> 24) & 0xFF) != 0) {
                    float y0 = h32 - (i + 1) * unit;
                    float y1 = h32 - i * unit;
                    addSideVertex(vc, m, pose, w32 - eps, y0, -1.0F, c, packedLight, 1.0F, 0.0F, 0.0F);
                    addSideVertex(vc, m, pose, w32 - eps, y1, -1.0F, c, packedLight, 1.0F, 0.0F, 0.0F);
                    addSideVertex(vc, m, pose, w32 - eps, y1, 1.0F, c, packedLight, 1.0F, 0.0F, 0.0F);
                    addSideVertex(vc, m, pose, w32 - eps, y0, 1.0F, c, packedLight, 1.0F, 0.0F, 0.0F);
                }
            }
            for (int i = 0; i < height; i++) {
                int c = sideColor(rightOffset + i);
                if (!skipTransparent || ((c >>> 24) & 0xFF) != 0) {
                    float y0 = h32 - (i + 1) * unit;
                    float y1 = h32 - i * unit;
                    addSideVertex(vc, m, pose, eps, y0, 1.0F, c, packedLight, -1.0F, 0.0F, 0.0F);
                    addSideVertex(vc, m, pose, eps, y1, 1.0F, c, packedLight, -1.0F, 0.0F, 0.0F);
                    addSideVertex(vc, m, pose, eps, y1, -1.0F, c, packedLight, -1.0F, 0.0F, 0.0F);
                    addSideVertex(vc, m, pose, eps, y0, -1.0F, c, packedLight, -1.0F, 0.0F, 0.0F);
                }
            }
        }

        private int sideColor(int index) {
            if (sidePixels == null || index < 0 || index >= sidePixels.length) return CanvasSides.DEFAULT_COLOR;
            return sidePixels[index];
        }

        private void addSideVertex(VertexConsumer vb, Matrix4f m, PoseStack.Pose pose,
                                   double x, double y, double z, int color, int lightmap,
                                   float nx, float ny, float nz) {
            addVertex(vb, m, pose, x, y, z, 0.0F, 0.0F, lightmap, nx, ny, nz, color);
        }

        private void addVertex(VertexConsumer vb, Matrix4f m, PoseStack.Pose pose,
                               double x, double y, double z, float tx, float ty, int lightmap,
                               float nx, float ny, float nz, int rgba) {
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

        @Override
        public void close() {
            this.canvasTexture.close();
            textureManager.release(location);
        }
    }
}
