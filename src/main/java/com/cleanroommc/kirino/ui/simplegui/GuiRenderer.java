package com.cleanroommc.kirino.ui.simplegui;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

import java.nio.ByteBuffer;

public class GuiRenderer {

    GuiRenderer() {
    }

    public void bake(@NonNull GuiCommandStream stream) {
        Preconditions.checkNotNull(stream);

        ByteBuffer view = stream.view();

        int pos = 0;
        int end = view.limit();

        while (pos < end) {
            int op = view.getInt(pos + SG_CmdHeader.OP);
            int flags = view.getInt(pos + SG_CmdHeader.FLAGS);
            int size = view.getInt(pos + SG_CmdHeader.SIZE);
            int used = view.getInt(pos + SG_CmdHeader.USED);

            Preconditions.checkState((flags & SG_GuiOp.FLAG_COMPILED) != 0,
                    "Command (pos=%s, op=%s) must be compiled already.", pos, op);

            if (op == SG_GuiOp.DRAW_RECT) {

            } else if (op == SG_GuiOp.DRAW_LINES) {

            } else if (op == SG_GuiOp.DRAW_BEZIER) {

            } else if (op == SG_GuiOp.PUSH_CLIP) {

            } else if (op == SG_GuiOp.POP_CLIP) {

            } else {
                throw new IllegalStateException("Unknown SG_GuiOp: " + op);
            }

            pos += size;
        }
    }

    public void render() {

    }
}
