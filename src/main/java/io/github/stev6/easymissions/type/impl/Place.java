package io.github.stev6.easymissions.type.impl;

import io.github.stev6.easymissions.type.MissionTarget;
import io.github.stev6.easymissions.type.TargetedMissionType;
import io.github.stev6.easymissions.context.impl.BlockContext;
import io.github.stev6.easymissions.matcher.impl.EnumMatcher;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class Place implements TargetedMissionType<BlockContext, Place.PlaceData> {

    public static final Place INSTANCE = new Place();

    private Place() {
    }

    @Override
    public @NotNull String id() {
        return "place";
    }

    @Override
    public @NotNull PlaceData parse(@NotNull ConfigurationSection section) {
        var matStrings = new HashSet<>(section.getStringList("materials"));
        return new PlaceData(EnumMatcher.parse(Material.class, matStrings));
    }

    public record PlaceData(@NotNull EnumMatcher<Material> materials) implements MissionTarget<BlockContext> {
        @Override
        public boolean matches(@NotNull BlockContext context) {
            return materials.matches(context.block().getType());
        }
    }
}
