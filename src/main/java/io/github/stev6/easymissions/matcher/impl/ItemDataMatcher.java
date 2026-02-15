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

package io.github.stev6.easymissions.matcher.impl;

import io.github.stev6.easymissions.exception.ConfigException;
import io.github.stev6.easymissions.matcher.ValueMatcher;
import io.github.stev6.easymissions.util.IntRange;
import io.github.stev6.easymissions.util.ListenerUtils;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * {@link ValueMatcher} that validates an {@link ItemStack} against multiple predicates.
 * <p>
 * This matcher checks (if configured):
 * <ul>
 *     <li>Material type (via {@link EnumMatcher})</li>
 *     <li>Stack amount (via {@link IntRange})</li>
 *     <li>Item model (useful for custom items that have custom textures but no PDC keys)</li>
 *     <li>Potion types (if the item is a potion/tipped arrow)</li>
 *     <li>Enchantments (via {@link ConfigEnchantmentMatcher})</li>
 *     <li>PersistentDataContainer keys (PDC)</li>
 * </ul>
 *
 **/
public class ItemDataMatcher implements ValueMatcher<ItemStack> {

    private final Predicate<ItemStack> predicate;

    public ItemDataMatcher(Predicate<ItemStack> predicate) {
        this.predicate = predicate;
    }

    /**
     * Parses a {@link ConfigurationSection} to create an ItemDataMatcher.
     *
     * @param section The configuration section (common convention to name its section "item" unless multiple can be set)
     * @return A new matcher instance
     * @throws ConfigException If configuration values (like materials or keys) are invalid
     */
    public static ItemDataMatcher parse(@NotNull ConfigurationSection section) {

        Predicate<ItemStack> predicate = Objects::nonNull;

        HashSet<String> materials = new HashSet<>(section.getStringList("materials"));
        if (!materials.isEmpty()) {
            EnumMatcher<Material> matcher = EnumMatcher.parse(Material.class, materials);
            predicate = predicate.and(i -> matcher.matches(i.getType()));
        }

        HashSet<String> potionTypes = new HashSet<>(section.getStringList("potion_types"));
        if (!potionTypes.isEmpty()) {
            EnumMatcher<PotionType> matcher = EnumMatcher.parse(PotionType.class, potionTypes);
            predicate = predicate.and(i -> {
                PotionType type = ListenerUtils.getPotionTypeOrNull(i);
                return type != null && matcher.matches(type);
            });
        }

        HashSet<String> itemModels = new HashSet<>(section.getStringList("item_models"));
        if (!itemModels.isEmpty()) {
            predicate = predicate.and(i -> {
                @SuppressWarnings("UnstableApiUsage")
                Key itemModel = i.getData(DataComponentTypes.ITEM_MODEL);

                return itemModel != null && itemModels.contains(itemModel.asString());
            });
        }

        ConfigurationSection enchSection = section.getConfigurationSection("enchants");
        if (enchSection != null) {
            boolean matchAny = section.getBoolean("match_any_enchant", true);
            ConfigEnchantmentMatcher enchants = ConfigEnchantmentMatcher.parse(enchSection, matchAny);
            if (enchants != null)
                predicate = predicate.and(i -> enchants.matches(ListenerUtils.getAllEnchants(i)));
        }

        String amountStr = section.getString("amount");
        IntRange amountRange;
        if (amountStr != null) {
            amountRange = IntRange.fromString(amountStr);
            predicate = predicate.and(i -> amountRange.isWithin(i.getAmount()));
        }

        HashSet<String> pdcKeys = new HashSet<>(section.getStringList("pdc"));
        if (!pdcKeys.isEmpty()) {
            for (String key : pdcKeys) {
                NamespacedKey namespaceKey = NamespacedKey.fromString(key);
                if (namespaceKey == null)
                    throw new ConfigException("Invalid NamespacedKey format: '" + key + "'. Must be 'namespace:key'.");

                predicate = predicate.and(i -> i.getPersistentDataContainer().has(namespaceKey));
            }
        }

        return new ItemDataMatcher(predicate);
    }

    @Override
    public boolean matches(ItemStack value) {
        return predicate.test(value);
    }
}
