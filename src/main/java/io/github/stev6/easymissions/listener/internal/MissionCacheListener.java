package io.github.stev6.easymissions.listener.internal;

import io.github.stev6.easymissions.MissionManager;
import io.github.stev6.easymissions.cache.PlayerMissionCache;
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

public record MissionCacheListener(PlayerMissionCache c, MissionManager manager) implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleJoin(PlayerJoinEvent e) {
        c.handlePlayer(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleLeave(PlayerQuitEvent e) {
        c.getCache().invalidate(e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleChange(PlayerInventorySlotChangeEvent e) {
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
                cached.put(slot, PlayerMissionCache.CachedMission.of(c));
                isModified = true;
            } else manager.handleBrokenMission(newItem, m.getConfigID());
        }

        if (!isModified) return;
        if (cached.isEmpty()) c.getCache().invalidate(uuid);
        else c.getCache().put(uuid, cached);
    }
}
