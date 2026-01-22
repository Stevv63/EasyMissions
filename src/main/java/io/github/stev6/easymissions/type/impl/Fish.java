package io.github.stev6.easymissions.type.impl;

import io.github.stev6.easymissions.type.MissionTarget;
import io.github.stev6.easymissions.type.TargetedMissionType;
import io.github.stev6.easymissions.context.impl.ItemContext;
import io.github.stev6.easymissions.matcher.impl.ItemDataMatcher;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Fish implements TargetedMissionType<ItemContext, Fish.FishData> {
    public static final Fish INSTANCE = new Fish();
    private static final String ID = "fish";

    private Fish() {
    }

    @Override
    public @NotNull String id() {
        return ID;
    }

    @Override
    public @NotNull FishData parse(@NotNull ConfigurationSection section) {
        var itemSection = section.getConfigurationSection("item");
        ItemDataMatcher itemMatcher = itemSection == null ? null : ItemDataMatcher.parse(itemSection);
        return new FishData(itemMatcher);
    }

    public record FishData(@Nullable ItemDataMatcher itemMatcher) implements MissionTarget<ItemContext> {

        @Override
        public boolean matches(ItemContext context) {
            return itemMatcher == null || itemMatcher.matches(context.item());
        }
    }
}