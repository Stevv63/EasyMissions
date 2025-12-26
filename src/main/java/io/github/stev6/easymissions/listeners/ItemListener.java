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

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import io.github.stev6.easymissions.MissionManager;
import io.github.stev6.easymissions.util.ListenerUtils;
import io.papermc.paper.event.player.PlayerTradeEvent;
import org.bukkit.entity.Item;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionType;

public record ItemListener(MissionManager m) implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFish(PlayerFishEvent e) {
        if (e.getState() == PlayerFishEvent.State.CAUGHT_FISH && e.getCaught() instanceof Item item) {
            m.findAndModifyFirstMission(e.getPlayer(), "fish", item.getItemStack().getType().name(),
                    mission -> mission.incrementProgress(1));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemConsume(PlayerItemConsumeEvent e) {
        PotionType type = ListenerUtils.getPotionTypeOrNull(e.getItem());
        if (type != null)
            m.findAndModifyFirstMission(e.getPlayer(), "potion", type.name(), mission -> mission.incrementProgress(1));

        m.findAndModifyFirstMission(e.getPlayer(), "consume", e.getItem().getType().name(), mission -> mission.incrementProgress(1));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPotionThrow(PlayerLaunchProjectileEvent e) {
        if (!(e.getProjectile() instanceof ThrownPotion pot)) return;
        PotionType type = ListenerUtils.getPotionTypeOrNull(pot.getItem());
        if (type != null)
            m.findAndModifyFirstMission(e.getPlayer(), "potion", type.name(), mission -> mission.incrementProgress(1));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEnchant(EnchantItemEvent e) {
        e.getEnchantsToAdd().forEach(((ench, level) -> ListenerUtils.incrementForEnchant(m, e.getEnchanter(), e.getItem().getType(), ench, level)));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSmelt(FurnaceExtractEvent e) {
        m.findAndModifyFirstMission(e.getPlayer(), "smelt", e.getItemType().name(), mission -> mission.incrementProgress(e.getItemAmount()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTrade(PlayerTradeEvent e) {
        m.findAndModifyFirstMission(e.getPlayer(), "trade", mission -> mission.incrementProgress(1));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onXP(PlayerExpChangeEvent e) {
        if (e.getAmount() > 0) {
            m.findAndModifyFirstMission(e.getPlayer(), "xp", mission -> mission.incrementProgress(e.getAmount()));
        }
    }
}