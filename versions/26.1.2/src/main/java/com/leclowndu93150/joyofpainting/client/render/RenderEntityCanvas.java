package com.leclowndu93150.joyofpainting.client.render;

import com.google.common.collect.Maps;
import com.leclowndu93150.joyofpainting.CanvasSides;
import com.leclowndu93150.joyofpainting.PaletteUtil;
import com.leclowndu93150.joyofpainting.client.render.state.EntityCanvasRenderState;
import com.leclowndu93150.joyofpainting.entity.EntityCanvas;
import com.leclowndu93150.joyofpainting.registry.ModDataComponents;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;

public class RenderEntityCanvas extends EntityRenderer<EntityCanvas, EntityCanvasRenderState> {
    public static RenderEntityCanvas theInstance;
    private static final Identifier BACK_LOCATION = Identifier.fromNamespaceAndPath("minecraft", "textures/block/birch_planks.png");
    private static final Identifier GLASS_FRAME_LOCATION = Identifier.fromNamespaceAndPath("minecraft", "textures/block/glass.png");
    private static final Identifier EMPTY_CANVAS_LOCATION = Identifier.fromNamespaceAndPath("joyofpainting", "textures/block/empty.png");
    private static final int[] EMPTY_PIXELS;
    private static final int GLASS_FRAME_RGBA = 0x99DCE8FF;

    static {
        EMPTY_PIXELS = new int[1024];
        for (int i = 0; i < 1024; i++) EMPTY_PIXELS[i] = PaletteUtil.Color.WHITE.rgbVal();
    }

    private final TextureManager textureManager;
    private final Map<String, Instance> loadedCanvases = Maps.newHashMap();
    private final Identifier whiteLocation;

    public RenderEntityCanvas(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.textureManager = Minecraft.getInstance().getTextureManager();
        this.whiteLocation = createWhiteTexture();
        theInstance = this;
    }

    private Identifier createWhiteTexture() {
        DynamicTexture texture = new DynamicTexture(() -> "joyofpainting:canvas_side_white", 1, 1, false);
        NativeImage image = texture.getPixels();
        if (image != null) {
            image.setPixelABGR(0, 0, 0xFFFFFFFF);
            texture.upload();
        }
        Identifier id = Identifier.fromNamespaceAndPath("joyofpainting", "canvas_side_white");
        this.textureManager.register(id, texture);
        return id;
    }

    @Override
    public EntityCanvasRenderState createRenderState() {
        return new EntityCanvasRenderState();
    }

    @Override
    public void extractRenderState(EntityCanvas entity, EntityCanvasRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.canvasId = entity.getCanvasID();
        state.canvasVersion = entity.getVersion();
        state.canvasWidth = entity.getWidth();
        state.canvasHeight = entity.getHeight();
        state.canvasRotation = entity.getRotation();
        state.facing = entity.getDirection();
        state.glass = entity.isGlass();
        state.sidesActive = entity.hasSidesActive();
        state.sidePixels = entity.getSidePixelsArray();
        state.yRot = entity.getYRot();
        state.xRot = entity.getXRot();
    }

