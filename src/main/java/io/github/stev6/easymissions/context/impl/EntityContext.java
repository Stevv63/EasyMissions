package io.github.stev6.easymissions.context.impl;

import io.github.stev6.easymissions.context.Locatable;
import io.github.stev6.easymissions.context.MissionContext;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

public record EntityContext(@NotNull Entity entity) implements MissionContext, Locatable {
    @NotNull
    @Override
    public Location getLocation() {
        return entity.getLocation();
    }
}