/*
 * EasyMissions – A Minecraft missions plugin.
 * Copyright (C) 2025 Stev6
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

package io.github.stev6.easymissions.command;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.stev6.easymissions.EasyMissions;
import io.github.stev6.easymissions.command.subcommand.*;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class MissionCommands {

    private final EasyMissions plugin;

    public MissionCommands(EasyMissions plugin) {
        this.plugin = plugin;
    }

    public LiteralCommandNode<CommandSourceStack> buildMainCommand() {
        var root = Commands.literal("easymissions").requires(s -> s.getSender().hasPermission("easymissions.admin"));

        new RandomCommand("random", plugin).addToTree(root);
        new CategoryRandomCommand("category-random", plugin).addToTree(root);
        new GiveCommand("give", plugin).addToTree(root);
        new ReloadCommand("reload", plugin).addToTree(root);
        new DataCommand("data", plugin).addToTree(root);
        new SetCommand("set", plugin).addToTree(root);
        new ListTypesCommand("list-types", plugin).addToTree(root);

        return root.build();
    }
}
