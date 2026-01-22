package io.github.stev6.easymissions.context.impl;

import io.github.stev6.easymissions.context.MissionContext;
import io.github.stev6.easymissions.context.Locatable;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockEvent;
import org.jetbrains.annotations.NotNull;

public record BlockContext(@NotNull Block block) implements MissionContext, Locatable {

    public static BlockContext from(BlockEvent e) {
        return new BlockContext(e.getBlock());
    }

    @NotNull
    @Override
    public Location getLocation() {
        return block.getLocation();
    }
}
