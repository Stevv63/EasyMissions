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
import io.github.stev6.easymissions.cache.BrewCache;
import io.github.stev6.easymissions.context.impl.ItemContext;
import io.github.stev6.easymissions.type.impl.Brew;
import io.github.stev6.easymissions.util.ListenerUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionType;

public record BrewListener(MissionManager m, BrewCache c) implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBrew(BrewEvent e) {
        Player p = c.getPlayerAssociated(e.getBlock());
        if (p == null) return;

        for (int i = 0; i < e.getResults().size(); i++) {

            ItemStack newItem = e.getResults().get(i);
            PotionType newType = ListenerUtils.getPotionTypeOrNull(newItem);
            if (newType == null) continue;

            ItemStack oldItem = e.getContents().getItem(i);
            PotionType oldType = ListenerUtils.getPotionTypeOrNull(oldItem);
            if (oldType != null && oldType == newType) continue; // skip if no PotionType change occurred

            m.findAndModifyFirstMission(p, Brew.INSTANCE, new ItemContext(newItem), mission -> mission.incrementProgress(1));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBrewerInteract(InventoryClickEvent e) {
        if (e.getInventory() instanceof BrewerInventory inv) {
            if (!(e.getWhoClicked() instanceof Player p)) return;
            if (inv.getLocation() == null) return;

            boolean clickedTop = e.getClickedInventory() == inv;
            boolean shiftInto = e.isShiftClick() && e.getClickedInventory() instanceof PlayerInventory;

            if (clickedTop || shiftInto) c.add(inv.getLocation().getBlock(), p);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBrewerDrag(InventoryDragEvent e) {
        if (e.getInventory() instanceof BrewerInventory inv) {
            if (!(e.getWhoClicked() instanceof Player p)) return;
            if (inv.getLocation() == null) return;
            for (int slot : e.getRawSlots())
                if (slot < inv.getSize()) {
                    c.add(inv.getLocation().getBlock(), p);
                    return;
                }
        }
    }
}
