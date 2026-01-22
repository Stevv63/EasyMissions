package io.github.stev6.easymissions.type.impl;

import io.github.stev6.easymissions.context.impl.ItemContext;
import io.github.stev6.easymissions.matcher.impl.ItemDataMatcher;
import io.github.stev6.easymissions.type.MissionTarget;
import io.github.stev6.easymissions.type.TargetedMissionType;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class Repair implements TargetedMissionType<ItemContext, Repair.RepairData> {
    public static final Repair INSTANCE = new Repair();
    private static final String ID = "repair";

    @Override
    public @NotNull RepairData parse(@NotNull ConfigurationSection section) {
        var itemSection = section.getConfigurationSection("item");
        ItemDataMatcher itemMatcher = itemSection == null ? null : ItemDataMatcher.parse(itemSection);
        return new RepairData(itemMatcher);
    }

    @Override
    public @NotNull String id() {
        return ID;
    }

    public record RepairData(ItemDataMatcher itemMatcher) implements MissionTarget<ItemContext> {
        @Override
        public boolean matches(ItemContext context) {
            return itemMatcher == null || itemMatcher.matches(context.item());
        }
    }
}