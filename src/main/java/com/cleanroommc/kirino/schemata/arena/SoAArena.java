package com.cleanroommc.kirino.schemata.arena;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class SoAArena {

    interface Column {
        void grow(int newCapacity);
    }

    private final List<Column> columns = new ArrayList<>();

    private int size = 0;
    private int capacity;

    public SoAArena(int initialCapacity) {
        this.capacity = initialCapacity;
    }

    public int alloc() {
        if (size >= capacity) {
            grow();
        }

        return size++;
    }

    public int size() {
        return size;
    }

    public int capacity() {
        return capacity;
    }

    public void reset() {
        size = 0;
    }

    @NonNull
    public FloatColumn floatColumn() {
        FloatColumn c = new FloatColumn(capacity);
        columns.add(c);
        return c;
    }

    @NonNull
    public IntColumn intColumn() {
        IntColumn c = new IntColumn(capacity);
        columns.add(c);
        return c;
    }

    @NonNull
    public LongColumn longColumn() {
        LongColumn c = new LongColumn(capacity);
        columns.add(c);
        return c;
    }

    @NonNull
    public BoolColumn boolColumn() {
        BoolColumn c = new BoolColumn(capacity);
        columns.add(c);
        return c;
    }

    private void grow() {
        int newCapacity = capacity << 1;

        for (Column c : columns) {
            c.grow(newCapacity);
        }

        capacity = newCapacity;
    }

    public static final class FloatColumn implements Column {

        private float[] data;

        FloatColumn(int capacity) {
            data = new float[capacity];
        }

        public float get(int index) {
            Preconditions.checkElementIndex(index, data.length);

            return data[index];
        }

        public void set(int index, float value) {
            Preconditions.checkElementIndex(index, data.length);

            data[index] = value;
        }

        @Override
        public void grow(int newCapacity) {
            data = Arrays.copyOf(data, newCapacity);
        }
    }

    public static final class IntColumn implements Column {

        private int[] data;

        IntColumn(int capacity) {
            data = new int[capacity];
        }

        public int get(int index) {
            Preconditions.checkElementIndex(index, data.length);

            return data[index];
        }

        public void set(int index, int value) {
            Preconditions.checkElementIndex(index, data.length);

            data[index] = value;
        }

        @Override
        public void grow(int newCapacity) {
            data = Arrays.copyOf(data, newCapacity);
        }
    }

    public static final class LongColumn implements Column {

        private long[] data;

        LongColumn(int capacity) {
            data = new long[capacity];
        }

        public long get(int index) {
            Preconditions.checkElementIndex(index, data.length);

            return data[index];
        }

        public void set(int index, long value) {
            Preconditions.checkElementIndex(index, data.length);

            data[index] = value;
        }

        @Override
        public void grow(int newCapacity) {
            data = Arrays.copyOf(data, newCapacity);
        }
    }

    public static final class BoolColumn implements Column {

        private boolean[] data;

        BoolColumn(int capacity) {
            data = new boolean[capacity];
        }

        public boolean get(int index) {
            Preconditions.checkElementIndex(index, data.length);

            return data[index];
        }

        public void set(int index, boolean value) {
            Preconditions.checkElementIndex(index, data.length);

            data[index] = value;
        }

        @Override
        public void grow(int newCapacity) {
            data = Arrays.copyOf(data, newCapacity);
        }
    }
}
