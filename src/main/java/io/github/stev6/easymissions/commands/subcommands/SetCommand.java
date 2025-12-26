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

package io.github.stev6.easymissions.commands.subcommands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.stev6.easymissions.EasyMissions;
import io.github.stev6.easymissions.MissionManager;
import io.github.stev6.easymissions.commands.arguments.MissionEntryArgument;
import io.github.stev6.easymissions.config.records.MissionConfig;
import io.github.stev6.easymissions.mission.Mission;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public final class SetCommand extends EasyMissionsCommand {
    private final MissionManager manager;

    public SetCommand(String name, EasyMissions plugin) {
        super(name, plugin);
        this.manager = plugin.getMissionManager();
    }

    @Override
    public void addToTree(ArgumentBuilder<CommandSourceStack, ?> root) {
        var setTree = Commands.literal(name);

        setTree.then(Commands.literal("progress")
                .then(Commands.argument("value", IntegerArgumentType.integer(0))
                        .executes(ctx -> modifyMission(ctx, m -> m.setProgress(IntegerArgumentType.getInteger(ctx, "value"))))
                )
        );

        setTree.then(Commands.literal("requirement")
                .then(Commands.argument("value", IntegerArgumentType.integer(0))
                        .executes(ctx -> modifyMission(ctx, m -> m.setRequirement(IntegerArgumentType.getInteger(ctx, "value"))))
                )
        );

        setTree.then(Commands.literal("entry")
                .then(Commands.argument("config", new MissionEntryArgument(plugin.getConfigManager().getMissions()))
                        .executes(ctx -> modifyMission(ctx, m -> m.setConfigID(ctx.getArgument("config", MissionConfig.class).key())))
                )
        );

        setTree.then(Commands.literal("completed")
                .then(Commands.argument("value", BoolArgumentType.bool())
                        .executes(ctx -> modifyMission(ctx, m -> m.setCompleted(BoolArgumentType.getBool(ctx, "value"))))
                )
        );

        root.then(setTree);

    }

    @Override
    public int execute(CommandContext<CommandSourceStack> ctx) {
        return 0;
    }

    private int modifyMission(CommandContext<CommandSourceStack> ctx, Consumer<Mission> modification) {
        var mainConfig = plugin.getConfigManager().getMainConfig();
        if (!(ctx.getSource().getSender() instanceof Player p)) {
            ctx.getSource().getSender().sendRichMessage(mainConfig.messages().needsPlayer());
            return Command.SINGLE_SUCCESS;
        }

        ItemStack i = p.getInventory().getItemInMainHand();

        if (!manager.editMission(i, modification)) {
            p.sendRichMessage(mainConfig.messages().needsMission());
            return Command.SINGLE_SUCCESS;
        }

        p.sendRichMessage(mainConfig.messages().setSuccess());
        return Command.SINGLE_SUCCESS;
    }

}
