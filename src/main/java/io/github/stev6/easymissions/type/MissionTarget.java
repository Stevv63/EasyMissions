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

package io.github.stev6.easymissions.type;

import io.github.stev6.easymissions.config.data.MissionConfig;
import io.github.stev6.easymissions.context.MissionContext;

/**
 * Represents the <b>data</b> for a specific {@link MissionConfig}.
 * <p>
 * This object is created <b>when</b> a mission config is being parsed. It holds the requirements for it
 * (e.g., "Must break DIAMOND_ORE") to check against the context passed from an event.
 *
 * <h3>Example Implementation</h3>
 * <pre>{@code
 * public record BreakData(Set<Material> allowedMaterials) implements MissionTarget<BlockContext> {
 *
 *     @Override
 *     public boolean matches(BlockContext context) {
 *         // Check if the block broken (context) matches the config (allowedMaterials)
 *         return allowedMaterials.contains(context.block().getType());
 *     }
 * }
 * }</pre>
 * @param <C> The {@link MissionContext} this target validates.
 */
public interface MissionTarget<C extends MissionContext> {
    boolean matches(C context);
}
