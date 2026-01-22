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

package io.github.stev6.easymissions.command.subcommand;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import io.github.stev6.easymissions.EasyMissions;
import io.github.stev6.easymissions.mission.Mission;

public final class DataCommand extends EasyMissionsCommand {



    public DataCommand(String name, EasyMissions plugin) {
        super(name, plugin);
    }

    @Override
    public void addToTree(ArgumentBuilder<CommandSourceStack, ?> root) {
        root.then(Commands.literal(name).executes(this::execute));
    }

    @Override
    public int execute(CommandContext<CommandSourceStack> ctx) {
        var mainConfig = plugin.getConfigManager().getMainConfig();
        var manager = plugin.getMissionManager();
        if (!(ctx.getSource().getSender() instanceof Player p)) {
            ctx.getSource().getSender().sendRichMessage(mainConfig.messages().needsPlayer());
            return Command.SINGLE_SUCCESS;
        }
        ItemStack i = p.getInventory().getItemInMainHand();
        Mission m = manager.getMissionOrNull(i);

        if (m == null) {
            p.sendRichMessage(mainConfig.messages().needsMission());
            return Command.SINGLE_SUCCESS;
        }

        p.sendRichMessage(mainConfig.menus().dataMenu(), manager.getMissionTags(m), Placeholder.component("name", i.displayName()));
        return Command.SINGLE_SUCCESS;
    }
}
