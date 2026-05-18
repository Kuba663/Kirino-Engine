package com.cleanroommc.kirino.simpletext.command;

import com.cleanroommc.kirino.schemata.arena.SoAArena;

final class TextCommandArena {

    private final SoAArena arena;

    final SoAArena.IntColumn glyphIndex;

    final SoAArena.FloatColumn x;
    final SoAArena.FloatColumn y;

    final SoAArena.FloatColumn u0;
    final SoAArena.FloatColumn v0;

    final SoAArena.FloatColumn u1;
    final SoAArena.FloatColumn v1;

    final SoAArena.FloatColumn size;

    final SoAArena.IntColumn page;

    final SoAArena.IntColumn color;
    final SoAArena.IntColumn hint;

    TextCommandArena(int initialCapacity) {
        arena = new SoAArena(initialCapacity);

        glyphIndex = arena.intColumn();

        x = arena.floatColumn();
        y = arena.floatColumn();

        u0 = arena.floatColumn();
        v0 = arena.floatColumn();

        u1 = arena.floatColumn();
        v1 = arena.floatColumn();

        size = arena.floatColumn();

        page = arena.intColumn();

        color = arena.intColumn();
        hint = arena.intColumn();
    }

    int alloc() {
        return arena.alloc();
    }

    void reset() {
        arena.reset();
    }

    int size() {
        return arena.size();
    }
}