/*
 * EasyMissions – A Minecraft missions plugin.
 * Copyright (C) 2026 Stev6
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
