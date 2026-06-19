package com.leclowndu93150.joyofpainting.item;

import com.leclowndu93150.joyofpainting.CanvasType;
import com.leclowndu93150.joyofpainting.client.JoyOfPaintingClient;
import com.leclowndu93150.joyofpainting.entity.EntityCanvas;
import com.leclowndu93150.joyofpainting.registry.ModDataComponents;
import com.leclowndu93150.joyofpainting.registry.ModEntities;
import com.leclowndu93150.joyofpainting.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class ItemCanvas extends Item {
    public static final int SIGNED_STACK_SIZE = 16;
    private final CanvasType canvasType;
    private final boolean glass;

    public ItemCanvas(CanvasType canvasType, boolean glass, Properties properties) {
        super(properties);
        this.canvasType = canvasType;
        this.glass = glass;
    }

    public boolean isGlass() {
        return this.glass;
    }

    public CanvasType getCanvasType() {
        return this.canvasType;
    }

    public int getWidth() {
        return CanvasType.getWidth(canvasType);
    }

    public int getHeight() {
        return CanvasType.getHeight(canvasType);
    }

    public static ItemCanvas canvasItemFor(CanvasType type, boolean glass) {
        return switch (type) {
            case SMALL -> glass ? ModItems.ITEM_CANVAS_GLASS.get() : ModItems.ITEM_CANVAS.get();
            case LONG -> glass ? ModItems.ITEM_CANVAS_GLASS_LONG.get() : ModItems.ITEM_CANVAS_LONG.get();
            case TALL -> glass ? ModItems.ITEM_CANVAS_GLASS_TALL.get() : ModItems.ITEM_CANVAS_TALL.get();
            case LARGE -> glass ? ModItems.ITEM_CANVAS_GLASS_LARGE.get() : ModItems.ITEM_CANVAS_LARGE.get();
        };
    }

    public static String generateName(Player player) {
        return player.getUUID() + "_" + System.currentTimeMillis() / 100;
    }

    public static boolean hasPixels(ItemStack stack) {
        return stack.has(ModDataComponents.CANVAS_PIXELS.get());
    }

    public static int getGeneration(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.CANVAS_GENERATION.get(), 0);
    }

    public static boolean hasTitle(ItemStack stack) {
        String s = stack.get(ModDataComponents.CANVAS_TITLE.get());
        return !StringUtil.isNullOrEmpty(s);
    }

    @Nullable
    public static Component getCustomTitle(ItemStack stack) {
        String s = stack.get(ModDataComponents.CANVAS_TITLE.get());
        return StringUtil.isNullOrEmpty(s) ? null : Component.literal(s);
    }

    public static Component getFullLabel(ItemStack stack) {
        StringBuilder label = new StringBuilder();
        Component title = getCustomTitle(stack);
        if (title != null) label.append(title.getString()).append(" ");
        String author = stack.get(ModDataComponents.CANVAS_AUTHOR.get());
        if (!StringUtil.isNullOrEmpty(author)) {
            label.append(Component.translatable("canvas.byAuthor", author).getString()).append(" ");
        }
        int generation = getGeneration(stack);
        MutableComponent result = Component.literal(label.toString());
        if (generation == 1) result.withStyle(ChatFormatting.YELLOW);
        else if (generation >= 3) result.withStyle(ChatFormatting.GRAY);
        return result;
    }

    public static void updateStackSize(ItemStack stack) {
        if (getGeneration(stack) > 0) {
            stack.set(DataComponents.MAX_STACK_SIZE, SIGNED_STACK_SIZE);
        } else {
            stack.remove(DataComponents.MAX_STACK_SIZE);
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            JoyOfPaintingClient.showCanvasGui(player);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos clickedPos = context.getClickedPos();
        Direction direction = context.getClickedFace();
        BlockPos pos = clickedPos.relative(direction);
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (player == null) return InteractionResult.FAIL;

        Level level = context.getLevel();
        if (!hasPixels(stack)) {
            if (level.isClientSide()) JoyOfPaintingClient.showCanvasGui(player);
            return InteractionResult.SUCCESS;
        }

        if (!this.mayPlace(player, direction, stack, pos)) {
            return InteractionResult.FAIL;
        }

        int rotation = getRotation(direction, clickedPos, player);
        EntityCanvas entityCanvas = new EntityCanvas(level, stack, pos, direction, canvasType, rotation);
        if (!entityCanvas.survives()) {
            return InteractionResult.CONSUME;
        }

        if (!level.isClientSide()) {
            entityCanvas.playPlacementSound();
            level.gameEvent(player, GameEvent.ENTITY_PLACE, entityCanvas.position());
            level.addFreshEntity(entityCanvas);
        }
        stack.shrink(1);
        return InteractionResult.SUCCESS;
    }

    private boolean mayPlace(Player player, Direction direction, ItemStack stack, BlockPos pos) {
        if (canvasType == CanvasType.SMALL) {
            return Level.isInSpawnableBounds(pos) && player.mayUseItemAt(pos, direction, stack);
        }
        return !direction.getAxis().isVertical() && player.mayUseItemAt(pos, direction, stack);
    }

    private static int getRotation(Direction direction, BlockPos blockpos, Player player) {
        int rotation = 0;
        if (direction.getAxis() == Direction.Axis.Y) {
            double xDiff = blockpos.getX() - player.getX();
            double zDiff = blockpos.getZ() - player.getZ();
            if (Math.abs(xDiff) > Math.abs(zDiff)) {
                rotation = xDiff > 0 ? 1 : 3;
            } else if (zDiff > 0) {
                rotation = 2;
            }
            if (direction == Direction.DOWN && Math.abs(xDiff) < Math.abs(zDiff)) {
                rotation += 2;
            }
        }
        return rotation;
    }

    @Override
    public Component getName(ItemStack stack) {
        Component title = getCustomTitle(stack);
        return title != null ? title : super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        if (hasPixels(stack)) {
            String author = stack.get(ModDataComponents.CANVAS_AUTHOR.get());
            if (!StringUtil.isNullOrEmpty(author)) {
                tooltip.accept(Component.translatable("canvas.byAuthor", author));
            }
            int generation = getGeneration(stack);
            if (generation > 0) {
                tooltip.accept(Component.translatable("canvas.generation." + (generation - 1))
                        .withStyle(generation == 1 ? ChatFormatting.GOLD : ChatFormatting.GRAY));
            }
        } else {
            tooltip.accept(Component.translatable("canvas.empty").withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return getGeneration(stack) > 0;
    }

}
