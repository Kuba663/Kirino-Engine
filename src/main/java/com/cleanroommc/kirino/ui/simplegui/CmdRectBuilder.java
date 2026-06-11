package com.cleanroommc.kirino.ui.simplegui;

import org.jspecify.annotations.NonNull;

public final class CmdRectBuilder {

    private final GuiCommandStream out;

    private final float x, y, width, height;
    private final int color;

    private int flags;

    private float radius;
    private int cornerType;

    private float borderWidth;
    private int borderColor;

    private float shadowBlur;
    private float shadowX;
    private float shadowY;
    private int shadowColor;

    CmdRectBuilder(GuiCommandStream out, float x, float y, float width, float height, int color) {
        this.out = out;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = color;
    }

    @NonNull
    public CmdRectBuilder radius(float r, int type) {
        flags |= SG_GuiOp.FLAG_RADIUS;
        radius = r;
        cornerType = type;
        return this;
    }

    @NonNull
    public CmdRectBuilder border(float width, int color) {
        flags |= SG_GuiOp.FLAG_BORDER;
        borderWidth = width;
        borderColor = color;
        return this;
    }

    @NonNull
    public CmdRectBuilder shadow(float blur, float offsetX, float offsetY, int color) {
        flags |= SG_GuiOp.FLAG_SHADOW;
        shadowBlur = blur;
        shadowX = offsetX;
        shadowY = offsetY;
        shadowColor = color;
        return this;
    }

    public void emit() {
        out.writeRectEx(
                x, y,
                width, height,
                color,
                flags,
                radius,
                cornerType,
                borderWidth, borderColor,
                shadowBlur, shadowX, shadowY, shadowColor);
    }
}
