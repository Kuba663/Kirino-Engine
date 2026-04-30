package com.cleanroommc.kirino.utils;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

public final class ForkJoinPoolUtils {

    private ForkJoinPoolUtils() {
    }

    @NonNull
    public static ForkJoinPool newWorkStealingPool() {
        int parallelism = Runtime.getRuntime().availableProcessors();
        return new ForkJoinPool(
                parallelism,
                ForkJoinPool.defaultForkJoinWorkerThreadFactory,
                null,
                true);
    }

    public static void shutdownPool(@NonNull ForkJoinPool pool) {
        Preconditions.checkNotNull(pool);

        pool.shutdown();

        try {
            if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
