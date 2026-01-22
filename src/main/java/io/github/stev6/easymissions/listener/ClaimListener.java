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

import io.github.stev6.easymissions.config.ConfigManager;
import io.github.stev6.easymissions.event.MissionClaimEvent;
import net.kyori.adventure.sound.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public record ClaimListener(ConfigManager configManager) implements Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMissionClaim(MissionClaimEvent e) {
        if (configManager.getMainConfig().mission().claimSound() != null) {
            e.getPlayer().playSound(Sound.sound()
                    .type(configManager.getMainConfig().mission().claimSound())
                    .pitch(configManager.getMainConfig().mission().claimPitch())
                    .volume(configManager.getMainConfig().mission().claimVolume())
                    .source(Sound.Source.MASTER)
                    .build());
        }
    }
}
