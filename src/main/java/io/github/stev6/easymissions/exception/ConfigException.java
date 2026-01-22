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

package io.github.stev6.easymissions.exception;

import java.util.ArrayList;
import java.util.List;

public class ConfigException extends RuntimeException {

    List<String> contexts = new ArrayList<>();

    public ConfigException(String message) {
        super(message);
    }

    public ConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigException addContext(String context) {
        contexts.add(context);
        return this;
    }

    public String getFormattedMessage() {
        StringBuilder sb = new StringBuilder();
        List<String> reversed = new ArrayList<>(contexts).reversed();

        for (String ctx : reversed) {
            sb.append("  -> ").append(ctx).append("\n");
        }
        sb.append("  Error: ").append(getMessage());
        return sb.toString();
    }
}
