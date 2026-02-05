package io.github.stev6.easymissions.type.impl;

import io.github.stev6.easymissions.context.impl.EntityKillContext;
import io.github.stev6.easymissions.exception.ConfigException;
import io.github.stev6.easymissions.matcher.impl.EntityDataMatcher;
import io.github.stev6.easymissions.matcher.impl.ItemDataMatcher;
import io.github.stev6.easymissions.type.MissionTarget;
import io.github.stev6.easymissions.type.TargetedMissionType;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Kill implements TargetedMissionType<EntityKillContext, Kill.KillData> {
    public static final Kill INSTANCE = new Kill();
    private static final String ID = "kill";

    private Kill() {
    }

    @Override
    public @NotNull String id() {
        return ID;
    }

    @Override
    public @NotNull KillData parse(@NotNull ConfigurationSection section) {
        ConfigurationSection entitySection = section.getConfigurationSection("entity");
        var entityMatcher = entitySection == null ? null : EntityDataMatcher.parse(entitySection);
        ConfigurationSection itemSection = section.getConfigurationSection("item");
        var itemMatcher = itemSection == null ? null : ItemDataMatcher.parse(itemSection);

        List<ItemDataMatcher> dropMatchers = new ArrayList<>();
        ConfigurationSection dropsSection = section.getConfigurationSection("drops");
        if (dropsSection != null) {
            for (String key : dropsSection.getKeys(false)) {
                var s = dropsSection.getConfigurationSection(key);
                if (s != null) dropMatchers.add(ItemDataMatcher.parse(s));
                else throw new ConfigException("Section `" + key + "` is not a valid section");
            }
        }
        return new KillData(entityMatcher, itemMatcher, dropMatchers);
    }

    public record KillData(@Nullable EntityDataMatcher entityMatcher,
                           @Nullable ItemDataMatcher itemMatcher,
                           @NotNull List<ItemDataMatcher> dropMatchers) implements MissionTarget<EntityKillContext> {
        @Override
        public boolean matches(EntityKillContext context) {
            if (entityMatcher != null && !entityMatcher.matches(context.entity())) return false;
            if (itemMatcher != null && !itemMatcher.matches(context.weapon())) return false;
            if (!dropMatchers.isEmpty() && context.drops().isEmpty()) return false;

            // each drop matcher must match at least one drop in the drops list
            return dropMatchers.stream().allMatch(m -> context.drops().stream().anyMatch(m::matches));
        }
    }
}