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

package io.github.stev6.easymissions.listener;

import io.github.stev6.easymissions.MissionManager;
import io.github.stev6.easymissions.mission.Mission;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public record GiveRewardsListener(MissionManager m) implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onUseMission(PlayerInteractEvent e) {
        ItemStack i = e.getItem();
        if (i == null) return;
        Mission mission = m.getMissionOrNull(i);
        if (mission == null || !mission.isCompleted()) return;
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            m.giveRewards(i, e.getPlayer());
            e.setCancelled(true);
        }
    }
}
