package com.cleanroommc.kirino.simpletext.command;

/**
 * <p>Note: {@link TextCommandList} must be owned by a {@link org.lwjgl.util.freetype.FT_Face} owner,
 * so <code>glyphIndex</code> therefore makes sense with a given font face.</p>
 */
public final class TextCommandList {

    private final TextCommandArena arena;

    public TextCommandList(int initialCapacity) {
        arena = new TextCommandArena(initialCapacity);
    }

    public void clear() {
        arena.reset();
    }

    public int size() {
        return arena.size();
    }

    /**
     * Push a missing glyph draw command (<code>glyphIndex</code> == 0 to be exact).
     */
    public void push(float x, float y) {
        int i = arena.alloc();

        arena.glyphIndex.set(i, 0);

        arena.x.set(i, x);
        arena.y.set(i, y);
    }

    /**
     * Push a glyph draw command.
     */
    public void push(
            int glyphIndex,
            float x, float y,
            float u0, float v0,
            float u1, float v1,
            float size,
            int page,
            int color,
            int hint) {

        int i = arena.alloc();

        arena.glyphIndex.set(i, glyphIndex);

        arena.x.set(i, x);
        arena.y.set(i, y);

        arena.u0.set(i, u0);
        arena.v0.set(i, v0);

        arena.u1.set(i, u1);
        arena.v1.set(i, v1);

        arena.size.set(i, size);

        arena.page.set(i, page);

        arena.color.set(i, color);
        arena.hint.set(i, hint);
    }

    public int glyphIndex(int i) {
        return arena.glyphIndex.get(i);
    }

    public float x(int i) {
        return arena.x.get(i);
    }

    public float y(int i) {
        return arena.y.get(i);
    }

    public float u0(int i) {
        return arena.u0.get(i);
    }

    public float v0(int i) {
        return arena.v0.get(i);
    }

    public float u1(int i) {
        return arena.u1.get(i);
    }

    public float v1(int i) {
        return arena.v1.get(i);
    }

    public float size(int i) {
        return arena.size.get(i);
    }

    public int page(int i) {
        return arena.page.get(i);
    }

    public int color(int i) {
        return arena.color.get(i);
    }
}