package com.leclowndu93150.joyofpainting.client.render.state;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.Direction;

public class EntityCanvasRenderState extends EntityRenderState {
    public String canvasId = "";
    public int canvasVersion = 0;
    public int canvasWidth = 16;
    public int canvasHeight = 16;
    public int canvasRotation = 0;
    public Direction facing = Direction.NORTH;
    public boolean glass;
    public boolean sidesActive;
    public int[] sidePixels = new int[0];
    public float yRot;
    public float xRot;
}
