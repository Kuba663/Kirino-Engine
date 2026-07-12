package com.cleanroommc.kirino.engine.process.graphics.view;

import com.cleanroommc.kirino.ecs.CleanECSRuntime;
import com.cleanroommc.kirino.engine.FramePhase;
import com.cleanroommc.kirino.engine.FramePhaseTiming;
import com.cleanroommc.kirino.engine.render.core.*;
import com.cleanroommc.kirino.engine.render.core.debug.hud.InGameDebugHUDManager;
import com.cleanroommc.kirino.engine.render.core.pipeline.GLStateBackup;
import com.cleanroommc.kirino.engine.render.core.pipeline.draw.cmd.HighLevelDC;
import com.cleanroommc.kirino.engine.render.core.pipeline.draw.cmd.LowLevelDC;
import com.cleanroommc.kirino.engine.render.core.pipeline.post.FrameFinalizer;
import com.cleanroommc.kirino.engine.render.usage.McIntegrationBundle;
import com.cleanroommc.kirino.engine.render.usage.McSceneViewState;
import com.cleanroommc.kirino.engine.resource.ResourceStorage;
import com.cleanroommc.kirino.engine.world.context.GraphicsWorldView;
import com.cleanroommc.kirino.engine.world.context.WorldContext;
import com.cleanroommc.kirino.engine.world.type.Graphics;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class GraphicsWorldViewImpl implements GraphicsWorldView {

    private final CleanECSRuntime ecs;
    private final RenderStructure render;
    private final RenderExtensions extensions;
    private final EventBus eventBus;
    private final Logger logger;
    private final ResourceStorage storage;
    private final BuiltinShaderBundle builtinShaderBundle;
    private final GraphicsRuntimeBundle graphicsRuntimeBundle;
    private final McIntegrationBundle mcIntegrationBundle;
    private final McSceneViewState mcSceneViewState;
    private final ShaderIntrospection shaderIntrospection;

    private final Map<FramePhase,
            Map<FramePhaseTiming, List<Consumer<WorldContext<Graphics>>>>> callbacks =
            new Object2ObjectOpenHashMap<>();

    public GraphicsWorldViewImpl(
            @NonNull CleanECSRuntime ecs,
            @NonNull RenderStructure render,
            @NonNull RenderExtensions extensions,
            @NonNull EventBus eventBus,
            @NonNull Logger logger,
            @NonNull ResourceStorage storage,
            @NonNull BuiltinShaderBundle builtinShaderBundle,
            @NonNull GraphicsRuntimeBundle graphicsRuntimeBundle,
            @NonNull McIntegrationBundle mcIntegrationBundle,
            @NonNull McSceneViewState mcSceneViewState,
            @NonNull ShaderIntrospection shaderIntrospection) {

        Preconditions.checkNotNull(ecs);
        Preconditions.checkNotNull(render);
        Preconditions.checkNotNull(extensions);
        Preconditions.checkNotNull(eventBus);
        Preconditions.checkNotNull(logger);
        Preconditions.checkNotNull(storage);
        Preconditions.checkNotNull(builtinShaderBundle);
        Preconditions.checkNotNull(graphicsRuntimeBundle);
        Preconditions.checkNotNull(mcIntegrationBundle);
        Preconditions.checkNotNull(mcSceneViewState);
        Preconditions.checkNotNull(shaderIntrospection);

        this.ecs = ecs;
        this.render = render;
        this.extensions = extensions;
        this.eventBus = eventBus;
        this.logger = logger;
        this.storage = storage;
        this.builtinShaderBundle = builtinShaderBundle;
        this.graphicsRuntimeBundle = graphicsRuntimeBundle;
        this.mcIntegrationBundle = mcIntegrationBundle;
        this.mcSceneViewState = mcSceneViewState;
        this.shaderIntrospection = shaderIntrospection;
    }

    @NonNull
    @Override
    public CleanECSRuntime ecs() {
        return ecs;
    }

    @NonNull
    @Override
    public RenderStructure rs() {
        return render;
    }

    @NonNull
    @Override
    public RenderExtensions ext() {
        return extensions;
    }

    @NonNull
    @Override
    public Logger logger() {
        return logger;
    }

    @NonNull
    @Override
    public EventBus bus() {
        return eventBus;
    }

    @NonNull
    @Override
    public ResourceStorage storage() {
        return storage;
    }

    @NonNull
    @Override
    public ShaderIntrospection shaderi() {
        return shaderIntrospection;
    }

    @NonNull
    @Override
    public BuiltinShaderBundle shaderb() {
        return builtinShaderBundle;
    }

    @NonNull
    @Override
    public GraphicsRuntimeBundle graphicsb() {
        return graphicsRuntimeBundle;
    }

    @NonNull
    @Override
    public McIntegrationBundle mcib() {
        return mcIntegrationBundle;
    }

    @NonNull
    @Override
    public McSceneViewState mcscene() {
        return mcSceneViewState;
    }

    @Override
    public void run(@NonNull FramePhase phase) {
        Map<FramePhaseTiming, List<Consumer<WorldContext<Graphics>>>> map = callbacks.get(phase);
        if (map != null) {
            List<Consumer<WorldContext<Graphics>>> list = map.get(FramePhaseTiming.BEFORE);
            if (list != null) {
                for (Consumer<WorldContext<Graphics>> consumer : list) {
                    consumer.accept(this);
                }
            }
        }

        switch (phase) {
            case PRE_UPDATE -> {

                FrameFinalizer frameFinalizer = storage.get(graphicsRuntimeBundle.frameFinalizer);
                frameFinalizer.updateResolution();

                // current render target: main framebuffer
                frameFinalizer.bindMainFramebuffer(true);
                GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);
            }
            case UPDATE -> {
                storage.get(graphicsRuntimeBundle.graphicResourceManager).runStaging();
                mcSceneViewState.scene.tryUpdateWorld(Minecraft.getMinecraft().world);
                mcSceneViewState.scene.update();
            }
            case POST_UPDATE -> {
                FrameFinalizer frameFinalizer = storage.get(graphicsRuntimeBundle.frameFinalizer);

                frameFinalizer.finalizeFramebuffer(storage);

                // current render target: minecraft framebuffer
                frameFinalizer.bindMinecraftFramebuffer(true);

                GL30.glBindVertexArray(0);
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
                GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

                HighLevelDC.nextGen();
                LowLevelDC.nextGen();

                // set up fixed-func overlay rendering
                ScaledResolution resolution = new ScaledResolution(Minecraft.getMinecraft());
                GL11.glMatrixMode(GL11.GL_PROJECTION);
                GL11.glLoadIdentity();
                GL11.glOrtho(0, resolution.getScaledWidth_double(), resolution.getScaledHeight_double(), 0, -1, 1);
                GL11.glMatrixMode(GL11.GL_MODELVIEW);
                GL11.glLoadIdentity();
            }
            case RENDER_OPAQUE -> {
                // test
                glStateBackup.storeStates();
                int vbo = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);

//                if (sceneViewState.scene.isMeshletRenderReady()) {
//                    rs().terrainGpuPass.render(
//                            storage,
//                            sceneViewState.camera,
//                            null,
//                            new Object[]{sceneViewState.scene.getMeshletRenderPayload()});
//                }
//                rs().chunkCpuPass.render(sceneViewState.camera);

                glStateBackup.restoreStates();
                GL30.glBindVertexArray(0);
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
                GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
            }
            case RENDER_TRANSPARENT -> {
                // test
                glStateBackup.storeStates();
                int vbo = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);

                rs().gizmosPassDesc.acquire().render(storage, mcSceneViewState.camera);

                glStateBackup.restoreStates();
                GL30.glBindVertexArray(0);
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
                GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
            }
            case RENDER_OVERLAY -> {
                InGameDebugHUDManager debugHudManager = storage.get(graphicsRuntimeBundle.debugHudManager);
                debugHudManager.updateAndRenderIfNeeded();
            }
        }

        if (map != null) {
            List<Consumer<WorldContext<Graphics>>> list = map.get(FramePhaseTiming.AFTER);
            if (list != null) {
                for (Consumer<WorldContext<Graphics>> consumer : list) {
                    consumer.accept(this);
                }
            }
        }
    }

    // test
    private GLStateBackup glStateBackup = new GLStateBackup();

    @Override
    public void on(@NonNull FramePhase phase, @NonNull FramePhaseTiming timing, @NonNull Consumer<WorldContext<Graphics>> consumer) {
        Preconditions.checkNotNull(phase);
        Preconditions.checkNotNull(timing);
        Preconditions.checkNotNull(consumer);

        Map<FramePhaseTiming, List<Consumer<WorldContext<Graphics>>>> map = callbacks.computeIfAbsent(phase, k -> new Object2ObjectOpenHashMap<>());
        List<Consumer<WorldContext<Graphics>>> list = map.computeIfAbsent(timing, k -> new ArrayList<>());
        list.add(consumer);
    }
}
