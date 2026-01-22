package io.github.stev6.easymissions.type.impl;

import io.github.stev6.easymissions.context.impl.ItemContext;
import io.github.stev6.easymissions.matcher.impl.ItemDataMatcher;
import io.github.stev6.easymissions.type.MissionTarget;
import io.github.stev6.easymissions.type.TargetedMissionType;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public class Craft implements TargetedMissionType<ItemContext, Craft.CraftData> {
    public static final Craft INSTANCE = new Craft();
    private static final String ID = "craft";

    @Override
    public @NonNull CraftData parse(@NotNull ConfigurationSection section) {
        var itemSection = section.getConfigurationSection("item");
        ItemDataMatcher itemMatcher = itemSection == null ? null : ItemDataMatcher.parse(itemSection);
        return new CraftData(itemMatcher);
    }

    @Override
    public @NotNull String id() {
        return ID;
    }

    public record CraftData(ItemDataMatcher itemMatcher) implements MissionTarget<ItemContext> {
        @Override
        public boolean matches(ItemContext context) {
            return itemMatcher == null || itemMatcher.matches(context.item());
        }
    }
}
