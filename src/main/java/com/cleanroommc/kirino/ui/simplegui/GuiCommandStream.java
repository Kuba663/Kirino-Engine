package com.cleanroommc.kirino.ui.simplegui;

import org.jspecify.annotations.NonNull;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

public class GuiCommandStream {

    private final int capacity;
    private final ByteBuffer buffer;

    GuiCommandStream(int capacity) {
        this.capacity = capacity;
        buffer = BufferUtils.createByteBuffer(capacity);
    }

    @NonNull
    ByteBuffer view() {
        ByteBuffer dup = buffer.duplicate();
        dup.order(buffer.order()).flip();
        return dup;
    }

    void reset() {
        buffer.clear();
    }

    private static int align16(int x) {
        return (x + 15) & ~15;
    }

    private int begin(int op, int flags, int payload, int reserve) {
        int start = buffer.position();

        int used = SG_CmdHeader.HEADER_SIZE + payload;
        int size = align16(used + reserve);

        if (start + size > capacity) {
            throw new RuntimeException(String.format(
                    "GuiCommandStream overflow: current position=%d, capacity=%d",
                    start + size, capacity));
        }

        buffer.putInt(op);
        buffer.putInt(flags);
        buffer.putInt(size);
        buffer.putInt(used);

        return start;
    }

    private void end(int start) {
        int size = buffer.getInt(start + SG_CmdHeader.SIZE);
        int end = start + size;

        while (buffer.position() < end) {
            buffer.put((byte) 0);
        }
    }

    public void rect(float x, float y, float width, float height, int color) {
        int start = begin(SG_GuiOp.DRAW_RECT, 0, 20, SG_CmdHeader.TAIL_SIZE);

        buffer.putFloat(x);
        buffer.putFloat(y);
        buffer.putFloat(width);
        buffer.putFloat(height);
        buffer.putInt(color);

        end(start);
    }

    @NonNull
    public CmdRectBuilder rectEx(float x, float y, float width, float height, int color) {
        return new CmdRectBuilder(this, x, y, width, height, color);
    }

    void writeRectEx(float x, float y,
                     float width, float height,
                     int color,
                     int flags,
                     float radius,
                     int cornerType,
                     float borderWidth,
                     int borderColor,
                     float shadowBlur,
                     float shadowX,
                     float shadowY,
                     int shadowColor) {

        int payload = 20;

        if ((flags & SG_GuiOp.FLAG_RADIUS) != 0) {
            payload += 8;
        }
        if ((flags & SG_GuiOp.FLAG_BORDER) != 0) {
            payload += 8;
        }
        if ((flags & SG_GuiOp.FLAG_SHADOW) != 0) {
            payload += 16;
        }

        boolean needsMesh = (flags & SG_GuiOp.FLAG_RADIUS) != 0;

        // plus 2 ints (meshOffset + vertexCount) if needsMesh
        int start = begin(SG_GuiOp.DRAW_RECT, flags, payload, SG_CmdHeader.TAIL_SIZE + (needsMesh ? 8 : 0));

        buffer.putFloat(x);
        buffer.putFloat(y);
        buffer.putFloat(width);
        buffer.putFloat(height);
        buffer.putInt(color);

        if ((flags & SG_GuiOp.FLAG_RADIUS) != 0) {
            buffer.putFloat(radius);
            buffer.putInt(cornerType);
        }

        if ((flags & SG_GuiOp.FLAG_BORDER) != 0) {
            buffer.putFloat(borderWidth);
            buffer.putInt(borderColor);
        }

        if ((flags & SG_GuiOp.FLAG_SHADOW) != 0) {
            buffer.putFloat(shadowBlur);
            buffer.putFloat(shadowX);
            buffer.putFloat(shadowY);
            buffer.putInt(shadowColor);
        }

        end(start);
    }
}
