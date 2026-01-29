package io.github.stev6.easymissions.type.impl;

import io.github.stev6.easymissions.context.impl.BlockContext;
import io.github.stev6.easymissions.matcher.impl.EnumMatcher;
import io.github.stev6.easymissions.type.MissionTarget;
import io.github.stev6.easymissions.type.TargetedMissionType;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class Harvest implements TargetedMissionType<BlockContext, Harvest.HarvestData> {
    public static final Harvest INSTANCE = new Harvest();
    private static final String ID = "harvest";

    private Harvest() {
    }

    @Override
    public @NotNull String id() {
        return ID;
    }

    @Override
    public @NotNull HarvestData parse(@NotNull ConfigurationSection section) {
        var blockStrings = new HashSet<>(section.getStringList("materials"));
        return new HarvestData(EnumMatcher.parse(Material.class, blockStrings));
    }

    public record HarvestData(EnumMatcher<Material> blocks) implements MissionTarget<BlockContext> {
        @Override
        public boolean matches(BlockContext context) {
            return blocks.matches(context.block().getType());
        }
    }
}
