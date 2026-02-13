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

import java.util.List;

/**
 * Fired whenever a mission is claimed
 */
public class MissionClaimEvent extends EasyMissionsEvent implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private @NotNull List<String> commands;
    private boolean isCancelled;

    public MissionClaimEvent(
            @NotNull Player player,
            @NotNull Mission mission,
            @NotNull ItemStack missionItem,
            @NotNull List<String> commands) {
        super(player, mission, missionItem);
        this.commands = commands;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    /**
     * Returns an immutable list of the commands, if you would like to modify the commands
     * feel free to use {@link #setCommands(List)}
     * @return An immutable list containing the commands that will be run
     */
    public @NotNull List<String> getCommands() {
        return commands;
    }

    /**
     * Sets the commands to execute for this event, will overwrite the existing commands
     * @param commands Your list
     */
    public void setCommands(@NotNull List<String> commands) {
        this.commands = commands;
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
        this.isCancelled = cancel;
    }
}
