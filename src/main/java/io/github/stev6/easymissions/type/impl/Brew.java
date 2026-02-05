package io.github.stev6.easymissions.type.impl;

import io.github.stev6.easymissions.context.impl.ItemContext;
import io.github.stev6.easymissions.matcher.impl.ItemDataMatcher;
import io.github.stev6.easymissions.matcher.impl.RegistryMatcher;
import io.github.stev6.easymissions.type.MissionTarget;
import io.github.stev6.easymissions.type.TargetedMissionType;
import io.github.stev6.easymissions.util.ListenerUtils;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

public class Brew implements TargetedMissionType<ItemContext, Brew.BrewData> {
    public static final Brew INSTANCE = new Brew();
    private static final String ID = "brew";

    private Brew() {
    }

    @Override
    public @NotNull String id() {
        return ID;
    }

    @Override
    public @NotNull BrewData parse(@NotNull ConfigurationSection section) {
        var itemSection = section.getConfigurationSection("item");
        ItemDataMatcher itemMatcher = itemSection == null ? null : ItemDataMatcher.parse(itemSection);

        HashSet<String> effectStrings = new HashSet<>(section.getStringList("effects"));
        RegistryMatcher<PotionEffectType> effectMatcher = RegistryMatcher.parse(Registry.MOB_EFFECT, effectStrings);
        return new BrewData(itemMatcher, effectMatcher);
    }

    public record BrewData(@Nullable ItemDataMatcher itemMatcher,
                           @NotNull RegistryMatcher<PotionEffectType> effectMatcher) implements MissionTarget<ItemContext> {
        @Override
        public boolean matches(ItemContext context) {
            if (itemMatcher != null && !itemMatcher.matches(context.item())) return false;
            if (effectMatcher.any()) return true;
            var effects = ListenerUtils.getAllPotionEffects(context.item());
            if (effects != null) {
                for (var effect : effects) if (effectMatcher.matches(effect)) return true;
            }
            return false;
        }
    }
}
