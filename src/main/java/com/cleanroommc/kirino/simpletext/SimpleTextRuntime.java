package com.cleanroommc.kirino.simpletext;

import com.cleanroommc.kirino.engine.render.core.shader.ImmediateShaderAccess;
import com.cleanroommc.kirino.simpletext.command.TextCommandList;
import com.cleanroommc.kirino.simpletext.glyph.GlyphMetrics;
import com.cleanroommc.kirino.simpletext.glyph.GlyphMetricsStore;
import com.google.common.base.Preconditions;
import net.minecraft.util.ResourceLocation;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.Function;

public class SimpleTextRuntime {

    private final ResourceLocation fontRl;
    private final ST_FontObject font;
    private final ST_Config config;
    private final ImmediateShaderAccess shaderAccess;

    public ResourceLocation getFontRl() {
        return fontRl;
    }

    public ST_FontObject getFont() {
        return font;
    }

    public ST_Config getConfig() {
        return config;
    }

    public ImmediateShaderAccess getShaderAccess() {
        return shaderAccess;
    }

    private final GlyphMetricsStore metricsStore;
    private final SimpleTextConsumer textConsumer;
    private final SimpleTextProducer textProducer;

    public SimpleTextRuntime(
            @NonNull BiFunction<ResourceLocation, ST_Config, ST_FontObject> fontFactory,
            @NonNull Function<SimpleTextRuntime, SimpleTextConsumer> consumerFactory,
            @NonNull Function<SimpleTextRuntime, SimpleTextProducer> producerFactory,
            @NonNull ImmediateShaderAccess shaderAccess,
            @NonNull ST_Config config,
            @NonNull ResourceLocation fontRl) {

        Preconditions.checkNotNull(fontFactory);
        Preconditions.checkNotNull(consumerFactory);
        Preconditions.checkNotNull(producerFactory);
        Preconditions.checkNotNull(shaderAccess);
        Preconditions.checkNotNull(config);
        Preconditions.checkNotNull(fontRl);

        this.fontRl = fontRl;
        this.config = config;
        font = fontFactory.apply(fontRl, config);

        Preconditions.checkState(font.type() == config.target(),
                "Backend must match. Font backend type=%s but config backend target=%s.",
                font.type().toString(), config.target().toString());

        this.shaderAccess = shaderAccess;

        metricsStore = new GlyphMetricsStore(config);

        textConsumer = consumerFactory.apply(this);
        textProducer = producerFactory.apply(this);

//        int[] outParallelism = new int[1];
//        ForkJoinPool workerPool = ForkJoinPoolUtils.newWorkStealingPool("KirinoSimpleTextSDF", outParallelism);
//        ShutdownManager.registerAsync(() -> ForkJoinPoolUtils.shutdownPool(workerPool, 5));
//        SDFGeneratorPool generatorPool = new SDFGeneratorPool(outParallelism[0], () ->
//                new SDFGenerator(SimpleTextConstants.SDF_PADDING, SimpleTextConstants.SDF_SPREAD));
    }

    /**
     * It automatically loads a new metrics if the requested one wasn't loaded.
     *
     * <p>Note: <b>Not</b> guaranteed to be thread safe.</p>
     */
    @NonNull
    public GlyphMetrics getGlyphMetrics(int glyphIndex) {
        return metricsStore.loadMetricsIfAbsent(font, fontRl, glyphIndex);
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
