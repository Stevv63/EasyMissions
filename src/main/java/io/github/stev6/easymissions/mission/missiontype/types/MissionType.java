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

package io.github.stev6.easymissions.mission.missiontype.types;

import io.github.stev6.easymissions.EasyMissionsAPI;
import org.bukkit.Keyed;
import org.bukkit.Registry;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import io.github.stev6.easymissions.mission.missiontype.TargetedMissionType;
import io.github.stev6.easymissions.mission.missiontype.validator.TargetValidator;
import io.github.stev6.easymissions.mission.missiontype.validator.Validators;

import java.util.List;

/**
 * Main MissionType interface, contains most of the helpers you'll be using to create new mission types
 * <p>
 * Remember that type registration done after {@link org.bukkit.event.server.ServerLoadEvent} will throw an exception and not work
 * <p>
 * See {@link EasyMissionsAPI#registerType(MissionType...)}
 *
 * @see EasyMissionsAPI
 */
@FunctionalInterface
public interface MissionType {

    /**
     * A simple mission type, used for types that do not need a target like walking missions
     * <p>
     * Examples of usage:
     * <pre>{@code
     * EasyMissionsAPI api = EasyMissionsAPI.getInstance();
     *
     * api.registerType(MissionType.simple("some_id"));
     * }</pre>
     *
     * @param id the identifier for your mission type, you'll use this in your listeners and mission configurations will be verified against this
     * @return a {@link SimpleMissionType} instance
     */
    @Contract("_ -> new")
    static @NotNull MissionType simple(@NotNull String id) {
        return new SimpleMissionType(id);
    }

    /**
     * An {@link Enum} mission type, used for missions that validate against {@link Enum} targets
     * such as break missions or place which would validate against {@link org.bukkit.Material}
     * <p>
     * Examples of usage:
     * <pre>{@code
     * EasyMissionsAPI api = EasyMissionsAPI.getInstance();
     *
     * api.registerType(MissionType.matchEnum("some_id", EntityType.class));
     * }</pre>
     *
     * @param id        the identifier for your mission type, you'll use this in your listeners and mission configurations will be verified against this
     * @param enumClass the {@link Enum} you want to match against
     * @return a {@link EnumMissionType} instance
     */
    @Contract("_, _ -> new")
    static <E extends Enum<E>> @NotNull TargetedMissionType matchEnum(@NotNull String id, @NotNull Class<E> enumClass) {
        return new EnumMissionType<>(id, enumClass);
    }

    /**
     * A complex mission type which matches using {@link TargetValidator}s, used for complex missions that validate against many targets
     * a good example would be a type that takes an enchant, its level, and the item its being applied to. those would utilize 3 {@link TargetValidator}s
     * <p>
     * Examples of usage:
     * <pre>{@code
     * EasyMissionsAPI api = EasyMissionsAPI.getInstance();
     *
     * Registry<@NotNull Enchantment> enchantment = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
     *
     * api.registerType(MissionType.complex("some_id",
     *         Validators.matchRegistry(enchantment), // match against the Enchantment registry
     *         Validators.matchInt(), // match an integer for enchant level
     *         Validators.matchEnum(Material.class))); // match an enum for item type
     * }</pre>
     *
     * @param id         the identifier for your mission type, you'll use this in your listeners and mission configurations will be verified against this
     * @param validators the {@link TargetValidator}s you want to match against, see {@link Validators}
     * @return a {@link ComplexMissionType} instance
     * @see Validators#matchRegistry(Registry)
     * @see Validators#matchEnum(Class)
     * @see Validators#matchInt()
     */
    @Contract("_, _ -> new")
    static @NotNull TargetedMissionType complex(@NotNull String id, @NotNull TargetValidator... validators) {
        return new ComplexMissionType(id, List.of(validators));
    }

    /**
     * A {@link Registry} mission type, used to match against a {@link Registry} such as {@link org.bukkit.enchantments.Enchantment}
     * <p>
     * Examples of usage:
     * <pre>{@code
     * EasyMissionsAPI api = EasyMissionsAPI.getInstance();
     * Registry<@NotNull Enchantment> enchantment = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
     *
     * api.registerType(MissionType.matchRegistry("some_id", enchantment));
     * }</pre>
     *
     * @param id       the identifier for your mission type, you'll use this in your listeners and mission configurations will be verified against this
     * @param registry the {@link Registry} you want to match against
     * @return a {@link KeyedMissionType} instance
     */
    @Contract("_, _ -> new")
    static <T extends Keyed> @NotNull TargetedMissionType matchRegistry(@NotNull String id, @NotNull Registry<@NotNull T> registry) {
        return new KeyedMissionType<>(id, registry);
    }

    @NotNull String id();


}