    @Override
    public void submit(EntityCanvasRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        Instance instance = getCanvasRendererInstance(state.canvasId, state.canvasVersion, state.canvasWidth, state.canvasHeight);
        instance.sidesActive = state.sidesActive;
        instance.sidePixels = state.sidePixels;

        poseStack.pushPose();
        applyWallTransform(poseStack, state);
        instance.submitGeometry(poseStack, submitNodeCollector, state.lightCoords, state.glass, -1, false);
        poseStack.popPose();

        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    private static void applyWallTransform(PoseStack ms, EntityCanvasRenderState state) {
        float wScale = state.canvasWidth / 16.0f;
        float hScale = state.canvasHeight / 16.0f;
        var facing = state.facing;
        float xOffset = facing.getStepX();
        float yOffset = facing.getStepY();
        float zOffset = facing.getStepZ();

        if (state.canvasRotation > 0) {
            ms.mulPose(Axis.XP.rotationDegrees(state.xRot));
            ms.mulPose(Axis.YP.rotationDegrees(180.f - state.yRot));
            ms.mulPose(Axis.ZP.rotationDegrees(90.f * state.canvasRotation));
            ms.mulPose(Axis.YP.rotationDegrees(-180.f + state.yRot));
            ms.mulPose(Axis.XP.rotationDegrees(-state.xRot));
        }

        if (facing.getAxis().isHorizontal()) {
            ms.translate(zOffset * 0.5d * wScale, -0.5d * hScale, -xOffset * 0.5d * wScale);
        } else {
            ms.translate(0.5 * wScale, 0, (yOffset > 0 ? 0.5 : -0.5) * wScale);
        }

        ms.mulPose(Axis.XP.rotationDegrees(state.xRot));
        ms.mulPose(Axis.YP.rotationDegrees(180 - state.yRot));
        ms.scale(1.0f / 32.0f, 1.0f / 32.0f, 1.0f / 32.0f);
    }

    public Instance getCanvasRendererInstance(ItemStack canvasStack, int width, int height) {
        String canvasId = canvasStack.getOrDefault(ModDataComponents.CANVAS_ID.get(), "");
        int version = canvasStack.getOrDefault(ModDataComponents.CANVAS_VERSION.get(), 1);
        if (version <= 0) version = 1;
        final int v = version;
        boolean sidesActive = canvasStack.getOrDefault(ModDataComponents.CANVAS_SIDES_ACTIVE.get(), false);
        List<Integer> sideList = canvasStack.get(ModDataComponents.CANVAS_SIDE_PIXELS.get());
        int[] sidePixels = sideList == null ? new int[0] : sideList.stream().mapToInt(Integer::intValue).toArray();
        List<Integer> pixelList = canvasStack.get(ModDataComponents.CANVAS_PIXELS.get());
        int[] pixels = pixelList == null ? new int[0] : pixelList.stream().mapToInt(Integer::intValue).toArray();
        EntityCanvas.PICTURES.compute(canvasId, (key, existing) -> {
            if (existing == null || existing.version() < v) {
                return new EntityCanvas.Picture(v, pixels, sidesActive, sidePixels);
            }
            return existing;
        });
        Instance instance = getCanvasRendererInstance(canvasId, version, width, height);
        instance.sidesActive = sidesActive;
        instance.sidePixels = sidePixels;
        return instance;
    }

    public Instance getInstanceForArgs(String canvasId, int version, int width, int height, int[] pixels, boolean sidesActive, int[] sidePixels) {
        if (canvasId.isEmpty()) return null;
        final int v = version;
        EntityCanvas.PICTURES.compute(canvasId, (key, existing) -> {
            if (existing == null || existing.version() < v) {
                return new EntityCanvas.Picture(v, pixels, sidesActive, sidePixels);
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

    public class Instance implements AutoCloseable {
        int version = 0;
        final int width;
        final int height;
        boolean loaded;
        boolean started;
        boolean sidesActive;
        int[] sidePixels = new int[0];
        public final DynamicTexture canvasTexture;
        public final Identifier location;

        private Instance(String key, String canvasId, int version, int width, int height) {
            this.width = width;
            this.height = height;
            this.canvasTexture = new DynamicTexture(() -> "joyofpainting:canvas/" + key, width, height, true);
            this.canvasTexture.upload();
            this.location = Identifier.fromNamespaceAndPath("joyofpainting", "canvas/" + key.replaceAll("[^a-z0-9_./-]", "_"));
            textureManager.register(this.location, this.canvasTexture);
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
                            image.setPixelABGR(x, y, swapRedBlue(pixels[x + y * width]));
                        }
                    }
                    canvasTexture.upload();
                    this.version = version;
                    this.started = true;
                }
            }
        }

        public int pixelWidth() {
            return width;
        }

        public int pixelHeight() {
            return height;
        }

        public void submitEmptyGeometry(PoseStack ms, SubmitNodeCollector collector, int packedLight, boolean glass) {
            final float w32 = 32.0f * (width / 16.0f);
            final float h32 = 32.0f * (height / 16.0f);
            if (glass) {
                collector.submitCustomGeometry(ms, RenderTypes.entityTranslucent(GLASS_FRAME_LOCATION),
                        (p, vc) -> renderGlassFrame(vc, p, w32, h32, packedLight));
            } else {
                collector.submitCustomGeometry(ms, RenderTypes.entitySolid(EMPTY_CANVAS_LOCATION),
                        (p, vc) -> addFrontQuad(vc, p, w32, h32, packedLight));
                collector.submitCustomGeometry(ms, RenderTypes.entitySolid(BACK_LOCATION),
                        (p, vc) -> drawWoodenSidesAndBack(vc, p, w32, h32, packedLight));
            }
        }

        public void submitGeometry(PoseStack ms, SubmitNodeCollector collector, int packedLight, boolean glass, int tint) {
            submitGeometry(ms, collector, packedLight, glass, tint, true);
        }

        public void submitGeometry(PoseStack ms, SubmitNodeCollector collector, int packedLight, boolean glass, int tint, boolean submitBack) {
            final float w32 = 32.0f * (width / 16.0f);
            final float h32 = 32.0f * (height / 16.0f);

            RenderType frontType = glass ? RenderTypes.entityCutout(location) : RenderTypes.entitySolid(location);
            collector.submitCustomGeometry(ms, frontType, (p, vc) -> addFrontQuad(vc, p, w32, h32, packedLight));

            if (glass) {
                if (submitBack) {
                    collector.submitCustomGeometry(ms, RenderTypes.entityCutout(location),
                            (p, vc) -> addGlassBackQuad(vc, p, w32, h32, packedLight));
                }
                if (tint != -1) {
                    collector.submitCustomGeometry(ms, RenderTypes.entityTranslucent(GLASS_FRAME_LOCATION),
                            (p, vc) -> drawGlassTint(vc, p, w32, h32, packedLight, tint));
                }
                if (sidesActive) {
                    collector.submitCustomGeometry(ms, RenderTypes.entityCutout(whiteLocation),
                            (p, vc) -> renderPaintedSides(vc, p, w32, h32, packedLight, true));
                } else {
                    collector.submitCustomGeometry(ms, RenderTypes.entityTranslucent(GLASS_FRAME_LOCATION),
                            (p, vc) -> renderGlassFrame(vc, p, w32, h32, packedLight));
                }
            } else {
                collector.submitCustomGeometry(ms, RenderTypes.entitySolid(BACK_LOCATION),
                        (p, vc) -> drawWoodenSidesAndBack(vc, p, w32, h32, packedLight));
                collector.submitCustomGeometry(ms, RenderTypes.entityCutout(whiteLocation),
                        (p, vc) -> renderPaintedSides(vc, p, w32, h32, packedLight, false));
            }
        }

        private void addFrontQuad(VertexConsumer front, PoseStack.Pose pose, float w32, float h32, int packedLight) {
            addVertex(front, pose, 0, h32, -1, 1, 0, packedLight, 0, 0, -1, 0xFFFFFFFF);
            addVertex(front, pose, w32, h32, -1, 0, 0, packedLight, 0, 0, -1, 0xFFFFFFFF);
            addVertex(front, pose, w32, 0, -1, 0, 1, packedLight, 0, 0, -1, 0xFFFFFFFF);
            addVertex(front, pose, 0, 0, -1, 1, 1, packedLight, 0, 0, -1, 0xFFFFFFFF);
        }

        private void addGlassBackQuad(VertexConsumer back, PoseStack.Pose pose, float w32, float h32, int packedLight) {
            addVertex(back, pose, 0, 0, 1, 1, 1, packedLight, 0, 0, 1, 0xFFFFFFFF);
            addVertex(back, pose, w32, 0, 1, 0, 1, packedLight, 0, 0, 1, 0xFFFFFFFF);
            addVertex(back, pose, w32, h32, 1, 0, 0, packedLight, 0, 0, 1, 0xFFFFFFFF);
            addVertex(back, pose, 0, h32, 1, 1, 0, packedLight, 0, 0, 1, 0xFFFFFFFF);
        }

        private void drawWoodenSidesAndBack(VertexConsumer back, PoseStack.Pose pose, float w32, float h32, int packedLight) {
            final float sideWidth = 1.0f / 16.0f;
            addVertex(back, pose, 0, 0, 1, 0, 0, packedLight, 0, 0, 1, 0xFFFFFFFF);
            addVertex(back, pose, w32, 0, 1, 1, 0, packedLight, 0, 0, 1, 0xFFFFFFFF);
            addVertex(back, pose, w32, h32, 1, 1, 1, packedLight, 0, 0, 1, 0xFFFFFFFF);
            addVertex(back, pose, 0, h32, 1, 0, 1, packedLight, 0, 0, 1, 0xFFFFFFFF);
            if (sidesActive) return;
            addVertex(back, pose, 0, 0, 1, sideWidth, 0, packedLight, -1, 0, 0, 0xFFFFFFFF);
            addVertex(back, pose, 0, h32, 1, sideWidth, 1, packedLight, -1, 0, 0, 0xFFFFFFFF);
            addVertex(back, pose, 0, h32, -1, 0, 1, packedLight, -1, 0, 0, 0xFFFFFFFF);
            addVertex(back, pose, 0, 0, -1, 0, 0, packedLight, -1, 0, 0, 0xFFFFFFFF);
            addVertex(back, pose, 0, h32, 1, 0, 0, packedLight, 0, 1, 0, 0xFFFFFFFF);
            addVertex(back, pose, w32, h32, 1, 1, 0, packedLight, 0, 1, 0, 0xFFFFFFFF);
            addVertex(back, pose, w32, h32, -1, 1, sideWidth, packedLight, 0, 1, 0, 0xFFFFFFFF);
            addVertex(back, pose, 0, h32, -1, 0, sideWidth, packedLight, 0, 1, 0, 0xFFFFFFFF);
            addVertex(back, pose, w32, 0, -1, 0, 0, packedLight, 1, 0, 0, 0xFFFFFFFF);
            addVertex(back, pose, w32, h32, -1, 0, 1, packedLight, 1, 0, 0, 0xFFFFFFFF);
            addVertex(back, pose, w32, h32, 1, sideWidth, 1, packedLight, 1, 0, 0, 0xFFFFFFFF);
            addVertex(back, pose, w32, 0, 1, sideWidth, 0, packedLight, 1, 0, 0, 0xFFFFFFFF);
            addVertex(back, pose, 0, 0, -1, 0, 1, packedLight, 0, -1, 0, 0xFFFFFFFF);
            addVertex(back, pose, w32, 0, -1, 1, 1, packedLight, 0, -1, 0, 0xFFFFFFFF);
            addVertex(back, pose, w32, 0, 1, 1, 1 - sideWidth, packedLight, 0, -1, 0, 0xFFFFFFFF);
            addVertex(back, pose, 0, 0, 1, 0, 1 - sideWidth, packedLight, 0, -1, 0, 0xFFFFFFFF);
        }

        private void renderGlassFrame(VertexConsumer vc, PoseStack.Pose pose, float w32, float h32, int packedLight) {
            double eps = 0.001D;
            float depth = 1.0F / 16.0F;
            addVertex(vc, pose, eps, eps, 1.0F, 0.0F, 0.0F, packedLight, -1.0F, 0.0F, 0.0F, GLASS_FRAME_RGBA);
            addVertex(vc, pose, eps, h32 - eps, 1.0F, 0.0F, 1.0F, packedLight, -1.0F, 0.0F, 0.0F, GLASS_FRAME_RGBA);
            addVertex(vc, pose, eps, h32 - eps, -1.0F, depth, 1.0F, packedLight, -1.0F, 0.0F, 0.0F, GLASS_FRAME_RGBA);
            addVertex(vc, pose, eps, eps, -1.0F, depth, 0.0F, packedLight, -1.0F, 0.0F, 0.0F, GLASS_FRAME_RGBA);
            addVertex(vc, pose, eps, h32 - eps, 1.0F, 0.0F, 0.0F, packedLight, 0.0F, 1.0F, 0.0F, GLASS_FRAME_RGBA);
            addVertex(vc, pose, w32 - eps, h32 - eps, 1.0F, 1.0F, 0.0F, packedLight, 0.0F, 1.0F, 0.0F, GLASS_FRAME_RGBA);
            addVertex(vc, pose, w32 - eps, h32 - eps, -1.0F, 1.0F, depth, packedLight, 0.0F, 1.0F, 0.0F, GLASS_FRAME_RGBA);
            addVertex(vc, pose, eps, h32 - eps, -1.0F, 0.0F, depth, packedLight, 0.0F, 1.0F, 0.0F, GLASS_FRAME_RGBA);
            addVertex(vc, pose, w32 - eps, eps, -1.0F, 0.0F, 0.0F, packedLight, 1.0F, 0.0F, 0.0F, GLASS_FRAME_RGBA);
            addVertex(vc, pose, w32 - eps, h32 - eps, -1.0F, 0.0F, 1.0F, packedLight, 1.0F, 0.0F, 0.0F, GLASS_FRAME_RGBA);
            addVertex(vc, pose, w32 - eps, h32 - eps, 1.0F, depth, 1.0F, packedLight, 1.0F, 0.0F, 0.0F, GLASS_FRAME_RGBA);
            addVertex(vc, pose, w32 - eps, eps, 1.0F, depth, 0.0F, packedLight, 1.0F, 0.0F, 0.0F, GLASS_FRAME_RGBA);
            addVertex(vc, pose, eps, eps, -1.0F, 0.0F, 0.0F, packedLight, 0.0F, -1.0F, 0.0F, GLASS_FRAME_RGBA);
            addVertex(vc, pose, w32 - eps, eps, -1.0F, 1.0F, 0.0F, packedLight, 0.0F, -1.0F, 0.0F, GLASS_FRAME_RGBA);
            addVertex(vc, pose, w32 - eps, eps, 1.0F, 1.0F, depth, packedLight, 0.0F, -1.0F, 0.0F, GLASS_FRAME_RGBA);
            addVertex(vc, pose, eps, eps, 1.0F, 0.0F, depth, packedLight, 0.0F, -1.0F, 0.0F, GLASS_FRAME_RGBA);
        }

        private void drawGlassTint(VertexConsumer vc, PoseStack.Pose pose, float w32, float h32, int packedLight, int tint) {
            int rgba = 0x66000000 | (tint & 0x00FFFFFF);
            addVertex(vc, pose, 0, h32, 0, 1, 0, packedLight, 0, 0, -1, rgba);
            addVertex(vc, pose, w32, h32, 0, 0, 0, packedLight, 0, 0, -1, rgba);
            addVertex(vc, pose, w32, 0, 0, 0, 1, packedLight, 0, 0, -1, rgba);
            addVertex(vc, pose, 0, 0, 0, 1, 1, packedLight, 0, 0, -1, rgba);
        }

        private void renderPaintedSides(VertexConsumer vc, PoseStack.Pose pose, float w32, float h32, int packedLight, boolean skipTransparent) {
            if (!sidesActive || sidePixels == null || sidePixels.length == 0) return;
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
                    addSideVertex(vc, pose, x0, h32 - eps, 1.0F, c, packedLight, 0.0F, 1.0F, 0.0F);
                    addSideVertex(vc, pose, x1, h32 - eps, 1.0F, c, packedLight, 0.0F, 1.0F, 0.0F);
                    addSideVertex(vc, pose, x1, h32 - eps, -1.0F, c, packedLight, 0.0F, 1.0F, 0.0F);
                    addSideVertex(vc, pose, x0, h32 - eps, -1.0F, c, packedLight, 0.0F, 1.0F, 0.0F);
                }
            }
            for (int k = 0; k < width; k++) {
                int c = sideColor(bottomOffset + k);
                if (!skipTransparent || ((c >>> 24) & 0xFF) != 0) {
                    float x0 = w32 - (k + 1) * unit;
                    float x1 = w32 - k * unit;
                    addSideVertex(vc, pose, x0, eps, -1.0F, c, packedLight, 0.0F, -1.0F, 0.0F);
                    addSideVertex(vc, pose, x1, eps, -1.0F, c, packedLight, 0.0F, -1.0F, 0.0F);
                    addSideVertex(vc, pose, x1, eps, 1.0F, c, packedLight, 0.0F, -1.0F, 0.0F);
                    addSideVertex(vc, pose, x0, eps, 1.0F, c, packedLight, 0.0F, -1.0F, 0.0F);
                }
            }
            for (int i = 0; i < height; i++) {
                int c = sideColor(leftOffset + i);
                if (!skipTransparent || ((c >>> 24) & 0xFF) != 0) {
                    float y0 = h32 - (i + 1) * unit;
                    float y1 = h32 - i * unit;
                    addSideVertex(vc, pose, w32 - eps, y0, -1.0F, c, packedLight, 1.0F, 0.0F, 0.0F);
                    addSideVertex(vc, pose, w32 - eps, y1, -1.0F, c, packedLight, 1.0F, 0.0F, 0.0F);
                    addSideVertex(vc, pose, w32 - eps, y1, 1.0F, c, packedLight, 1.0F, 0.0F, 0.0F);
                    addSideVertex(vc, pose, w32 - eps, y0, 1.0F, c, packedLight, 1.0F, 0.0F, 0.0F);
                }
            }
            for (int i = 0; i < height; i++) {
                int c = sideColor(rightOffset + i);
                if (!skipTransparent || ((c >>> 24) & 0xFF) != 0) {
                    float y0 = h32 - (i + 1) * unit;
                    float y1 = h32 - i * unit;
                    addSideVertex(vc, pose, eps, y0, 1.0F, c, packedLight, -1.0F, 0.0F, 0.0F);
                    addSideVertex(vc, pose, eps, y1, 1.0F, c, packedLight, -1.0F, 0.0F, 0.0F);
                    addSideVertex(vc, pose, eps, y1, -1.0F, c, packedLight, -1.0F, 0.0F, 0.0F);
                    addSideVertex(vc, pose, eps, y0, -1.0F, c, packedLight, -1.0F, 0.0F, 0.0F);
                }
            }
        }

        private int sideColor(int index) {
            if (sidePixels == null || index < 0 || index >= sidePixels.length) return CanvasSides.DEFAULT_COLOR;
            return sidePixels[index];
        }

        private void addSideVertex(VertexConsumer vb, PoseStack.Pose pose,
                                   double x, double y, double z, int color, int lightmap,
                                   float nx, float ny, float nz) {
            addVertex(vb, pose, x, y, z, 0.0F, 0.0F, lightmap, nx, ny, nz, color);
        }

        private void addVertex(VertexConsumer vb, PoseStack.Pose pose,
                               double x, double y, double z, float tx, float ty, int lightmap,
                               float nx, float ny, float nz, int rgba) {
            int a = (rgba >>> 24) & 0xFF;
            int r = (rgba >>> 16) & 0xFF;
            int g = (rgba >>> 8) & 0xFF;
            int b = rgba & 0xFF;
            vb.addVertex(pose, (float) x, (float) y, (float) z)
                    .setColor(r, g, b, a)
                    .setUv(tx, ty)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(lightmap)
                    .setNormal(pose, nx, ny, nz);
        }

        @Override
        public void close() {
            this.canvasTexture.close();
            textureManager.release(location);
        }
    }
}
