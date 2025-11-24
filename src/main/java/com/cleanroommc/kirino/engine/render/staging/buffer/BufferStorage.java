package com.cleanroommc.kirino.engine.render.staging.buffer;

import com.cleanroommc.kirino.engine.render.staging.StagingBufferManager;

/**
 * <p>A buffer storage consists of several pages (a page is an individual coherent-persistently-mapped buffer).</p>
 * <p>Every page is huge in size (~10MB), and a new page will be allocated when this buffer storage is full.
 * Every page will be split into slots dynamically, and slots can be freed to upload new data.</p>
 *
 * <p><code>BufferStorage</code> is only utilized by {@link StagingBufferManager}.</p>
 *
 * @see StagingBufferManager
 */
public class BufferStorage {
    private final static int PAGE_SIZE_IN_BYTE = 1024 * 1024 * 10; // 10MB
}
