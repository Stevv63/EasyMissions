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

package io.github.stev6.easymissions.event;

import io.github.stev6.easymissions.mission.Mission;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Fired whenever a {@link Mission} progresses
 */
public class MissionProgressEvent extends EasyMissionsEvent implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final int oldProgress;
    private int newProgress;
    private boolean isCancelled;

    public MissionProgressEvent(@NotNull Player player, Mission mission, ItemStack missionItem, int oldProgress, int newProgress) {
        super(player, mission, missionItem);
        this.oldProgress = oldProgress;
        this.newProgress = newProgress;
    }

    /**
     * @return The old progress on the mission
     */
    public int getOldProgress() {
        return oldProgress;
    }

    /**
     * @return The new progress that will be applied to the mission
     */
    public int getNewProgress() {
        return newProgress;
    }

    /**
     * Sets the new progress to be applied to the mission
     */
    public void setNewProgress(int newProgress) {
        this.newProgress = newProgress;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        isCancelled = cancel;
    }
}
