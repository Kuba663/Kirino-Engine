package com.cleanroommc.kirino.ui.simplegui;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

import java.nio.ByteBuffer;

public class GuiCompiler {

    private final TransientArena arena;

    GuiCompiler(TransientArena arena) {
        this.arena = arena;
    }

    public void compile(@NonNull GuiCommandStream stream) {
        Preconditions.checkNotNull(stream);

        ByteBuffer view = stream.view();

        int pos = 0;
        int end = view.limit();

        int layer = 0;

        while (pos < end) {
            int op = view.getInt(pos + SG_CmdHeader.OP);
            int flags = view.getInt(pos + SG_CmdHeader.FLAGS);
            int size = view.getInt(pos + SG_CmdHeader.SIZE);
            int used = view.getInt(pos + SG_CmdHeader.USED);

            // |---header---|---used---|---tail---|---other---|---padding---|
            view.putInt(pos + used, layer++);

            if (op == SG_GuiOp.DRAW_RECT && (flags & SG_GuiOp.FLAG_COMPILED) == 0) {
                compileRect(view, pos, flags, used, size);
            } else if (op == SG_GuiOp.DRAW_LINES && (flags & SG_GuiOp.FLAG_COMPILED) == 0) {
                compileLines(view, pos, flags, used, size);
            } else if (op == SG_GuiOp.DRAW_BEZIER && (flags & SG_GuiOp.FLAG_COMPILED) == 0) {
                compileBezier(view, pos, flags, used, size);
            } else if (op == SG_GuiOp.PUSH_CLIP && (flags & SG_GuiOp.FLAG_COMPILED) == 0) {
                compilePushClip(view, pos, flags, used, size);
            } else if (op == SG_GuiOp.POP_CLIP && (flags & SG_GuiOp.FLAG_COMPILED) == 0) {
                compilePopClip(view, pos, flags, used, size);
            } else {
                throw new IllegalStateException("Unknown SG_GuiOp: " + op);
            }

            pos += size;
        }
    }

    private void compileRect(ByteBuffer buffer, int pos, int flags, int used, int size) {
        int posPointer = pos + SG_CmdHeader.HEADER_SIZE;

        float x = buffer.getFloat(posPointer); posPointer += 4;
        float y = buffer.getFloat(posPointer); posPointer += 4;
        float width = buffer.getFloat(posPointer); posPointer += 4;
        float height = buffer.getFloat(posPointer); posPointer += 4;
        int color = buffer.getInt(posPointer); posPointer += 4;

        float radius = 0f;
        int cornerType = 0;

        if ((flags & SG_GuiOp.FLAG_RADIUS) != 0) {
            radius = buffer.getFloat(posPointer); posPointer += 4;
            cornerType = buffer.getInt(posPointer); posPointer += 4;
        }

        boolean needsMesh = (flags & SG_GuiOp.FLAG_RADIUS) != 0;

        if (!needsMesh) {
            buffer.putInt(pos + SG_CmdHeader.FLAGS, flags | SG_GuiOp.FLAG_COMPILED);
            return;
        }

        int[] out = new int[2];
        buildRoundedRectMesh(x, y, width, height, radius, cornerType, out);
        int meshOffset = out[0];
        int vertexCount = out[1];

        Preconditions.checkState(used + SG_CmdHeader.TAIL_SIZE + 8 <= size,
                "No reserved space for in-place rewrite (used=%s, want=%s, size=%s).",
                used, used + SG_CmdHeader.TAIL_SIZE + 8, size);

        int outputPos = used + SG_CmdHeader.TAIL_SIZE;

        buffer.putInt(outputPos, meshOffset);
        buffer.putInt(outputPos + 4, vertexCount);
        buffer.putInt(pos + SG_CmdHeader.FLAGS, flags | SG_GuiOp.FLAG_COMPILED);
    }

    private void compileLines(ByteBuffer buffer, int pos, int flags, int used, int size) {

    }

    private void compileBezier(ByteBuffer buffer, int pos, int flags, int used, int size) {

    }

    private void compilePushClip(ByteBuffer buffer, int pos, int flags, int used, int size) {

    }

    private void compilePopClip(ByteBuffer buffer, int pos, int flags, int used, int size) {

    }

    private void buildRoundedRectMesh(
            float x,
            float y,
            float width,
            float height,
            float radius,
            int cornerType,
            int[] out) {

        Preconditions.checkArgument(out.length == 2, "Length of \"out\" must be 2.");
        Preconditions.checkArgument(cornerType == 0 || cornerType == 1 || cornerType == 2 || cornerType == 3,
                "Argument \"cornerType\"=%s must be either 0 or 1 or 2 or 3.", cornerType);

        boolean bezier = cornerType == 2 || cornerType == 3;

        // todo
        if (!bezier) {
            // 0: 5 vertices
            // 1: 10 vertices
            int cornerVertCount = cornerType == 0 ? 5 : 10;
            int size = cornerVertCount * 4 * 8; // 8 bytes per vert (vec2)
            int offset = arena.alloc(size, 16);
            ByteBuffer view = arena.view();

            out[0] = offset;
            out[1] = cornerVertCount * 4;

        } else {
            // 2: 8 vertices
            // 3: 16 vertices
            int cornerVertCount = cornerType == 2 ? 8 : 16;
            int size = cornerVertCount * 4 * 8; // 8 bytes per vert (vec2)
            int offset = arena.alloc(size, 16);
            ByteBuffer view = arena.view();

            out[0] = offset;
            out[1] = cornerVertCount * 4;

        }
    }
}
