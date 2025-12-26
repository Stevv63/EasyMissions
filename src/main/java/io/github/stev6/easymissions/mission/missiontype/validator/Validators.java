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

package io.github.stev6.easymissions.mission.missiontype.validator;

import com.google.common.base.Enums;
import io.github.stev6.easymissions.mission.missiontype.types.MissionType;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;


/**
 * provides methods for common {@link TargetValidator} implementations.
 * <p>
 * These validators are for creating complex multitarget mission types
 * using {@link MissionType#complex(String, TargetValidator...)}.
 * <p>
 * <b>For simpler, normal mission types, you should use the helpers on the {@link MissionType}
 * interface directly, such as {@link MissionType#matchEnum(String, Class)} or
 * {@link MissionType#matchRegistry(String, Registry)}.</b>
 */
public class Validators {

    /**
     * Creates a validator that checks if a string matches an enum class value.
     * <p>
     * This is intended for use with {@link MissionType#complex}.
     * For a simple enum mission, use {@link MissionType#matchEnum(String, Class)} instead.
     *
     * @param enumClass The {@link Enum} class to match against (e.g., {@code Material.class}).
     * @return A {@link TargetValidator} instance.
     * @see #matchRegistry(Registry)
     * @see #matchInt()
     * @see MissionType#complex(String, TargetValidator...)
     */
    public static <E extends Enum<E>> TargetValidator matchEnum(Class<E> enumClass) {
        return token -> Enums.getIfPresent(enumClass, token.trim().toUpperCase(Locale.ROOT)).isPresent();
    }

    /**
     * Creates a validator that checks if a string matches a {@link Registry}'s {@link NamespacedKey}.
     * <p>
     * This is intended for use with {@link MissionType#complex}.
     * For a simple registry mission, use {@link MissionType#matchRegistry(String, Registry)} instead.
     *
     * @param registry The {@link Registry} class to match against (e.g., {@link org.bukkit.enchantments.Enchantment}).
     * @return A {@link TargetValidator} instance.
     * @see #matchEnum(Class)
     * @see #matchInt()
     * @see MissionType#complex(String, TargetValidator...)
     */
    public static <T extends Keyed> TargetValidator matchRegistry(Registry<@NotNull T> registry) {
        return new TargetValidator() {
            @Override
            public boolean validate(@NotNull String token) {
                NamespacedKey key = NamespacedKey.fromString(token.trim().toLowerCase(Locale.ROOT));
                return key != null && registry.get(key) != null;
            }

            @Override
            public String normalize(@NotNull String token) {
                NamespacedKey key = NamespacedKey.fromString(token.trim());
                if (key == null) return token;
                return key.asString();
            }
        };
    }

    /**
     * Creates a validator that checks if a string is a valid int.
     * <p>
     * This is intended for use with {@link MissionType#complex}.
     * For a simple missions, see {@link MissionType} instead.
     *
     * @return A {@link TargetValidator} instance.
     * @see #matchEnum(Class)
     * @see #matchInt()
     * @see MissionType#complex(String, TargetValidator...)
     */
    public static TargetValidator matchInt() {
        return token -> {
            try {
                Integer.parseInt(token);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        };
    }
}
