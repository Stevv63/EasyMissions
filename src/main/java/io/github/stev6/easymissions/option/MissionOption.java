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
    boolean check(@NotNull Player player, @NotNull Mission mission, @NotNull ItemStack item, @NotNull MissionContext context);
}
