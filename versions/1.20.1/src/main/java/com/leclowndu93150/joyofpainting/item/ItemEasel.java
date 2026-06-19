package com.leclowndu93150.joyofpainting.item;

import com.leclowndu93150.joyofpainting.entity.EntityEasel;
import com.leclowndu93150.joyofpainting.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ItemEasel extends Item {
    public ItemEasel(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Direction direction = ctx.getClickedFace();
        if (direction == Direction.DOWN) {
            return InteractionResult.FAIL;
        }
        Level level = ctx.getLevel();
        BlockPlaceContext placeContext = new BlockPlaceContext(ctx);
        BlockPos blockpos = placeContext.getClickedPos();
        ItemStack stack = ctx.getItemInHand();
        Vec3 vec3 = Vec3.atBottomCenterOf(blockpos);
        AABB aabb = ModEntities.EASEL.get().getDimensions().makeBoundingBox(vec3.x(), vec3.y(), vec3.z());
        if (!level.noCollision(null, aabb) || !level.getEntities(null, aabb).isEmpty()) {
            return InteractionResult.FAIL;
        }
        if (level instanceof ServerLevel serverLevel) {
            EntityEasel easel = ModEntities.EASEL.get().create(serverLevel, stack.getTag(), null, blockpos, MobSpawnType.SPAWN_EGG, true, true);
            if (easel == null) {
                return InteractionResult.FAIL;
            }
            float yaw = Mth.floor((Mth.wrapDegrees(ctx.getRotation() - 180.0F) + 22.5F) / 45.0F) * 45.0F;
            easel.moveTo(easel.getX(), easel.getY(), easel.getZ(), yaw, 0.0F);
            serverLevel.addFreshEntityWithPassengers(easel);
            level.playSound(null, easel.getX(), easel.getY(), easel.getZ(), SoundEvents.ARMOR_STAND_PLACE, SoundSource.BLOCKS, 0.75F, 0.8F);
            level.gameEvent(ctx.getPlayer(), GameEvent.ENTITY_PLACE, easel.getPosition(0));
        }
        stack.shrink(1);
        return InteractionResult.SUCCESS;
    }
}
