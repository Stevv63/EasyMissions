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

import io.github.stev6.easymissions.EasyMissions;
import io.github.stev6.easymissions.cache.PlayerMissionCache;
import io.github.stev6.easymissions.cache.antiexploit.RecentStepCache;
import io.github.stev6.easymissions.type.MissionType;
import io.github.stev6.easymissions.type.impl.SimpleTypes;
import io.github.stev6.easymissions.util.BlockPos;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.function.Predicate;

public record MoveListener(RecentStepCache stepCache, PlayerMissionCache missionCache,
                           EasyMissions plugin) implements Listener {

    private void handleMove(PlayerMoveEvent e, MissionType type, Predicate<Player> condition) {
        Location from = e.getFrom();
        Location to = e.getTo();
        if (from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ()) return;

        Player p = e.getPlayer();

        if (!missionCache.playerHasAnyMission(p)) return;

        if (!condition.test(p)) return;
        var mainConfig = plugin.getConfigManager().getMainConfig();
        BlockPos pos = new BlockPos(
                to.getWorld().getUID(),
                to.getBlockX(),
                to.getBlockY(),
                to.getBlockZ()
        );
        if (mainConfig.antiAbuse().recentBlockStepCache() && stepCache.isRecentBlock(pos, p.getUniqueId())) return;

        stepCache.add(pos, p.getUniqueId());
        RecentStepCache.WalkCache walkData = stepCache.getWalkData(p.getUniqueId());
        if (++walkData.blocksWalked >= mainConfig.mission().updateWalk()) {
            plugin.getMissionManager().findAndModifyFirstMission(p, type, mission -> mission.incrementProgress(walkData.blocksWalked));
            walkData.blocksWalked = 0;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWalk(PlayerMoveEvent e) {
        handleMove(e, SimpleTypes.WALK, p -> !p.isGliding() && !p.isSwimming() && !p.isFlying() && !p.isRiptiding());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSwim(PlayerMoveEvent e) {
        handleMove(e, SimpleTypes.SWIM, p -> p.isSwimming() && !p.isFlying());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGlide(PlayerMoveEvent e) {
        handleMove(e, SimpleTypes.GLIDE, Player::isGliding);
    }
}