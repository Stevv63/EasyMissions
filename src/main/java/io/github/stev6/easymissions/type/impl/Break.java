package io.github.stev6.easymissions.type.impl;

import io.github.stev6.easymissions.context.impl.BlockContext;
import io.github.stev6.easymissions.matcher.impl.EnumMatcher;
import io.github.stev6.easymissions.type.MissionTarget;
import io.github.stev6.easymissions.type.TargetedMissionType;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class Break implements TargetedMissionType<BlockContext, Break.BreakData> {

    public static final Break INSTANCE = new Break();
    private static final String ID = "break";

    private Break() {
    }

    @Override
    public @NotNull String id() {
        return ID;
    }

    @Override
    public @NotNull BreakData parse(@NotNull ConfigurationSection section) {
        var matStrings = new HashSet<>(section.getStringList("materials"));
        return new BreakData(EnumMatcher.parse(Material.class, matStrings));
    }

    public record BreakData(@NotNull EnumMatcher<Material> materials) implements MissionTarget<BlockContext> {
        @Override
        public boolean matches(@NotNull BlockContext context) {
            return materials.matches(context.block().getType());
        }
    }
}

