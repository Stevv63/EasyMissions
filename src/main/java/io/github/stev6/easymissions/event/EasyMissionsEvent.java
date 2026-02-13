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

import io.github.stev6.easymissions.EasyMissionsAPI;
import io.github.stev6.easymissions.mission.Mission;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract event for EasyMissions events
 */
public abstract class EasyMissionsEvent extends PlayerEvent {
    protected final ItemStack missionItem;
    protected final Mission mission;

    public EasyMissionsEvent(@NotNull Player player, Mission mission, ItemStack missionItem) {
        super(player);
        this.missionItem = missionItem;
        this.mission = mission;
    }

    /**
     * @return The {@link ItemStack} of the mission item
     */
    @NotNull
    public ItemStack getMissionItem() {
        return this.missionItem;
    }

    /**
     * Gets the {@link Mission} object in the event, changes made to this object will not be reflected on the actual mission,
     * to save your changes use the {@link EasyMissionsAPI#updateMissionData(ItemStack, Mission, boolean)} method on the mission item
     * @return the {@link Mission} involved in the event
     */
    @NotNull
    public Mission getMission() {
        return this.mission;
    }
}
