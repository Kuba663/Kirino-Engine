package com.cleanroommc.kirino.simpletext.text;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;

public class CodepointIterator implements PrimitiveIterator.OfInt {

    private final String text;
    private int index;

    public CodepointIterator(@NonNull String text) {
        Preconditions.checkNotNull(text);

        this.text = text;
        this.index = 0;
    }

    @Override
    public boolean hasNext() {
        return index < text.length();
    }

    @Override
    public int nextInt() {
        if (!hasNext()) {
            throw new NoSuchElementException("No remaining codepoints.");
        }

        int codepoint = text.codePointAt(index);

        index += Character.charCount(codepoint);

        return codepoint;
    }

    public int getIndex() {
        return index;
    }

    public void reset() {
        index = 0;
    }
}
