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
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    public static void withContext(String context, Runnable action) {
        try {
            action.run();
        } catch (ConfigException e) {
            throw e.addContext(context);
        } catch (Exception e) {
            throw new ConfigException(e.getMessage(), e).addContext(context);
        }
    }

    public void handleException(Logger logger, boolean stackTrace, String pluginVersion) {
        final List<String> ERROR_HEADER = List.of(
                "=======================================",
                "         EasyMissions CONFIG ERROR     ",
                "         Plugin version: " + pluginVersion,
                "======================================="
        );

        ERROR_HEADER.forEach(logger::severe);
        this.getFormattedMessage().lines().forEach(logger::severe);
        logger.severe("=======================================");
        if (stackTrace) logger.log(Level.SEVERE, "Stack trace:", this);
        else logger.severe("Enable debug mode to see the stack trace.");
    }

    public static <T> T withContext(String context, Callable<T> action) {
        try {
            return action.call();
        } catch (ConfigException e) {
            throw e.addContext(context);
        } catch (Exception e) {
            throw new ConfigException(e.getMessage(), e).addContext(context);
        }
    }
}
