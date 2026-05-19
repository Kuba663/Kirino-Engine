package com.cleanroommc.kirino.simpletext;

import com.cleanroommc.kirino.simpletext.backend.DebugTextRenderer;
import com.cleanroommc.kirino.simpletext.freetype.FreeTypeManager;
import com.cleanroommc.kirino.simpletext.glyph.GlyphMetrics;
import com.cleanroommc.kirino.simpletext.glyph.GlyphMetricsStore;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FreeType;

public class TextContext {

    private final ResourceLocation fontRl;
    private final FT_Face face;
    private final boolean hasKerning;

    public ResourceLocation getFontRl() {
        return fontRl;
    }

    public FT_Face getFontFace() {
        return face;
    }

    public boolean hasFontKerning() {
        return hasKerning;
    }

    private final GlyphMetricsStore metricsStore = new GlyphMetricsStore();
    private final TextConsumer textConsumer;
    private final TextProducer textProducer;

    public TextContext(FreeTypeManager freeTypeManager, ResourceLocation fontRl) {
        this.fontRl = fontRl;
        face = freeTypeManager.load(fontRl, 0, 64);
        hasKerning = FreeType.FT_HAS_KERNING(face);

        textConsumer = new DebugTextRenderer(this);
        textProducer = new TextProducer(this);
    }

    public GlyphMetrics getMetrics(int glyphIndex) {
        return metricsStore.loadMetricsIfAbsent(face, fontRl, glyphIndex);
    }
}
