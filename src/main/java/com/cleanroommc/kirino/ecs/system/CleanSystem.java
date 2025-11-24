package com.cleanroommc.kirino.ecs.system;

import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.job.JobScheduler;
import org.jspecify.annotations.NonNull;

public abstract class CleanSystem {
    protected final ExecutionContainer execution = new ExecutionContainer();

    /**
     * This update is guaranteed to be synchronized, but you can start async tasks here.
     * <hr>
     * <p><code>execution.updateExecutions</code> should be called during this update so we can keep track of the async tasks started here.</p>
     * <code>execution.updateExecutions</code> assigns the async futures to this update;
     * {@link ExecutionContainer#noExecutions()} will be executed before the update by default, which assumes that you didn't start any async task.
     *
     * @param entityManager The entity manager
     * @param jobScheduler The job scheduler
     */
    public abstract void update(@NonNull EntityManager entityManager, @NonNull JobScheduler jobScheduler);
}
