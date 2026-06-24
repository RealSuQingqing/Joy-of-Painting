package com.leclowndu93150.joyofpainting.entity;

import com.leclowndu93150.joyofpainting.item.ItemCanvas;
import com.leclowndu93150.joyofpainting.item.ItemPalette;
import com.leclowndu93150.joyofpainting.network.packets.CloseGuiPacket;
import com.leclowndu93150.joyofpainting.network.packets.OpenGuiPacket;
import com.leclowndu93150.joyofpainting.registry.ModDataComponents;
import com.leclowndu93150.joyofpainting.registry.ModItems;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class EntityEasel extends Entity {
    private static final EntityDataAccessor<ItemStack> DATA_CANVAS = SynchedEntityData.defineId(EntityEasel.class, EntityDataSerializers.ITEM_STACK);

    private Player painter = null;
    private Runnable dropDeferred = null;
    private int dropWaitTicks = 0;

    public EntityEasel(EntityType<EntityEasel> type, Level level) {
        super(type, level);
    }

    public void setPainter(Player painter) {
        this.painter = painter;
    }

    public Player getPainter() {
        return painter;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean hurtServer(@NotNull ServerLevel level, @NotNull DamageSource damageSource, float amount) {
        if (this.isInvulnerableToBase(damageSource)) return false;
        if (this.isRemoved()) return false;
        if (!getItem().isEmpty() && !damageSource.is(DamageTypeTags.IS_EXPLOSION)) {
            this.dropItem(damageSource.getEntity(), false);
        } else {
            this.dropItem(damageSource.getEntity());
            this.kill(level);
            this.markHurt();
        }
        return false;
    }

    private void showBreakingParticles(ServerLevel level) {
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BIRCH_PLANKS.defaultBlockState()),
                this.getX(), this.getY(0.6666666666666666D), this.getZ(),
                10, this.getBbWidth() / 4.0F, this.getBbHeight() / 4.0F, this.getBbWidth() / 4.0F, 0.05D);
    }

    @Override
    public void kill(@NotNull ServerLevel level) {
        showBreakingParticles(level);
        super.kill(level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_CANVAS, ItemStack.EMPTY);
    }

    public void dropItem(@Nullable Entity entity) {
        this.dropItem(entity, true);
    }

    private void dropItem(@Nullable Entity entity, boolean dropSelf) {
        if (painter != null) {
            if (dropDeferred == null) {
                PacketDistributor.sendToPlayer((ServerPlayer) painter, new CloseGuiPacket());
                dropDeferred = () -> doDrop(entity, dropSelf);
            }
        } else {
            doDrop(entity, dropSelf);
        }
    }

    public void doDrop(@Nullable Entity entity, boolean dropSelf) {
        ItemStack canvasStack = this.getItem();
        this.setItem(ItemStack.EMPTY);
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        if (!canvasStack.isEmpty()) this.spawnAtLocation(serverLevel, canvasStack.copy());
        if (entity instanceof Player p && p.getAbilities().instabuild) return;
        if (dropSelf && serverLevel.getGameRules().get(GameRules.ENTITY_DROPS)) {
            this.spawnAtLocation(serverLevel, this.getEaselItemStack());
        }
    }

    public ItemStack getItem() {
        return this.entityData.get(DATA_CANVAS);
    }

    public void setItem(ItemStack stack) {
        this.setItem(stack, true);
    }

    public void setItem(ItemStack stack, boolean makeSound) {
        if (!stack.isEmpty()) {
            stack = stack.copyWithCount(1);
        }
        this.entityData.set(DATA_CANVAS, stack);
        if (makeSound) {
            this.playSound(stack.isEmpty() ? SoundEvents.PAINTING_BREAK : SoundEvents.PAINTING_PLACE, 1.0F, 1.0F);
        }
    }

    @Override
    public @NotNull SlotAccess getSlot(int i) {
        return i == 0 ? new SlotAccess() {
            @Override
            public @NotNull ItemStack get() {
                return EntityEasel.this.getItem();
            }

            @Override
            public boolean set(@NotNull ItemStack stack) {
                EntityEasel.this.setItem(stack);
                return true;
            }
        } : super.getSlot(i);
    }

    @Override
    protected void addAdditionalSaveData(@NotNull ValueOutput output) {
        if (!this.getItem().isEmpty()) {
            output.store("Item", ItemStack.CODEC, this.getItem());
        }
    }

    @Override
    protected void readAdditionalSaveData(@NotNull ValueInput input) {
        ItemStack stack = input.read("Item", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        if (!stack.isEmpty()) {
            this.setItem(stack, false);
        }
    }

    @Override
    public @NotNull InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand, @NotNull Vec3 location) {
        ItemStack inHand = player.getItemInHand(hand);
        boolean filled = !this.getItem().isEmpty();
        boolean handCanvas = inHand.getItem() instanceof ItemCanvas;
        boolean handPalette = inHand.getItem() instanceof ItemPalette;
        if (this.level().isClientSide()) {
            return !filled && !handCanvas ? InteractionResult.PASS : InteractionResult.SUCCESS;
        }
        if (!filled) {
            if (handCanvas && !this.isRemoved()) {
                this.setItem(inHand);
                inHand.shrink(1);
            }
        } else {
            boolean unused = this.painter == null;
            boolean toEdit = handPalette && getItem().getOrDefault(ModDataComponents.CANVAS_GENERATION.get(), 0) <= 0;
            boolean allowed = unused || !toEdit;
            OpenGuiPacket pack = new OpenGuiPacket(this.getId(), allowed, toEdit, hand);
            PacketDistributor.sendToPlayer((ServerPlayer) player, pack);
            if (toEdit && allowed) this.painter = player;
        }
        return InteractionResult.CONSUME;
    }

    protected ItemStack getEaselItemStack() {
        return new ItemStack(ModItems.ITEM_EASEL.get());
    }

    @Override
    public ItemStack getPickResult() {
        ItemStack canvas = this.getItem();
        return canvas.isEmpty() ? this.getEaselItemStack() : canvas.copy();
    }

    @Override
    public void tick() {
        super.tick();
        move(MoverType.SELF, new Vec3(0, -0.25, 0));
        reapplyPosition();
        if (!level().isClientSide() && dropDeferred != null) {
            dropWaitTicks++;
            if (painter == null || dropWaitTicks > 80) {
                dropDeferred.run();
                dropDeferred = null;
                dropWaitTicks = 0;
            }
        }
        if (painter != null && (painter.isRemoved() || !painter.isAlive() || painter.distanceToSqr(this) > 64)) {
            painter = null;
        }
    }

    @Override
    public boolean isPickable() {
        return true;
    }
}
