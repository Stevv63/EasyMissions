/*
 * EasyMissions – A Minecraft missions plugin.
 * Copyright (C) 2025 Stev6
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

import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;

@ApiStatus.Internal // you wouldn't want to use this yourself anyway...
public final class MatchWildCard {
    public static boolean matchesTarget(String pattern, String text) {
        if (pattern.equals("*")) return true;
        int index1 = pattern.indexOf('*');
        if (index1 < 0) return text.equals(pattern);
        int index2 = pattern.lastIndexOf('*');
        if (index1 == 0 && index2 == pattern.length() - 1 && index1 != index2)
            return text.contains(pattern.substring(1, index2));

        String pre = pattern.substring(0, index1);
        String suf = pattern.substring(index1 + 1);

        if (text.length() < pre.length() + suf.length()) {
            return false;
        }

        return text.startsWith(pre) && text.endsWith(suf);
    }

    public static boolean wildCardCheck(Collection<String> config, String target) {
        for (String pattern : config) if (matchesTarget(pattern, target)) return true;
        return false;
    }

}
