package io.github.stev6.easymissions.type.impl;

import io.github.stev6.easymissions.context.impl.EnchantContext;
import io.github.stev6.easymissions.matcher.impl.ConfigEnchantmentMatcher;
import io.github.stev6.easymissions.matcher.impl.ItemDataMatcher;
import io.github.stev6.easymissions.type.MissionTarget;
import io.github.stev6.easymissions.type.TargetedMissionType;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Enchant implements TargetedMissionType<EnchantContext, Enchant.EnchantData> {
    public static final Enchant INSTANCE = new Enchant();
    private static final String ID = "enchant";

    private Enchant() {
    }

    @Override
    public @NotNull String id() {
        return ID;
    }

    @Override
    public @NotNull EnchantData parse(@NotNull ConfigurationSection section) {
        ConfigEnchantmentMatcher requirements = null;
        boolean matchAny = section.getBoolean("match_any_enchant", true);

        var enchantSection = section.getConfigurationSection("enchants");
        if (enchantSection != null) requirements = ConfigEnchantmentMatcher.parse(enchantSection, matchAny);

        var itemSection = section.getConfigurationSection("item");
        ItemDataMatcher itemMatcher = itemSection == null ? null : ItemDataMatcher.parse(itemSection);

        boolean ignoreAnvil = section.getBoolean("ignore_anvil", false);

        return new EnchantData(requirements, itemMatcher, ignoreAnvil);
    }

    public record EnchantData(@Nullable ConfigEnchantmentMatcher enchants,
                              @Nullable ItemDataMatcher itemMatcher,
                              boolean ignoreAnvil) implements MissionTarget<EnchantContext> {

        @Override
        public boolean matches(@NotNull EnchantContext context) {
            if (ignoreAnvil && context.anvil()) return false;
            if (itemMatcher != null && !itemMatcher.matches(context.enchantItem())) return false;
            if (enchants == null) return true;
            return enchants.matches(context.enchants());
        }
    }
}
