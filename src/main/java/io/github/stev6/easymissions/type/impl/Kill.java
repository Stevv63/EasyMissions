package io.github.stev6.easymissions.type.impl;

import io.github.stev6.easymissions.context.impl.EntityKillContext;
import io.github.stev6.easymissions.matcher.impl.EntityDataMatcher;
import io.github.stev6.easymissions.matcher.impl.ItemDataMatcher;
import io.github.stev6.easymissions.type.MissionTarget;
import io.github.stev6.easymissions.type.TargetedMissionType;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        return new KillData(entityMatcher, itemMatcher);
    }

    public record KillData(@Nullable EntityDataMatcher entityMatcher,
                           @Nullable ItemDataMatcher itemMatcher) implements MissionTarget<EntityKillContext> {
        @Override
        public boolean matches(EntityKillContext context) {
            return (entityMatcher == null || entityMatcher.matches(context.entity())) && (itemMatcher == null || itemMatcher.matches(context.weapon()));
        }
    }
}