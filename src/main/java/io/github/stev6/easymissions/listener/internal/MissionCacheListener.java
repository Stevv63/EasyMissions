/*
 * EasyMissions – A Minecraft missions plugin.
 * Copyright (C) 2026 Stev6
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

package io.github.stev6.easymissions.listener.internal;

import io.github.stev6.easymissions.MissionManager;
import io.github.stev6.easymissions.cache.PlayerMissionCache;
import io.github.stev6.easymissions.config.ConfigManager;
import io.github.stev6.easymissions.config.data.MissionConfig;
import io.github.stev6.easymissions.mission.Mission;
import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.TreeMap;
import java.util.UUID;

import static io.github.stev6.easymissions.cache.PlayerMissionCache.IGNORED_SLOTS;

public record MissionCacheListener(PlayerMissionCache c, MissionManager manager, ConfigManager configManager) implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleJoin(PlayerJoinEvent e) {
        if (!configManager.getMainConfig().mission().cacheSlots()) return;
        c.handlePlayer(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleLeave(PlayerQuitEvent e) {
        if (!configManager.getMainConfig().mission().cacheSlots()) return;
        c.getCache().invalidate(e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleChange(PlayerInventorySlotChangeEvent e) {
        if (!configManager.getMainConfig().mission().cacheSlots()) return;
        int slot = e.getSlot();
        if (IGNORED_SLOTS.contains(slot)) return;
        UUID uuid = e.getPlayer().getUniqueId();
        ItemStack oldItem = e.getOldItemStack();
        ItemStack newItem = e.getNewItemStack();
        var cached = c.getCache().getIfPresent(uuid);

        boolean isModified = false;
        if (cached == null) cached = new TreeMap<>();

        if (manager.isMission(oldItem)) isModified = cached.remove(slot) != null;

        Mission m = manager.getMissionOrNull(newItem);
        if (m != null) {
            MissionConfig c = manager.getMissionConfigOrNull(m);
            if (c != null) {
                cached.put(slot, c);
                isModified = true;
            } else manager.handleBrokenMission(newItem, m.getConfigID());
        }

        if (!isModified) return;
        if (cached.isEmpty()) c.getCache().invalidate(uuid);
        else c.getCache().put(uuid, cached);
    }
}
