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

import io.github.stev6.easymissions.EasyMissions;
import io.github.stev6.easymissions.config.data.MissionConfig;
import io.github.stev6.easymissions.context.MissionContext;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

/**
 * An interface for mission types that have config requirements (Targets)
 * and specific event data (Context) to determine progress.
 *
 * <p><b>Process:</b>
 * <ol>
 *     <li><b>Parsing:</b> On server load, {@link #parse(ConfigurationSection)} reads the config
 *     and creates a {@link MissionTarget} (D) to store the requirements. </li>
 *     <li><b>Triggering:</b> When an event is fired (e.g., BlockBreak), you create a {@link MissionContext} (C)
 *     and call the API.</li>
 *     <li><b>Matching:</b> The plugin receives the specific {@link MissionTarget} (D) for the player's mission
 *     and passes it alongside the {@link MissionContext} (C) to {@link #matches(MissionTarget, MissionContext)}.</li>
 * </ol>
 *
 * <p><b>Convention:</b>
 * <br>The generic type <b>C</b> must match the {@link MissionContext} implementation passed to
 * {@code EasyMissionsAPI.findAndProgressMission} when triggering this type.
 * If they do not match, the plugin's {@link #matchesRaw} will catch a {@link ClassCastException}
 * and the mission will not progress. You will receive a log error indicating that it doesn't match.
 *
 * <p>
 * There must be a singleton instance of your type to be used in registering and in events.
 *
 * <h3>Example implementation</h3>
 * <pre>{@code
 * public class BreakType implements TargetedMissionType<BlockContext, BreakData> {
 *     public static final BreakType INSTANCE = new BreakType();
 *
 *     @Override
 *     public String id() { return "break"; }
 *
 *     @Override
 *     public BreakData parse(ConfigurationSection section) {
 *         // Read list of allowable blocks from the config
 *         Set<Material> materials = ...;
 *         return new BreakData(materials); // This is just an example, usually for Enums youd check out the EnumMatcher
 *     }
 * }
 *
 * // The Data Record (D)
 * public record BreakData(Set<Material> allowed) implements MissionTarget<BlockContext> {
 *     @Override
 *     public boolean matches(BlockContext context) {
 *         // Check if the runtime block matches the config requirements
 *         return allowed.contains(context.block().getType());
 *     }
 * }
 * }</pre>
 *
 * <p>
 * You can find some real examples of real types in the plugin in the see also section below.
 * @param <C> The specific {@link MissionContext} implementation containing runtime event data (e.g., BlockContext, EntityKillContext).
 * @param <D> The specific {@link MissionTarget} implementation containing cached configuration data (e.g., allowed Materials, Matchers).
 * @see io.github.stev6.easymissions.type.impl.Break Break type
 * @see io.github.stev6.easymissions.type.impl.Consume Consume type
 * @see MissionType
 * @see MissionContext
 * @see MissionTarget
 */
public interface TargetedMissionType<C extends MissionContext, D extends MissionTarget<C>> extends MissionType {

    /**
     * Parses the section of a mission config into a data object.
     * <p>
     * This is called only once during config load (or reload). The final object is stored in a {@link MissionConfig}.
     *
     * @param section The configuration section from the YAML file to read requirements from.
     * @return An immutable data object containing the parsed requirements.
     */
    @NotNull D parse(@NotNull ConfigurationSection section);

    /**
     * Checks if the passed context matches the parsed configuration requirements.
     *
     * @param data    The parsed configuration data returned by {@link #parse(ConfigurationSection)}.
     * @param context The runtime context given.
     * @return true if the context satisfies the requirement, false otherwise.
     */
    default boolean matches(@NotNull D data, @NotNull C context) {
        return data.matches(context);
    }

    @SuppressWarnings("unchecked")
    default boolean matchesRaw(Object data, Object context) {
        try {
            return matches((D) data, (C) context);
        } catch (ClassCastException e) {
            EasyMissions.getInstance().getLogger().severe("Cast error: " + e.getMessage());
            EasyMissions.getInstance().getLogger().severe("For type: " + id());
            return false;
        }
    }
}
