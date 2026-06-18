package com.cleanroommc.kirino.ui.simplegui;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

public final class CmdLinesBuilder {

    private final GuiCommandStream out;

    private final int vertexNum;
    private final float lineWidth;
    private final float[] vertices;
    private final boolean formsLoop;

    private int index = 0;

    CmdLinesBuilder(GuiCommandStream out, int vertexNum, float lineWidth, boolean formsLoop) {
        this.out = out;
        this.vertexNum = vertexNum;
        this.lineWidth = lineWidth;
        vertices = new float[vertexNum * 2];
        this.formsLoop = formsLoop;
    }

    @NonNull
    public CmdLinesBuilder set(int vertIndex, float x, float y) {
        Preconditions.checkElementIndex(vertIndex, vertexNum);

        vertices[vertIndex * 2] = x;
        vertices[vertIndex * 2 + 1] = y;
        return this;
    }

    @NonNull
    public CmdLinesBuilder put(float x, float y) {
        Preconditions.checkElementIndex(index, vertexNum);

        vertices[index * 2] = x;
        vertices[index * 2 + 1] = y;
        index++;
        return this;
    }

    public void emit() {
        out.writeLines(vertexNum, lineWidth, vertices, formsLoop);
    }
}
