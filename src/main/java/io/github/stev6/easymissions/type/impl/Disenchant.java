package io.github.stev6.easymissions.type.impl;

import io.github.stev6.easymissions.context.impl.ItemContext;
import io.github.stev6.easymissions.matcher.impl.ItemDataMatcher;
import io.github.stev6.easymissions.type.MissionTarget;
import io.github.stev6.easymissions.type.TargetedMissionType;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class Disenchant implements TargetedMissionType<ItemContext, Disenchant.DisenchantData> {
    public static final Disenchant INSTANCE = new Disenchant();
    private static final String ID = "disenchant";

    @Override
    public @NotNull DisenchantData parse(@NotNull ConfigurationSection section) {
        var itemSection = section.getConfigurationSection("item");
        ItemDataMatcher itemMatcher = itemSection == null ? null : ItemDataMatcher.parse(itemSection);
        return new DisenchantData(itemMatcher);
    }

    @Override
    public @NotNull String id() {
        return ID;
    }

    public record DisenchantData(ItemDataMatcher itemMatcher) implements MissionTarget<ItemContext> {
        @Override
        public boolean matches(ItemContext context) {
            return itemMatcher == null || itemMatcher.matches(context.item());
        }
    }
}
