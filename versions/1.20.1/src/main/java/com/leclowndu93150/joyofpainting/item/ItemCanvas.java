package com.leclowndu93150.joyofpainting.item;

import com.leclowndu93150.joyofpainting.CanvasType;
import com.leclowndu93150.joyofpainting.client.JoyOfPaintingClient;
import com.leclowndu93150.joyofpainting.entity.EntityCanvas;
import com.leclowndu93150.joyofpainting.client.render.CanvasItemRenderer;
import com.leclowndu93150.joyofpainting.nbt.NbtCanvas;
import com.leclowndu93150.joyofpainting.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class ItemCanvas extends Item {
    public static final int SIGNED_STACK_SIZE = 16;
    private final CanvasType canvasType;
    private final boolean glass;

    public ItemCanvas(CanvasType canvasType, Properties properties) {
        this(canvasType, false, properties);
    }

    public ItemCanvas(CanvasType canvasType, boolean glass, Properties properties) {
        super(properties);
        this.canvasType = canvasType;
        this.glass = glass;
    }

    public boolean isGlass() {
        return this.glass;
    }

    public static ItemCanvas canvasItemFor(CanvasType type, boolean glass) {
        return switch (type) {
            case SMALL -> glass ? ModItems.ITEM_CANVAS_GLASS.get() : ModItems.ITEM_CANVAS.get();
            case LONG -> glass ? ModItems.ITEM_CANVAS_GLASS_LONG.get() : ModItems.ITEM_CANVAS_LONG.get();
            case TALL -> glass ? ModItems.ITEM_CANVAS_GLASS_TALL.get() : ModItems.ITEM_CANVAS_TALL.get();
            case LARGE -> glass ? ModItems.ITEM_CANVAS_GLASS_LARGE.get() : ModItems.ITEM_CANVAS_LARGE.get();
        };
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) {
            JoyOfPaintingClient.showCanvasGui(player);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos clickedPos = context.getClickedPos();
        Direction direction = context.getClickedFace();
        BlockPos pos = clickedPos.relative(direction);
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (player == null) {
            return InteractionResult.FAIL;
        }
        Level level = context.getLevel();
        if (!NbtCanvas.hasPixels(stack)) {
            if (level.isClientSide) {
                JoyOfPaintingClient.showCanvasGui(player);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!this.mayPlace(player, direction, stack, pos)) {
            return InteractionResult.FAIL;
        }

        int rotation = getRotation(direction, clickedPos, player);
        EntityCanvas entityCanvas = new EntityCanvas(level, stack, pos, direction, canvasType, rotation);
        if (!entityCanvas.survives()) {
            return InteractionResult.CONSUME;
        }

        if (!level.isClientSide) {
            entityCanvas.playPlacementSound();
            level.gameEvent(player, GameEvent.ENTITY_PLACE, entityCanvas.position());
            level.addFreshEntity(entityCanvas);
        }
        stack.shrink(1);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static int getRotation(Direction direction, BlockPos blockpos, Player player) {
        int rotation = 0;
        if (direction.getAxis() == Direction.Axis.Y) {
            double xDiff = blockpos.getX() - player.getX();
            double zDiff = blockpos.getZ() - player.getZ();
            if (Math.abs(xDiff) > Math.abs(zDiff)) {
                if (xDiff > 0) {
                    rotation = 1;
                } else {
                    rotation = 3;
                }
            } else {
                if (zDiff > 0) {
                    rotation = 2;
                }
            }
            if (direction == Direction.DOWN && Math.abs(xDiff) < Math.abs(zDiff)) {
                rotation += 2;
            }
        }
        return rotation;
    }

    public static boolean hasTitle(ItemStack stack) {
        return NbtCanvas.hasTitle(stack);
    }

    public static Component getFullLabel(ItemStack stack) {
        StringBuilder labelString = new StringBuilder();
        Component title = getCustomTitle(stack);
        if (title != null) {
            labelString.append(title.getString()).append(" ");
        }
        String author = NbtCanvas.getAuthor(stack);
        if (!StringUtil.isNullOrEmpty(author)) {
            labelString.append(Component.translatable("canvas.byAuthor", author).getString()).append(" ");
        }
        int generation = NbtCanvas.getGeneration(stack);
        MutableComponent label = Component.literal(labelString.toString());
        if (generation == 1) {
            label.withStyle(ChatFormatting.YELLOW);
        } else if (generation >= 3) {
            label.withStyle(ChatFormatting.GRAY);
        }
        return label;
    }

    @Nullable
    public static Component getCustomTitle(ItemStack stack) {
        String s = NbtCanvas.getTitle(stack);
        if (!StringUtil.isNullOrEmpty(s)) {
            return Component.literal(s);
        }
        return null;
    }

    @Override
    public Component getName(ItemStack stack) {
        Component comp = getCustomTitle(stack);
        if (comp != null) {
            return comp;
        }
        return super.getName(stack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (NbtCanvas.hasPixels(stack)) {
            String author = NbtCanvas.getAuthor(stack);
            if (!StringUtil.isNullOrEmpty(author)) {
                tooltip.add(Component.translatable("canvas.byAuthor", author));
            }
            int generation = NbtCanvas.getGeneration(stack);
            if (generation > 0) {
                tooltip.add(Component.translatable("canvas.generation." + (generation - 1)).withStyle(generation == 1 ? ChatFormatting.GOLD : ChatFormatting.GRAY));
            }
        } else {
            tooltip.add(Component.translatable("canvas.empty").withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isFoil(ItemStack stack) {
        return NbtCanvas.getGeneration(stack) > 0;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return NbtCanvas.getGeneration(stack) > 0 ? SIGNED_STACK_SIZE : 1;
    }

    public int getWidth() {
        return CanvasType.getWidth(canvasType);
    }

    public int getHeight() {
        return CanvasType.getHeight(canvasType);
    }

    public CanvasType getCanvasType() {
        return canvasType;
    }

    private boolean mayPlace(Player player, Direction direction, ItemStack stack, BlockPos pos) {
        if (canvasType == CanvasType.SMALL) {
            return Level.isInSpawnableBounds(pos) && player.mayUseItemAt(pos, direction, stack);
        } else {
            return !direction.getAxis().isVertical() && player.mayUseItemAt(pos, direction, stack);
        }
    }

    public static String generateName(Player player) {
        return player.getUUID() + "_" + System.currentTimeMillis() / 100;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return CanvasItemRenderer.getInstance();
            }
        });
    }
}
