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

package io.github.stev6.easymissions.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.github.stev6.easymissions.util.BlockPos;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BrewCache {
    private volatile Cache<BlockPos, UUID> brewingPlayerCache;

    public BrewCache(long timeout) {
        buildCache(timeout);
    }

    public void buildCache(long timeout) {
        Map<BlockPos, UUID> oldEntries = brewingPlayerCache != null ? new HashMap<>(brewingPlayerCache.asMap()) : Map.of();
        Cache<BlockPos, UUID> cache = CacheBuilder.newBuilder().expireAfterWrite(Duration.ofSeconds(timeout)).build();
        cache.putAll(oldEntries);
        brewingPlayerCache = cache;
    }

    public void add(Block b, Player p) {
        brewingPlayerCache.put(BlockPos.of(b), p.getUniqueId());
    }

    public Player getPlayerAssociated(Block b) {
        UUID p = brewingPlayerCache.getIfPresent(BlockPos.of(b));
        if (p == null) return null;
        return Bukkit.getPlayer(p);
    }
}
