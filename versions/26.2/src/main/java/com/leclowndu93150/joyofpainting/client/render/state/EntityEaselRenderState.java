package com.leclowndu93150.joyofpainting.client.render.state;

import com.leclowndu93150.joyofpainting.CanvasType;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;

public class EntityEaselRenderState extends EntityRenderState {
    public float yRot;
    public ItemStack canvasItem = ItemStack.EMPTY;
    public @Nullable CanvasType canvasType;
    public boolean hasCanvas;
    public boolean canvasGlass;
    public int canvasWidth;
    public int canvasHeight;
}
