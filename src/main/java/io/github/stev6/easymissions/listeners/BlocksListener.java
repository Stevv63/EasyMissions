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

import io.github.stev6.easymissions.MissionManager;
import io.github.stev6.easymissions.caches.antiexploit.RecentPlaceCache;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.HashSet;
import java.util.Set;

import static io.github.stev6.easymissions.util.ListenerUtils.*;

public record BlocksListener(MissionManager m, RecentPlaceCache c) implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        Block b = e.getBlock();
        if (c != null && c.isRecentBlock(b)) return;
        int amount;
        if (isChorus(b)) {
            HashSet<Block> set = new HashSet<>();
            getChorusBlocksBrokenByPhysics(b, set);
            amount = set.size();
        } else amount = 1;

        m.findAndModifyFirstMission(e.getPlayer(), "break", b.getType().name(), mission -> mission.incrementProgress(amount));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        if (c != null) c.add(e.getBlock());
        m.findAndModifyFirstMission(e.getPlayer(), "place", e.getBlock().getType().name(), mission -> mission.incrementProgress(1));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHarvest(BlockBreakEvent e) {
        Block b = e.getBlock();
        boolean isAnnoying = isAnnoyingAgeAbleBlock(b);
        if (c != null && c.isRecentBlock(b)) return;
        if (!(b.getBlockData() instanceof Ageable ageable)) return;
        if (!isAnnoying && ageable.getAge() != ageable.getMaximumAge()) return;
        int amount = isAnnoying ? getBlocksBrokenByPhysics(b, false) : 1;

        m.findAndModifyFirstMission(e.getPlayer(), "harvest", b.getType().name(), mission -> mission.incrementProgress(amount));
    }

    private int getBlocksBrokenByPhysics(Block b, boolean d) {
        Material targetType = b.getType();
        int ceiling = switch (targetType) {
            case SUGAR_CANE, CACTUS -> SUGARCANE_CACTUS_MAX;
            case BAMBOO -> BAMBOO_MAX;
            default -> 1;
        };

        int broken = 1;
        Block current = b;
        for (int i = 1; i < ceiling; i++) {
            current = current.getRelative(d ? BlockFace.DOWN : BlockFace.UP);
            if (current.getType() != targetType) break;
            if (c == null || !c.isRecentBlock(current)) broken++;
        }

        return broken;
    }

    // logic taken from mcMMO, thank you!
    public void getChorusBlocksBrokenByPhysics(Block b, Set<Block> blocks) {
        if (!isChorus(b)) return;
        if (c != null && c.isRecentBlock(b)) return;
        if (blocks.size() >= CHORUS_MAX) return;
        if (!blocks.add(b)) return;
        for (var face : new BlockFace[]{BlockFace.UP, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST})
            getChorusBlocksBrokenByPhysics(b.getRelative(face), blocks);
    }

}