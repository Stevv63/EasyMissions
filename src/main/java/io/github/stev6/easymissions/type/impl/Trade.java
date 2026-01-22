package io.github.stev6.easymissions.type.impl;

import io.github.stev6.easymissions.type.MissionTarget;
import io.github.stev6.easymissions.type.TargetedMissionType;
import io.github.stev6.easymissions.context.impl.ItemContext;
import io.github.stev6.easymissions.matcher.impl.ItemDataMatcher;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Trade implements TargetedMissionType<ItemContext, Trade.TradeData> {
    public static final Trade INSTANCE = new Trade();
    private static final String ID = "trade";

    private Trade() {
    }

    @Override
    public @NotNull String id() {
        return ID;
    }

    @Override
    public @NotNull TradeData parse(@NotNull ConfigurationSection section) {
        var itemSection = section.getConfigurationSection("item");
        ItemDataMatcher itemMatcher = itemSection == null ? null : ItemDataMatcher.parse(itemSection);
        return new TradeData(itemMatcher);
    }

    public record TradeData(@Nullable ItemDataMatcher itemMatcher) implements MissionTarget<ItemContext> {
        @Override
        public boolean matches(ItemContext context) {
            return itemMatcher == null || itemMatcher.matches(context.item());
        }
    }
}
