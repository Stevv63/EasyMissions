package io.github.stev6.easymissions.matcher.impl;

import io.github.stev6.easymissions.exception.ConfigException;
import io.github.stev6.easymissions.matcher.ValueMatcher;
import io.github.stev6.easymissions.util.IntRange;
import io.github.stev6.easymissions.util.ListenerUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;

import java.util.HashSet;
import java.util.Objects;
import java.util.function.Predicate;

public class ItemDataMatcher implements ValueMatcher<ItemStack> {

    private final Predicate<ItemStack> predicate;

    public ItemDataMatcher(Predicate<ItemStack> predicate) {
        this.predicate = predicate;
    }

    public static ItemDataMatcher parse(ConfigurationSection section) {

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
