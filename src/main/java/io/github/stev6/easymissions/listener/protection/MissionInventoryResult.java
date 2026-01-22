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

package io.github.stev6.easymissions.listener.protection;

import com.destroystokyo.paper.event.inventory.PrepareResultEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import io.github.stev6.easymissions.MissionManager;

import java.util.Arrays;

public record MissionInventoryResult(MissionManager m) implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCraftResult(PrepareItemCraftEvent e) {
        boolean containsMissionItem = Arrays.stream(e.getInventory().getMatrix()).anyMatch(i -> i != null && m.getMissionOrNull(i) != null);
        if (containsMissionItem) e.getInventory().setResult(null);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onResult(PrepareResultEvent e) {
        boolean containsMissionItem = Arrays.stream(e.getInventory().getContents()).anyMatch(i -> i != null && m.getMissionOrNull(i) != null);
        if (containsMissionItem) e.setResult(null);
    }
}
