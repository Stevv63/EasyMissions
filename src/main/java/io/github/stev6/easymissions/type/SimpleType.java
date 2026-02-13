package io.github.stev6.easymissions.type;

import org.jetbrains.annotations.NotNull;

/**
 * A simple record for simple mission types, if you need to specify targets, take a look at {@link TargetedMissionType}
 * @param id The name you wish to give to the type, will be used in placeholders and the type registry
 */
public record SimpleType(@NotNull String id) implements MissionType {
}
