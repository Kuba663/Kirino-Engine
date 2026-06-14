package com.cleanroommc.kirino.ui.simplegui;

import com.cleanroommc.kirino.engine.ShutdownManager;
import com.cleanroommc.kirino.gl.buffer.GLBuffer;
import com.cleanroommc.kirino.gl.buffer.view.IDBView;
import com.cleanroommc.kirino.gl.buffer.view.SSBOView;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class GuiRenderer {

    private static final int DRAW_INFO_STRIDE = 16;
    private static final int IDB_STRIDE = 16;
    private static final int RECT_PAYLOAD_STRIDE = 64;
    private static final int LINES_PAYLOAD_STRIDE = 1;
    private static final int BEZIER_PAYLOAD_STRIDE = 1;

    // stride=8
    // vec2 pos
    private final TransientArena arena;
    private final SSBOView arenaSsbo;

    // stride=16
    // int drawType (implies mode)
    // int flags
    // int payloadIndex
    // float depth: [0, 1]
    private ByteBuffer drawInfoWorkspace = null;
    private final SSBOView drawInfo;

    // stride=64
    // vec4 rect
    // vec2 shadow
    // int meshOffset (for rounded rect; refers to transient arena)
    // int vertexCount (for rounded rect; refers to transient arena)
    // int color
    // float borderWidth
    // int borderColor
    // float shadowBlur
    // int shadowColor
    // float radius
    private ByteBuffer rectPayloadWorkspace = null;
    private final SSBOView rectPayload;

    private ByteBuffer linesPayloadWorkspace = null;
    private final SSBOView linesPayload;

    private ByteBuffer bezierPayloadWorkspace = null;
    private final SSBOView bezierPayload;

    // stride=16
    // int count: actual vert count (= TRIANGLES mode vert count. != vert count from the input stream.
    //     every type uses diff mode. normalize in vert shader)
    // int instanceCount = 1
    // int first = 0
    // int baseInstance: drawInfo SSBO index
    private ByteBuffer idbWorkspace = null;
    private final IDBView idb;

    GuiRenderer(TransientArena arena) {
        this.arena = arena;
        arenaSsbo = new SSBOView(new GLBuffer());
        drawInfo = new SSBOView(new GLBuffer());
        rectPayload = new SSBOView(new GLBuffer());
        linesPayload = new SSBOView(new GLBuffer());
        bezierPayload = new SSBOView(new GLBuffer());
        idb = new IDBView(new GLBuffer());

        ShutdownManager.registerAsync(() -> {
            MemoryUtil.memFree(drawInfoWorkspace);
            MemoryUtil.memFree(rectPayloadWorkspace);
            MemoryUtil.memFree(linesPayloadWorkspace);
            MemoryUtil.memFree(bezierPayloadWorkspace);
            MemoryUtil.memFree(idbWorkspace);
        });
    }

    /**
     * Arbitrary alignment implementation (not necessarily power-of-two).
     *
     * <p>Note: It assumes no arithmetic overflow and doesn't handle such situation.</p>
     */
    private static int alignUp(int pos, int align) {
        if (align <= 1) {
            return pos;
        }
        if (Integer.bitCount(align) == 1) {
            return (pos + align - 1) & ~(align - 1);
        } else {
            int r = pos % align;
            return r == 0 ? pos : pos + align - r;
        }
    }

    private static final int[] CURSOR = {0, 0, 0};

    private int calcMaxCommandBatch(ByteBuffer buffer, int[] out) {
        Preconditions.checkArgument(out.length == 3);

        int pos = 0;
        int end = buffer.limit();

        int maxCount = 0;
        int count = 0;

        while (pos < end) {
            int op = buffer.getInt(pos + SG_CmdHeader.OP);
            int size = buffer.getInt(pos + SG_CmdHeader.SIZE);

            if (op == SG_GuiOp.DRAW_RECT || op == SG_GuiOp.DRAW_LINES || op == SG_GuiOp.DRAW_BEZIER) {
                count++;

                if (op == SG_GuiOp.DRAW_RECT) {
                    out[0]++;
                } else if (op == SG_GuiOp.DRAW_LINES) {
                    out[1]++;
                } else {
                    out[2]++;
                }
            } else if (op == SG_GuiOp.PUSH_CLIP || op == SG_GuiOp.POP_CLIP) {
                if (count > 0) {
                    count = 0;
                }
            } else {
                throw new IllegalStateException("Unknown SG_GuiOp: " + op);
            }

            maxCount = Math.max(maxCount, count);
            pos += size;
        }

        return maxCount;
    }

    /**
     * It initializes and resets all buffer workspaces: {@link #drawInfoWorkspace}, {@link #rectPayloadWorkspace},
     * {@link #linesPayloadWorkspace}, {@link #bezierPayloadWorkspace}, {@link #idbWorkspace}
     *
     * <p>Note: Some buffer may still remain <code>null</code> and unallocated after this call.</p>
     */
    private void initBufferWorkspaces(int commandCount, int rectCount, int linesCount, int bezierCount) {
        int drawInfoSize = commandCount * DRAW_INFO_STRIDE;
        if (drawInfoSize > 0) {
            if (drawInfoWorkspace == null) {
                drawInfoWorkspace = MemoryUtil.memAlloc(drawInfoSize);
            } else if (drawInfoWorkspace.capacity() < drawInfoSize) {
                MemoryUtil.memFree(drawInfoWorkspace);
                drawInfoWorkspace = MemoryUtil.memAlloc(drawInfoSize);
            }
            drawInfoWorkspace.clear();
        }

        int rectPayloadSize = rectCount * RECT_PAYLOAD_STRIDE;
        if (rectPayloadSize > 0) {
            if (rectPayloadWorkspace == null) {
                rectPayloadWorkspace = MemoryUtil.memAlloc(rectPayloadSize);
            } else if (rectPayloadWorkspace.capacity() < rectPayloadSize) {
                MemoryUtil.memFree(rectPayloadWorkspace);
                rectPayloadWorkspace = MemoryUtil.memAlloc(rectPayloadSize);
            }
            rectPayloadWorkspace.clear();
        }

        int linesPayloadSize = linesCount * LINES_PAYLOAD_STRIDE;
        if (linesPayloadSize > 0) {
            if (linesPayloadWorkspace == null) {
                linesPayloadWorkspace = MemoryUtil.memAlloc(linesPayloadSize);
            } else if (linesPayloadWorkspace.capacity() < linesPayloadSize) {
                MemoryUtil.memFree(linesPayloadWorkspace);
                linesPayloadWorkspace = MemoryUtil.memAlloc(linesPayloadSize);
            }
            linesPayloadWorkspace.clear();
        }

        int bezierPayloadSize = bezierCount * BEZIER_PAYLOAD_STRIDE;
        if (bezierPayloadSize > 0) {
            if (bezierPayloadWorkspace == null) {
                bezierPayloadWorkspace = MemoryUtil.memAlloc(bezierPayloadSize);
            } else if (bezierPayloadWorkspace.capacity() < bezierPayloadSize) {
                MemoryUtil.memFree(bezierPayloadWorkspace);
                bezierPayloadWorkspace = MemoryUtil.memAlloc(bezierPayloadSize);
            }
            bezierPayloadWorkspace.clear();
        }

        int idbSize = commandCount * IDB_STRIDE;
        if (idbSize > 0) {
            if (idbWorkspace == null) {
                idbWorkspace = MemoryUtil.memAlloc(idbSize);
            } else if (idbWorkspace.capacity() < idbSize) {
                MemoryUtil.memFree(idbWorkspace);
                idbWorkspace = MemoryUtil.memAlloc(idbSize);
            }
            idbWorkspace.clear();
        }
    }

    private void writeIdb(ByteBuffer buffer, int op, int pos, int flags, int used, int drawInfoPos) {
        // int count
        if (op == SG_GuiOp.DRAW_RECT) {
            boolean needsMesh = (flags & SG_GuiOp.FLAG_RADIUS) != 0;
            if (needsMesh) {
                int vertexCountPos = pos + SG_CmdHeader.HEADER_SIZE + used + SG_CmdHeader.TAIL_SIZE + 4;
                int vertexCount = buffer.getInt(vertexCountPos); // fan vert count
                // todo: fan 2 tri mesh conversion
            } else {
                idbWorkspace.putInt(6); // 2 tris
            }
        } else if (op == SG_GuiOp.DRAW_LINES) {

        } else if (op == SG_GuiOp.DRAW_BEZIER) {

        } else {
            throw new IllegalStateException("Invalid SG_GuiOp: " + op);
        }

        // int instanceCount
        idbWorkspace.putInt(1);

        // int first
        idbWorkspace.putInt(0);

        // int baseInstance
        idbWorkspace.putInt(drawInfoPos / DRAW_INFO_STRIDE);
    }

    private int writeDrawInfo(ByteBuffer buffer, int op, int pos, int flags, int used, int payloadPos) {
        int drawInfoPos = buffer.position();

        // int drawType
        drawInfoWorkspace.putInt(op);

        // int flags
        drawInfoWorkspace.putInt(flags);

        // int payloadIndex
        if (op == SG_GuiOp.DRAW_RECT) {
            drawInfoWorkspace.putInt(payloadPos / RECT_PAYLOAD_STRIDE);
        } else if (op == SG_GuiOp.DRAW_LINES) {
            drawInfoWorkspace.putInt(payloadPos / LINES_PAYLOAD_STRIDE);
        } else if (op == SG_GuiOp.DRAW_BEZIER) {
            drawInfoWorkspace.putInt(payloadPos / BEZIER_PAYLOAD_STRIDE);
        } else {
            throw new IllegalStateException("Invalid SG_GuiOp: " + op);
        }

        // float depth
        int depthPos = pos + SG_CmdHeader.HEADER_SIZE + used + 4;
        float depth = buffer.getFloat(depthPos);
        drawInfoWorkspace.putFloat(depth);

        return drawInfoPos;
    }

    private int writeRectPayload(ByteBuffer buffer, int pos, int flags, int used) {
        int posPointer = pos + SG_CmdHeader.HEADER_SIZE;

        float x = buffer.getFloat(posPointer); posPointer += 4;
        float y = buffer.getFloat(posPointer); posPointer += 4;
        float width = buffer.getFloat(posPointer); posPointer += 4;
        float height = buffer.getFloat(posPointer); posPointer += 4;
        int color = buffer.getInt(posPointer); posPointer += 4;

        boolean hasRadius = (flags & SG_GuiOp.FLAG_RADIUS) != 0;
        float radius = 0f;
        if (hasRadius) {
            radius = buffer.getFloat(posPointer);
            posPointer += 8;
        }

        boolean hasBorder = (flags & SG_GuiOp.FLAG_BORDER) != 0;
        float borderWidth = 0f;
        int borderColor = 0;
        if (hasBorder) {
            borderWidth = buffer.getFloat(posPointer);
            posPointer += 4;
            borderColor = buffer.getInt(posPointer);
            posPointer += 4;
        }

        boolean hasShadow = (flags & SG_GuiOp.FLAG_SHADOW) != 0;
        float shadowBlur = 0f;
        float shadowX = 0f;
        float shadowY = 0f;
        int shadowColor = 0;
        if (hasShadow) {
            shadowBlur = buffer.getFloat(posPointer);
            posPointer += 4;
            shadowX = buffer.getFloat(posPointer);
            posPointer += 4;
            shadowY = buffer.getFloat(posPointer);
            posPointer += 4;
            shadowColor = buffer.getInt(posPointer);
            posPointer += 4;
        }

        int meshOffset = 0;
        int vertexCount = 0;
        // needs mesh
        if (hasRadius) {
            int meshOffsetPos = pos + SG_CmdHeader.HEADER_SIZE + used + SG_CmdHeader.TAIL_SIZE;
            int vertexCountPos = pos + SG_CmdHeader.HEADER_SIZE + used + SG_CmdHeader.TAIL_SIZE + 4;
            meshOffset = buffer.getInt(meshOffsetPos);
            vertexCount = buffer.getInt(vertexCountPos);
        }

        int payloadPos = rectPayloadWorkspace.position();

        // vec4 rect
        rectPayloadWorkspace.putFloat(x);
        rectPayloadWorkspace.putFloat(y);
        rectPayloadWorkspace.putFloat(width);
        rectPayloadWorkspace.putFloat(height);

        // vec2 shadow
        rectPayloadWorkspace.putFloat(shadowX);
        rectPayloadWorkspace.putFloat(shadowY);

        // int meshOffset
        rectPayloadWorkspace.putInt(meshOffset);

        // int vertexCount
        rectPayloadWorkspace.putInt(vertexCount);

        // int color
        rectPayloadWorkspace.putInt(color);

        // float borderWidth
        rectPayloadWorkspace.putFloat(borderWidth);

        // int borderColor
        rectPayloadWorkspace.putInt(borderColor);

        // float shadowBlur
        rectPayloadWorkspace.putFloat(shadowBlur);

        // int shadowColor
        rectPayloadWorkspace.putInt(shadowColor);

        // float radius
        rectPayloadWorkspace.putFloat(radius);

        int next = alignUp(rectPayloadWorkspace.position(), RECT_PAYLOAD_STRIDE);
        while (rectPayloadWorkspace.position() < next) {
            buffer.put((byte) 0);
        }

        return payloadPos;
    }

    private int writeLinesPayload(ByteBuffer buffer, int pos, int flags) {
        return 0;
    }

    private int writeBezierPayload(ByteBuffer buffer, int pos, int flags) {
        return 0;
    }

    public void render(@NonNull GuiCommandStream stream) {
        Preconditions.checkNotNull(stream);

        CURSOR[0] = 0;
        CURSOR[1] = 0;
        CURSOR[2] = 0;
        int commandCount = calcMaxCommandBatch(stream.view(), CURSOR);
        int rectCount = CURSOR[0];
        int linesCount = CURSOR[1];
        int bezierCount = CURSOR[2];
        initBufferWorkspaces(commandCount, rectCount, linesCount, bezierCount);

        ByteBuffer view = stream.view();

        int pos = 0;
        int end = view.limit();

        int count = 0;

        while (pos < end) {
            int op = view.getInt(pos + SG_CmdHeader.OP);
            int flags = view.getInt(pos + SG_CmdHeader.FLAGS);
            int size = view.getInt(pos + SG_CmdHeader.SIZE);
            int used = view.getInt(pos + SG_CmdHeader.USED);

            Preconditions.checkState((flags & SG_GuiOp.FLAG_COMPILED) != 0,
                    "Command (pos=%s, op=%s) must be compiled already.", pos, op);

            if (op == SG_GuiOp.DRAW_RECT) {
                int _pos = writeRectPayload(view, pos, flags, used);
                int __pos = writeDrawInfo(view, op, pos, flags, used, _pos);
                writeIdb(view, op, pos, flags, used, __pos);

            } else if (op == SG_GuiOp.DRAW_LINES) {
                int _pos = writeLinesPayload(view, pos, flags);
                int __pos = writeDrawInfo(view, op, pos, flags, used, _pos);
                writeIdb(view, op, pos, flags, used, __pos);

            } else if (op == SG_GuiOp.DRAW_BEZIER) {
                int _pos = writeBezierPayload(view, pos, flags);
                int __pos = writeDrawInfo(view, op, pos, flags, used, _pos);
                writeIdb(view, op, pos, flags, used, __pos);

            } else if (op == SG_GuiOp.PUSH_CLIP) {
                if (count > 0) {
                    count = 0;
                    flush();
                }
                pushClip();

            } else if (op == SG_GuiOp.POP_CLIP) {
                if (count > 0) {
                    count = 0;
                    flush();
                }
                popClip();

            } else {
                throw new IllegalStateException("Unknown SG_GuiOp: " + op);
            }

            count++;
            pos += size;
        }

        if (count > 0) {
            count = 0;
            flush();
        }
    }

    private void pushClip() {

    }

    private void popClip() {

    }

    private void flush() {

    }
}
