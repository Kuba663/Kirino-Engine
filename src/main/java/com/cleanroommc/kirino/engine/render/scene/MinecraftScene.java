package com.cleanroommc.kirino.engine.render.scene;

import com.cleanroommc.kirino.KirinoCore;
import com.cleanroommc.kirino.ecs.entity.CleanEntityHandle;
import com.cleanroommc.kirino.ecs.entity.EntityDestroyContext;
import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.entity.IEntityDestroyCallback;
import com.cleanroommc.kirino.ecs.job.JobScheduler;
import com.cleanroommc.kirino.ecs.world.CleanWorld;
import com.cleanroommc.kirino.engine.render.camera.MinecraftCamera;
import com.cleanroommc.kirino.engine.render.geometry.component.ChunkComponent;
import com.cleanroommc.kirino.engine.render.gizmos.GizmosManager;
import com.cleanroommc.kirino.engine.render.task.system.ChunkMeshletGenSystem;
import com.cleanroommc.kirino.engine.render.task.system.ChunkPrioritizationSystem;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.util.math.ChunkPos;
import org.apache.commons.lang3.time.StopWatch;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MinecraftScene extends CleanWorld {
    private final GizmosManager gizmosManager;
    private final MinecraftCamera camera;

    public MinecraftScene(EntityManager entityManager, JobScheduler jobScheduler, GizmosManager gizmosManager, MinecraftCamera camera) {
        super(entityManager, jobScheduler);
        this.gizmosManager = gizmosManager;
        this.camera = camera;
    }

    static class ChunkDestroyCallback implements IEntityDestroyCallback {
        @Override
        public void beforeDestroy(@NonNull EntityDestroyContext destroyContext) {
            ChunkComponent chunkComponent = (ChunkComponent) destroyContext.getComponent(ChunkComponent.class);
            //KirinoCore.LOGGER.info("chunk destroyed: " + chunkComponent.chunkPosX + ", " + chunkComponent.chunkPosY + ", " + chunkComponent.chunkPosZ);
        }
    }

    record ChunkPosKey(int x, int y, int z) {
    }

    private final Map<ChunkPosKey, CleanEntityHandle> chunkHandles = new HashMap<>();

    private static final ChunkDestroyCallback CHUNK_DESTROY_CALLBACK = new ChunkDestroyCallback();

    private boolean rebuildWorld = false;
    private ChunkProviderClient chunkProvider = null;

    public void tryUpdateChunkProvider(ChunkProviderClient chunkProvider) {
        if (this.chunkProvider != chunkProvider) {
            rebuildWorld = true;
            this.chunkProvider = chunkProvider;
            this.chunkProvider.loadChunkCallback = (x, z) -> {
                for (int i = 0; i < 16; i++) {
                    ChunkComponent chunkComponent = new ChunkComponent();
                    chunkComponent.chunkPosX = x;
                    chunkComponent.chunkPosY = i;
                    chunkComponent.chunkPosZ = z;
                    chunkHandles.put(new ChunkPosKey(x, i, z), entityManager.createEntity(CHUNK_DESTROY_CALLBACK, chunkComponent));
                }
            };
            this.chunkProvider.unloadChunkCallback = (x, z) -> {
                for (int i = 0; i < 16; i++) {
                    ChunkPosKey key = new ChunkPosKey(x, i, z);
                    CleanEntityHandle handle = chunkHandles.get(key);
                    if (handle != null) {
                        handle.tryDestroy();
                        chunkHandles.remove(key);
                    }
                }
            };
            // all changes are buffered and will be consumed at the end of this update
        }
    }

    public void notifyBlockUpdate(int x, int y, int z, IBlockState oldState, IBlockState newState) {
    }

    public void notifyLightUpdate(int x, int y, int z) {
    }

    // temp
    ChunkPrioritizationSystem chunkPrioritizationSystem = null;
    ChunkMeshletGenSystem chunkMeshletGenSystem = null;

    @Override
    public void update() {
        if (rebuildWorld) {
            rebuildWorld = false;
            for (CleanEntityHandle handle : chunkHandles.values()) {
                handle.tryDestroy();
            }
            chunkHandles.clear();
            for (Long chunkKey : chunkProvider.getLoadedChunks().keySet()) {
                for (int i = 0; i < 16; i++) {
                    ChunkComponent chunkComponent = new ChunkComponent();
                    chunkComponent.chunkPosX = ChunkPos.getX(chunkKey);
                    chunkComponent.chunkPosY = i;
                    chunkComponent.chunkPosZ = ChunkPos.getZ(chunkKey);
                    chunkHandles.put(new ChunkPosKey(chunkComponent.chunkPosX, chunkComponent.chunkPosY, chunkComponent.chunkPosZ), entityManager.createEntity(CHUNK_DESTROY_CALLBACK, chunkComponent));
                }
            }
            // all changes are buffered and will be consumed at the end of this update
        }

        // temp
        if (chunkPrioritizationSystem == null) {
            chunkPrioritizationSystem = new ChunkPrioritizationSystem(camera);
        }
        chunkPrioritizationSystem.update(entityManager, jobScheduler);

        if (chunkMeshletGenSystem == null) {
            chunkMeshletGenSystem = new ChunkMeshletGenSystem(chunkProvider, gizmosManager);
        }
        chunkMeshletGenSystem.setPriority(0);
        chunkMeshletGenSystem.update(entityManager, jobScheduler);

        super.update();
    }
}
