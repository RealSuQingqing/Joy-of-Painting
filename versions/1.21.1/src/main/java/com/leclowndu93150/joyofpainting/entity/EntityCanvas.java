package com.leclowndu93150.joyofpainting.entity;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.leclowndu93150.joyofpainting.CanvasType;
import com.leclowndu93150.joyofpainting.item.ItemCanvas;
import com.leclowndu93150.joyofpainting.network.JoyOfPaintingNetwork;
import com.leclowndu93150.joyofpainting.network.packets.PictureRequestPacket;
import com.leclowndu93150.joyofpainting.registry.ModDataComponents;
import com.leclowndu93150.joyofpainting.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EntityCanvas extends HangingEntity {
    private static final EntityDataAccessor<String> CANVAS_ID = SynchedEntityData.defineId(EntityCanvas.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> CANVAS_VERSION = SynchedEntityData.defineId(EntityCanvas.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Byte> CANVAS_TYPE_KEY = SynchedEntityData.defineId(EntityCanvas.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> CANVAS_ROTATION = SynchedEntityData.defineId(EntityCanvas.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> CANVAS_GLASS = SynchedEntityData.defineId(EntityCanvas.class, EntityDataSerializers.BOOLEAN);

    public static final Map<String, Picture> PICTURES = Maps.newHashMap();
    public static final Set<String> PICTURE_REQUESTS = Sets.newHashSet();

    public record Picture(int version, int[] pixels, boolean sidesActive, int[] sidePixels) {
        public Picture(int version, int[] pixels) {
            this(version, pixels, false, new int[0]);
        }
    }

    private String canvasTitle;
    private String canvasAuthor;
    private int canvasGeneration = 0;
    private boolean canvasSigned;
    private int tickCounter1 = 0;

    public EntityCanvas(EntityType<? extends HangingEntity> type, Level level) {
        super(type, level);
        clientPictureInit(level);
    }

    public EntityCanvas(Level level, ItemStack stack, BlockPos pos, Direction facing, CanvasType canvasType, int rotation) {
        super(ModEntities.CANVAS.get(), level, pos);
        String id = stack.getOrDefault(ModDataComponents.CANVAS_ID.get(), "");
        int version = stack.getOrDefault(ModDataComponents.CANVAS_VERSION.get(), 0);
        String title = stack.get(ModDataComponents.CANVAS_TITLE.get());
        String author = stack.get(ModDataComponents.CANVAS_AUTHOR.get());
        this.setCanvasID(id);
        this.setVersion(version);
        if (title != null && author != null) {
            this.canvasSigned = true;
            this.canvasTitle = title;
            this.canvasAuthor = author;
            this.canvasGeneration = stack.getOrDefault(ModDataComponents.CANVAS_GENERATION.get(), 0);
        } else {
            this.canvasSigned = false;
        }
        this.setCanvasType(canvasType);
        this.setGlass(stack.getItem() instanceof ItemCanvas ic && ic.isGlass());
        this.setRotation(rotation);
        this.setDirection(facing);

        Picture picture = PICTURES.get(id);
        if (picture == null || picture.version < version) {
            List<Integer> pixelList = stack.get(ModDataComponents.CANVAS_PIXELS.get());
            int[] pixels = pixelList == null ? new int[0] : pixelList.stream().mapToInt(Integer::intValue).toArray();
            boolean sidesActive = stack.getOrDefault(ModDataComponents.CANVAS_SIDES_ACTIVE.get(), false);
            List<Integer> sideList = stack.get(ModDataComponents.CANVAS_SIDE_PIXELS.get());
            int[] sidePixels = sideList == null ? new int[0] : sideList.stream().mapToInt(Integer::intValue).toArray();
            PICTURES.put(id, new Picture(version, pixels, sidesActive, sidePixels));
        }
    }

    private void clientPictureInit(Level level) {
        if (!level.isClientSide) return;
        String canvasID = getCanvasID();
        int version = getVersion();
        if (!canvasID.isEmpty() && version > 0) {
            Picture picture = PICTURES.get(canvasID);
            if ((picture == null || picture.version < version) && !PICTURE_REQUESTS.contains(canvasID)) {
                PICTURE_REQUESTS.add(canvasID);
                PacketDistributor.sendToServer(new PictureRequestPacket(canvasID));
            }
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(CANVAS_ID, "");
        builder.define(CANVAS_VERSION, 0);
        builder.define(CANVAS_TYPE_KEY, (byte) 0);
        builder.define(CANVAS_ROTATION, (byte) 0);
        builder.define(CANVAS_GLASS, false);
    }

    @Override
    public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> key) {
        if (CANVAS_TYPE_KEY.equals(key)) {
            this.recalculateBoundingBox();
        } else if (CANVAS_ID.equals(key) || CANVAS_VERSION.equals(key)) {
            clientPictureInit(this.level());
        }
        super.onSyncedDataUpdated(key);
    }

    public int getWidth() {
        return CanvasType.getWidth(getCanvasType());
    }

    public int getHeight() {
        return CanvasType.getHeight(getCanvasType());
    }

    @Override
    public void dropItem(@Nullable Entity brokenEntity) {
        if (!this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) return;
        this.playSound(isGlass() ? SoundEvents.GLASS_BREAK : SoundEvents.PAINTING_BREAK, 1.0F, 1.0F);
        if (brokenEntity instanceof Player player && player.getAbilities().instabuild) return;

        CanvasType canvasType = getCanvasType();
        ItemStack canvasItem = new ItemStack(ItemCanvas.canvasItemFor(canvasType, isGlass()));
        canvasItem.set(ModDataComponents.CANVAS_ID.get(), getCanvasID());
        canvasItem.set(ModDataComponents.CANVAS_VERSION.get(), getVersion());
        if (canvasSigned) {
            canvasItem.set(ModDataComponents.CANVAS_AUTHOR.get(), canvasAuthor);
            canvasItem.set(ModDataComponents.CANVAS_TITLE.get(), canvasTitle);
            canvasItem.set(ModDataComponents.CANVAS_GENERATION.get(), canvasGeneration);
        }
        ItemCanvas.updateStackSize(canvasItem);
        Picture picture = PICTURES.get(getCanvasID());
        if (picture != null && picture.pixels != null) {
            canvasItem.set(ModDataComponents.CANVAS_PIXELS.get(),
                    Arrays.stream(picture.pixels).boxed().toList());
            if (picture.sidePixels() != null && picture.sidePixels().length > 0) {
                canvasItem.set(ModDataComponents.CANVAS_SIDES_ACTIVE.get(), picture.sidesActive());
                canvasItem.set(ModDataComponents.CANVAS_SIDE_PIXELS.get(),
                        Arrays.stream(picture.sidePixels()).boxed().toList());
            }
        }
        this.spawnAtLocation(canvasItem);
    }

    @Override
    public void tick() {
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();
        if (this.tickCounter1++ == 50 && !this.level().isClientSide) {
            this.tickCounter1 = 0;
            if (this.isAlive() && !this.survives()) {
                this.discard();
                this.dropItem(null);
            }
        }
    }

    @Override
    public void playPlacementSound() {
        this.playSound(isGlass() ? SoundEvents.GLASS_PLACE : SoundEvents.PAINTING_PLACE, 1.0F, 1.0F);
    }

    @Override
    protected void setDirection(@NotNull Direction facing) {
        this.direction = facing;
        if (facing.getAxis().isHorizontal()) {
            this.setXRot(0.0F);
            this.setYRot(this.direction.get2DDataValue() * 90);
        } else {
            this.setXRot(-90 * facing.getAxisDirection().getStep());
            this.setYRot(0.0F);
        }
        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();
        this.recalculateBoundingBox();
    }

    private double offs(int l) {
        return l % 32 == 0 ? 0.5D : 0.0D;
    }

    @Override
    public void moveTo(double x, double y, double z, float yRot, float xRot) {
        this.setPos(x, y, z);
    }

    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps) {
        this.setPos(x, y, z);
    }

    @Override
    protected @NotNull AABB calculateBoundingBox(@NotNull BlockPos pos, @NotNull Direction direction) {
        double cx = pos.getX() + 0.5D - direction.getStepX() * 0.46875D;
        double cy = pos.getY() + 0.5D - direction.getStepY() * 0.46875D;
        double cz = pos.getZ() + 0.5D - direction.getStepZ() * 0.46875D;
        if (direction.getAxis().isHorizontal()) {
            double dw = this.offs(this.getWidth());
            double dh = this.offs(this.getHeight());
            cy += dh;
            Direction ccw = direction.getCounterClockWise();
            cx += dw * ccw.getStepX();
            cz += dw * ccw.getStepZ();
        }
        double w = this.getWidth();
        double h = this.getHeight();
        double d = this.getWidth();
        switch (direction.getAxis()) {
            case X -> w = 1.0D;
            case Y -> h = 1.0D;
            case Z -> d = 1.0D;
        }
        w /= 32.0D;
        h /= 32.0D;
        d /= 32.0D;
        return new AABB(cx - w, cy - h, cz - d, cx + w, cy + h, cz + d);
    }

    @Override
    public boolean survives() {
        if (direction.getAxis().isHorizontal()) {
            return super.survives();
        }
        Level level = this.level();
        if (!level.noCollision(this)) return false;
        BlockPos supportPos = this.pos.relative(this.direction.getOpposite());
        BlockState state = level.getBlockState(supportPos);
        return (state.isFaceSturdy(level, supportPos, this.direction)
                || (this.direction.getAxis().isHorizontal() && DiodeBlock.isDiode(state)))
                && level.getEntities(this, this.getBoundingBox(), HANGING_ENTITY).isEmpty();
    }

    @Override
    public Vec3 getLightProbePosition(float partialTick) {
        return Vec3.atCenterOf(this.pos);
    }

    public int getRotation() {
        return this.entityData.get(CANVAS_ROTATION);
    }

    private void setRotation(int rotation) {
        this.entityData.set(CANVAS_ROTATION, (byte) (rotation % 4));
    }

    public String getCanvasID() {
        return this.entityData.get(CANVAS_ID);
    }

    private void setCanvasID(String canvasID) {
        this.entityData.set(CANVAS_ID, canvasID);
    }

    public int getVersion() {
        return this.entityData.get(CANVAS_VERSION);
    }

    private void setVersion(int version) {
        this.entityData.set(CANVAS_VERSION, version);
    }

    public CanvasType getCanvasType() {
        CanvasType t = CanvasType.fromByte(this.entityData.get(CANVAS_TYPE_KEY));
        return t == null ? CanvasType.SMALL : t;
    }

    public byte getCanvasTypeKey() {
        return this.entityData.get(CANVAS_TYPE_KEY);
    }

    private void setCanvasType(CanvasType canvasType) {
        this.entityData.set(CANVAS_TYPE_KEY, (byte) canvasType.ordinal());
    }

    public boolean isGlass() {
        return this.entityData.get(CANVAS_GLASS);
    }

    private void setGlass(boolean glass) {
        this.entityData.set(CANVAS_GLASS, glass);
    }

    public boolean hasSidesActive() {
        Picture pic = PICTURES.get(getCanvasID());
        return pic != null && pic.sidesActive() && pic.sidePixels() != null && pic.sidePixels().length > 0;
    }

    public int[] getSidePixelsArray() {
        Picture pic = PICTURES.get(getCanvasID());
        return pic == null ? null : pic.sidePixels();
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket(@NotNull ServerEntity entity) {
        return new ClientboundAddEntityPacket(this, this.direction.get3DDataValue(), this.getPos());
    }

    @Override
    public void recreateFromPacket(@NotNull ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        this.setDirection(Direction.from3DDataValue(packet.getData()));
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        this.pos = new BlockPos(tag.getInt("TileX"), tag.getInt("TileY"), tag.getInt("TileZ"));
        CompoundTag canvasNBT = tag.contains("canvas") ? tag.getCompound("canvas") : tag;

        this.canvasSigned = canvasNBT.contains("author") && canvasNBT.contains("title");
        String canvasId = canvasNBT.getString("name");
        this.setCanvasID(canvasId);
        int version = canvasNBT.getInt("v");
        this.setVersion(version);
        if (canvasSigned) {
            this.canvasAuthor = canvasNBT.getString("author");
            this.canvasTitle = canvasNBT.getString("title");
            this.canvasGeneration = canvasNBT.getInt("generation");
        }

        Picture picture = PICTURES.get(canvasId);
        if (picture == null || picture.version < version) {
            boolean sidesActive = canvasNBT.getBoolean("sidesActive");
            int[] sidePixels = canvasNBT.getIntArray("sidePixels");
            PICTURES.put(canvasId, new Picture(version, canvasNBT.getIntArray("pixels"), sidesActive, sidePixels));
        }

        this.setGlass(tag.getBoolean("glass"));
        CanvasType canvasType = CanvasType.fromByte(tag.getByte("ctype"));
        if (canvasType == null) {
            this.discard();
            return;
        }
        this.setCanvasType(canvasType);
        if (tag.contains("Facing") && !tag.contains("RealFace")) {
            int facing = tag.getByte("Facing");
            this.setDirection(Direction.from2DDataValue(facing));
        } else {
            this.setDirection(Direction.from3DDataValue(tag.getByte("RealFace")));
        }
        this.setRotation(tag.getByte("Rotation"));
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        BlockPos bp = this.getPos();
        tag.putInt("TileX", bp.getX());
        tag.putInt("TileY", bp.getY());
        tag.putInt("TileZ", bp.getZ());
        tag.putString("name", getCanvasID());
        tag.putInt("v", getVersion());
        if (canvasSigned && canvasAuthor != null && canvasTitle != null) {
            tag.putString("author", canvasAuthor);
            tag.putString("title", canvasTitle);
            tag.putInt("generation", canvasGeneration);
        }
        tag.putByte("ctype", getCanvasTypeKey());
        tag.putBoolean("glass", isGlass());
        tag.putByte("RealFace", (byte) this.direction.get3DDataValue());
        tag.putByte("Rotation", (byte) this.getRotation());

        Picture picture = PICTURES.get(getCanvasID());
        if (picture != null && picture.pixels != null) {
            tag.putIntArray("pixels", picture.pixels);
            if (picture.sidePixels() != null && picture.sidePixels().length > 0) {
                tag.putBoolean("sidesActive", picture.sidesActive());
                tag.putIntArray("sidePixels", picture.sidePixels());
            }
        }
    }

    @Override
    public @NotNull InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand) {
        CanvasType canvasType = getCanvasType();
        if (canvasType == CanvasType.SMALL || canvasType == CanvasType.LARGE) {
            if (!this.level().isClientSide) setRotation(getRotation() + 1);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
