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

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.stev6.easymissions.EasyMissions;
import io.github.stev6.easymissions.config.data.MissionConfig;
import io.github.stev6.easymissions.event.MissionCreateEvent;
import io.github.stev6.easymissions.event.MissionCreateEvent.Source;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public sealed abstract class EasyMissionsCommand permits CategoryRandomCommand, DataCommand, GiveCommand, ListTypesCommand, RandomCommand, ReloadCommand, SetCommand {

    protected final String name;
    protected final EasyMissions plugin;

    public EasyMissionsCommand(String name, EasyMissions plugin) {
        this.name = name;
        this.plugin = plugin;
    }

    protected void giveItem(CommandSender sender, Player target, MissionConfig config, Source source) {
        var manager = plugin.getMissionManager();
        var mainConfig = plugin.getConfigManager().getMainConfig();
        if (config == null) {
            sender.sendRichMessage(mainConfig.messages().randMissionNotFound());
            return;
        }

        var missionItem = manager.createMissionItem(config);
        var mission = manager.getMissionOrNull(missionItem);

        if (mission == null) return; // not possible but who cares

        MissionCreateEvent event = new MissionCreateEvent(target, missionItem, mission, config, source);

        if (!event.callEvent()) return;

        target.give(missionItem);
        sender.sendRichMessage(
                mainConfig.messages().giveMission(),
                Placeholder.unparsed("target", target.getName()),
                Placeholder.unparsed("mission", config.key())
        );
    }

    public abstract void addToTree(ArgumentBuilder<CommandSourceStack, ?> root);

    public abstract int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException;
}
