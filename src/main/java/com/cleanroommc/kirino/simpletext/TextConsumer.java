package com.cleanroommc.kirino.simpletext;

import com.cleanroommc.kirino.simpletext.command.TextCommandList;

public interface TextConsumer {
    void consume(TextCommandList commandList);
}
