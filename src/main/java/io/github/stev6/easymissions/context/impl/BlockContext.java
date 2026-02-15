/*
 * EasyMissions – A Minecraft missions plugin.
 * Copyright (C) 2026 Stev6
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
