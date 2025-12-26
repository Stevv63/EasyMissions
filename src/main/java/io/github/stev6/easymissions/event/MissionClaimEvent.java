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

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import io.github.stev6.easymissions.mission.Mission;

import java.util.List;

public class MissionClaimEvent extends EasyMissionsEvent implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final @NotNull List<String> commands;
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

    public @NotNull List<String> getCommands() {
        return commands;
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
