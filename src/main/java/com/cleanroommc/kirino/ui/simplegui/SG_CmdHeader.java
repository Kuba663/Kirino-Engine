package com.cleanroommc.kirino.ui.simplegui;

final class SG_CmdHeader {

    static final int HEADER_SIZE = 16; // 4 ints
    static final int TAIL_SIZE = 8; // int + float: layer + depth

    static final int OP = 0;     // int
    static final int FLAGS = 4;  // int
    static final int SIZE = 8;   // int. full command size
    static final int USED = 12;  // int. used bytes inside allocated command
}
