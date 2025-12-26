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

package io.github.stev6.easymissions.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public record CategoryArgument(Set<String> categories) implements CustomArgumentType.Converted<@NotNull String, @NotNull String> {

    private static final DynamicCommandExceptionType ERROR_INVALID_CATEGORY =
            new DynamicCommandExceptionType(category -> MessageComponentSerializer.message()
                    .serialize(Component.text(category + " is an invalid category.")));

    @Override
    public String convert(String nativeType) throws CommandSyntaxException {
        if (!categories.contains(nativeType.toLowerCase())) {
            throw ERROR_INVALID_CATEGORY.create(nativeType);
        }
        return nativeType.toLowerCase();
    }

    @Override
    public <S> @NotNull CompletableFuture<Suggestions> listSuggestions(
            @NotNull CommandContext<S> context, @NotNull SuggestionsBuilder builder) {
        categories.stream()
                .filter(m -> m.startsWith(builder.getRemainingLowerCase()))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }

    @Override
    public @NotNull ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }
}
