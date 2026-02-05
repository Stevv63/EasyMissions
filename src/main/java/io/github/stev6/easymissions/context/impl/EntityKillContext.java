package io.github.stev6.easymissions.context.impl;

import io.github.stev6.easymissions.context.MissionContext;
import io.github.stev6.easymissions.context.Locatable;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record EntityKillContext(@NotNull Entity entity, ItemStack weapon, List<ItemStack> drops) implements MissionContext, Locatable {
    @NotNull
    @Override
    public Location getLocation() {
        return entity.getLocation();
    }
}


