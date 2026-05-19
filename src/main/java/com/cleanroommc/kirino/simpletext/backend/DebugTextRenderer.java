package com.cleanroommc.kirino.simpletext.backend;

import com.cleanroommc.kirino.simpletext.TextConsumer;
import com.cleanroommc.kirino.simpletext.TextContext;
import com.cleanroommc.kirino.simpletext.command.TextCommandList;

public class DebugTextRenderer implements TextConsumer {

    private final TextContext context;

    public DebugTextRenderer(TextContext context) {
        this.context = context;
    }

    @Override
    public void consume(TextCommandList commandList) {

    }
}
