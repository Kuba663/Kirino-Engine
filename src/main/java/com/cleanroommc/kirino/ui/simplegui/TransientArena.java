package com.cleanroommc.kirino.ui.simplegui;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

final class TransientArena {

    private final int capacity;
    private final ByteBuffer buffer;

    @NonNull
    ByteBuffer view() {
        ByteBuffer dup = buffer.duplicate();
        dup.order(buffer.order()).flip();
        return dup;
    }

    void reset() {
        buffer.clear();
    }

    TransientArena(int capacity) {
        this.capacity = capacity;
        buffer = BufferUtils.createByteBuffer(capacity);
    }

    public int alloc(int size, int align) {
        Preconditions.checkArgument(Integer.bitCount(align) == 1,
                "Argument \"align\"=%s must be power of 2 and >= 2.", align);

        int p = buffer.position();
        int aligned = (p + align - 1) & ~(align - 1);

        if (aligned + size > capacity) {
            throw new RuntimeException(String.format(
                    "TransientArena overflow: current position=%d, capacity=%d",
                    aligned + size, capacity));
        }

        buffer.position(aligned + size);
        return aligned;
    }
}
