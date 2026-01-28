package io.github.stev6.easymissions.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.github.stev6.easymissions.EasyMissions;
import io.github.stev6.easymissions.MissionManager;
import io.github.stev6.easymissions.config.data.MissionConfig;
import io.github.stev6.easymissions.mission.Mission;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PlayerMissionCache {
    public static final Set<Integer> IGNORED_SLOTS = Set.of(36, 37, 38, 39);
    private final Cache<UUID, NavigableMap<Integer, MissionConfig>> cache;
    private final MissionManager manager;
    private final EasyMissions plugin;

    public PlayerMissionCache(EasyMissions plugin) {
        this.plugin = plugin;
        this.manager = plugin.getMissionManager();
        cache = CacheBuilder.newBuilder().build();
    }

    @NotNull
    public NavigableMap<Integer, MissionConfig> getCachedMissionsForPlayer(Player p) {
        var map = cache.getIfPresent(p.getUniqueId());
        return map == null ? Collections.emptyNavigableMap() : map;
    }

    @ApiStatus.Internal
    public Cache<UUID, NavigableMap<Integer, MissionConfig>> getCache() {
        return cache;
    }

    public boolean playerHasAnyMission(Player p) {
        var map = cache.getIfPresent(p.getUniqueId());
        return map != null && !map.isEmpty();
    }

    public void reloadCache(Collection<? extends Player> players) {
        cache.invalidateAll();

        if (!plugin.getConfigManager().getMainConfig().mission().cacheSlots()) return;

        for (Player p : players) {
            p.getScheduler().run(plugin, e -> handlePlayer(p), () -> {
            });
        }
    }

    public void handlePlayer(Player p) {
        var uuid = p.getUniqueId();
        var missions = manager.getMissionsInInventory(p.getInventory(), IGNORED_SLOTS);
        if (missions.isEmpty()) {
            cache.invalidate(uuid);
            return;
        }
        NavigableMap<Integer, MissionConfig> cached = new TreeMap<>();
        for (var e : missions.entrySet()) {
            int slot = e.getKey();
            Mission m = e.getValue();
            ItemStack i = p.getInventory().getItem(slot);
            if (i == null) continue;
            var mConfig = manager.getMissionConfigOrNull(m);
            if (mConfig == null) {
                manager.handleBrokenMission(i, m.getConfigID());
                continue;
            }
            cached.put(slot, mConfig);
        }
        cache.put(uuid, cached);
    }
}
