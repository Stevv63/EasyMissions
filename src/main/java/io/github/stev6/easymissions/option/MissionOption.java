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

package io.github.stev6.easymissions.option;

import io.github.stev6.easymissions.EasyMissionsAPI;
import io.github.stev6.easymissions.MissionManager;
import io.github.stev6.easymissions.context.Locatable;
import io.github.stev6.easymissions.context.MissionContext;
import io.github.stev6.easymissions.mission.Mission;
import io.github.stev6.easymissions.option.impl.PermissionOption;
import io.github.stev6.easymissions.registry.MissionOptionRegistry;
import io.github.stev6.easymissions.type.MissionType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A condition that must be met for a mission to progress, such as having a specific permission or being in a certain WorldGuard region.
 * <p>
 * Options are checked in the {@link MissionManager#findAndModifyFirstMission(Player, MissionType, MissionContext, Consumer)} method.
 * <p>
 * You must register your option using the {@link MissionOptionRegistry}
 * using the {@link EasyMissionsAPI#registerOption(String, Function)} for it to work
 * <p>
 *
 * @see EasyMissionsAPI#registerOption(String, Function)
 * @see PermissionOption An example of an option that checks if a player has a specific permission.
 */
public interface MissionOption {

    /**
     * Checks if this condition is met. To use location handling, the mission context may be an instance of {@link Locatable}, you may then cast to that and get the location.
     * This is useful for precise location cases since things like block placement or break will give incorrect locations if you check the player's location.
     * <p>
     * @param player the player the check is for
     * @param mission the mission object on the mission item
     * @param item the mission item
     * @param context the passed mission context, may or may not be an instance of {@link Locatable}
     * @return whether this condition is met or not.
     */
    boolean check(@NotNull Player player, @NotNull Mission mission, @NotNull ItemStack item, @Nullable MissionContext context);
}
