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
import io.github.stev6.easymissions.context.impl.EntityContext;
import io.github.stev6.easymissions.context.impl.EntityDamageContext;
import io.github.stev6.easymissions.context.impl.EntityKillContext;
import io.github.stev6.easymissions.type.impl.*;
import org.bukkit.Material;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Goat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;

public record EntityListener(MissionManager m) implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onKill(EntityDeathEvent e) {
        if (e.getDamageSource().getCausingEntity() instanceof Player p) {
            var ctx = new EntityKillContext(e.getEntity(), p.getInventory().getItemInMainHand());
            m.findAndModifyFirstMission(p, Kill.INSTANCE, ctx, mission -> mission.incrementProgress(1));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player p)
            m.findAndModifyFirstMission(p, Damage.INSTANCE, new EntityDamageContext(e.getEntity(), p.getInventory().getItemInMainHand()), mission -> mission.incrementProgress(Math.round(((float) e.getFinalDamage()))));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTame(EntityTameEvent e) {
        if (e.getOwner() instanceof Player p)
            m.findAndModifyFirstMission(p, Tame.INSTANCE, new EntityContext(e.getEntity()), mission -> mission.incrementProgress(1));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMilk(PlayerInteractEntityEvent e) {
        if (e.getRightClicked() instanceof Cow || e.getRightClicked() instanceof Goat)
            if (e.getPlayer().getInventory().getItemInMainHand().getType() == Material.BUCKET)
                m.findAndModifyFirstMission(e.getPlayer(), Milk.INSTANCE, new EntityContext(e.getRightClicked()), mission -> mission.incrementProgress(1));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onShear(PlayerShearEntityEvent e) {
        m.findAndModifyFirstMission(e.getPlayer(), Shear.INSTANCE, new EntityContext(e.getEntity()), mission -> mission.incrementProgress(1));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreed(EntityBreedEvent e) {
        if (e.getBreeder() instanceof Player p)
            m.findAndModifyFirstMission(p, Breed.INSTANCE, new EntityContext(e.getEntity()), mission -> mission.incrementProgress(1));
    }
}