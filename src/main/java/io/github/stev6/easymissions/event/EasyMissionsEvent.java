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
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import io.github.stev6.easymissions.mission.Mission;

public abstract class EasyMissionsEvent extends PlayerEvent {
    protected final ItemStack missionItem;
    protected final Mission mission;

    public EasyMissionsEvent(@NotNull Player player, Mission mission, ItemStack missionItem) {
        super(player);
        this.missionItem = missionItem;
        this.mission = mission;
    }

    @NotNull
    public ItemStack getMissionItem() {
        return this.missionItem;
    }

    @NotNull
    public Mission getMission() {
        return this.mission;
    }
}
