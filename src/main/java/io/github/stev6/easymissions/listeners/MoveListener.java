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

package io.github.stev6.easymissions.listeners;

import io.github.stev6.easymissions.EasyMissions;
import io.github.stev6.easymissions.caches.antiexploit.RecentStepCache;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.function.Predicate;

public record MoveListener(RecentStepCache c, EasyMissions plugin) implements Listener {

    private void handleMove(PlayerMoveEvent e, String type, Predicate<Player> condition) {
        if (e.getFrom().getBlockX() == e.getTo().getBlockX() && e.getFrom().getBlockZ() == e.getTo().getBlockZ())
            return;
        Player p = e.getPlayer();
        if (!condition.test(p)) return;
        var mainConfig = plugin.getConfigManager().getMainConfig();
        Block b = e.getTo().getBlock();
        if (mainConfig.antiAbuse().recentBlockStepCache() && c.isRecentBlock(b, p.getUniqueId())) return;

        c.add(b, p.getUniqueId());
        RecentStepCache.WalkCache data = c.getWalkData(p.getUniqueId());
        if (++data.blocksWalked >= mainConfig.mission().updateWalk()) {
            plugin.getMissionManager().findAndModifyFirstMission(p, type, mission -> mission.incrementProgress(data.blocksWalked));
            data.blocksWalked = 0;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWalk(PlayerMoveEvent e) {
        handleMove(e, "walk", p -> !p.isGliding() && !p.isSwimming() && !p.isFlying() && !p.isRiptiding());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSwim(PlayerMoveEvent e) {
        handleMove(e, "swim", p -> p.isSwimming() && !p.isFlying());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGlide(PlayerMoveEvent e) {
        handleMove(e, "glide", Player::isGliding);
    }
}