package io.github.stev6.easymissions.util;

import java.util.concurrent.ThreadLocalRandom;

public record IntRange(int min, int max) {
    public static final IntRange ANY_RANGE = IntRange.of(0, Integer.MAX_VALUE);
    public static final String SPLITTER = "-";

    public IntRange {
        if (max < min)
            throw new IllegalArgumentException("Tried creating range with illegal arguments, min cannot be greater than max");
    }

    public static IntRange fromString(String range) {
        String[] split = range.split(SPLITTER);
        int min = Integer.parseInt(split[0]);
        if (split.length == 1) return of(min,min);
        int max = Integer.parseInt(split[1]);
        return of(min,max);
    }

    public static IntRange of(int min, int max) {
        return new IntRange(Math.min(min, max), Math.max(min, max));
    }

    public int random() {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public boolean isWithin(int i) {
        return i >= min && i <= max;
    }
}
