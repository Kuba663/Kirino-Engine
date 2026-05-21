package com.cleanroommc.kirino.simpletext;

import com.cleanroommc.kirino.engine.render.core.shader.ImmediateShaderAccess;
import com.cleanroommc.kirino.simpletext.atlas.Tex2DGlyphAtlas;
import com.cleanroommc.kirino.simpletext.backend.DebugTextRenderer;
import com.cleanroommc.kirino.simpletext.command.TextCommandList;
import com.cleanroommc.kirino.simpletext.freetype.FreeTypeManager;
import com.cleanroommc.kirino.simpletext.glyph.GlyphMetrics;
import com.cleanroommc.kirino.simpletext.glyph.GlyphMetricsStore;
import com.cleanroommc.kirino.simpletext.sdf.SDFGenerator;
import net.minecraft.util.ResourceLocation;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FreeType;

public class SimpleTextRuntime {

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
    private final SimpleTextConsumer textConsumer;
    private final SimpleTextProducer textProducer;

    public SimpleTextRuntime(FreeTypeManager freeTypeManager, ImmediateShaderAccess shaderAccess, ResourceLocation fontRl) {
        this.fontRl = fontRl;
        face = freeTypeManager.load(fontRl, 0, SimpleTextConstants.PIXEL_SIZE);
        hasKerning = FreeType.FT_HAS_KERNING(face);

//        int[] outParallelism = new int[1];
//        ForkJoinPool workerPool = ForkJoinPoolUtils.newWorkStealingPool("KirinoSimpleTextSDF", outParallelism);
//        ShutdownManager.registerAsync(() -> ForkJoinPoolUtils.shutdownPool(workerPool, 5));
//        SDFGeneratorPool generatorPool = new SDFGeneratorPool(outParallelism[0], () ->
//                new SDFGenerator(SimpleTextConstants.SDF_PADDING, SimpleTextConstants.SDF_SPREAD));

        textConsumer = new DebugTextRenderer(
                this,
                new SDFGenerator(SimpleTextConstants.SDF_PADDING, SimpleTextConstants.SDF_SPREAD),
                new Tex2DGlyphAtlas(1024, 1024),
                shaderAccess);
        textProducer = new SimpleTextProducer(this, SimpleTextConstants.PIXEL_SIZE);
    }

    /**
     * It automatically loads a new metrics if the requested one wasn't loaded.
     *
     * <p>Note: <b>Not</b> guaranteed to be thread safe.</p>
     */
    @NonNull
    public GlyphMetrics getGlyphMetrics(int glyphIndex) {
        return metricsStore.loadMetricsIfAbsent(face, fontRl, glyphIndex);
    }

    /**
     * It straight up fetches the metrics. Will return <code>null</code> if the requested one wasn't loaded.
     *
     * <p>Note: Guaranteed to be thread safe.</p>
     */
    @Nullable
    public GlyphMetrics getGlyphMetricsDirectly(int glyphIndex) {
        return metricsStore.get(glyphIndex);
    }

    @NonNull
    public SimpleTextRuntime begin() {
        textProducer.beginBatch();
        return this;
    }

    @NonNull
    public SimpleTextRuntime endDraw() {
        textProducer.endBatch();
        textConsumer.consume(textProducer.submit());
        return this;
    }

    /**
     * <p>Debug Utility</p>
     * It's a simulated version of {@link #endDraw()}, which won't draw anything.
     */
    @NonNull
    public TextCommandList endPseudo() {
        textProducer.endBatch();
        return textProducer.submit().copy();
    }

    @NonNull
    public SimpleTextRuntime append(String text, float x, float y) {
        textProducer.append(text, x, y);
        return this;
    }
}
