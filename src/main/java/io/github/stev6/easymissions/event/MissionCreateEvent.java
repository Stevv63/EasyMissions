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

package io.github.stev6.easymissions.event;

import io.github.stev6.easymissions.config.data.MissionConfig;
import io.github.stev6.easymissions.mission.Mission;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Fired whenever a mission is created by plugin commands, does not fire for API creation
 */
public class MissionCreateEvent extends EasyMissionsEvent implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Source source;
    private final MissionConfig missionConfig;
    private boolean isCancelled;

    public MissionCreateEvent(@NotNull Player player, @NotNull ItemStack missionItem, @NotNull Mission mission, @NotNull MissionConfig missionConfig, @NotNull Source source) {
        super(player, mission, missionItem);
        this.missionConfig = missionConfig;
        this.source = source;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    /**
     * The {@link Source} of the mission, tells you whether it is from a give or random or category random command
     * @return The {@link Source} involved with the event
     */
    public Source getSource() {
        return source;
    }

    /**
     * @return The {@link MissionConfig} the mission created belongs to
     */
    public MissionConfig getMissionConfig() {
        return missionConfig;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    /**
     * Possible sources that give a player a mission
     */
    public enum Source {
        /**
         * Source for the {@code /easymissions give} command
         */
        GIVE,

        /**
         * Source for the {@code /easymissions random} command
         */
        RANDOM,

        /**
         * Source for the {@code /easymissions category-random} command
         */
        RANDOM_CATEGORY
    }
}
