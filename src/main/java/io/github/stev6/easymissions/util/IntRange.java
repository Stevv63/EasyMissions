package io.github.stev6.easymissions.util;

import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

public record IntRange(int min, int max) {
    public static final IntRange ANY_RANGE = IntRange.of(0, Integer.MAX_VALUE);
    public static final String SPLITTER = "-";

    public IntRange {
        if (max < min)
            throw new IllegalArgumentException("Tried creating range with illegal arguments, min cannot be greater than max");
    }

    public static IntRange fromString(String range) {
        if (range.endsWith("+") && range.length() > 1)
            return of(Integer.parseInt(range.substring(0, range.length() - 1).trim()), Integer.MAX_VALUE);
        else if (range.startsWith("-") && range.length() > 1)
            return of(0, Integer.parseInt(range.substring(1).trim()));

        String[] split = range.split(Pattern.quote(SPLITTER), 2);
        int min = Integer.parseInt(split[0].trim());
        if (split.length == 1) return of(min, min);
        int max = Integer.parseInt(split[1].trim());
        return of(min, max);
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
