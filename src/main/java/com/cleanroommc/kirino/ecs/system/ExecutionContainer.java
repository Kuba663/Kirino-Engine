package com.cleanroommc.kirino.ecs.system;

import com.cleanroommc.kirino.ecs.job.JobScheduler;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ExecutionContainer {
    ExecutionContainer() {
    }

    private final List<JobScheduler.ExecutionHandle> handles = new ArrayList<>();
    private final List<CompletableFuture<?>> futures = new ArrayList<>();

    public void noExecutions() {
        updateExecutions(null, (JobScheduler.ExecutionHandle[]) null);
    }

    @SuppressWarnings("all")
    public void updateExecutions(@NonNull JobScheduler.ExecutionHandle @NonNull ... handles) {
        updateExecutions(null, handles);
    }

    @SuppressWarnings("all")
    public void updateExecutions(@NonNull CompletableFuture<?> @Nullable [] futures, @NonNull JobScheduler.ExecutionHandle @Nullable ... handles) {
        this.handles.clear();
        this.futures.clear();
        if (futures != null) {
            this.futures.addAll(Arrays.asList(futures));
        }
        if (handles != null) {
            this.handles.addAll(Arrays.asList(handles));
        }
    }
}
