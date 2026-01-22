package io.github.stev6.easymissions.type;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface MissionType {

    static @NotNull MissionType simple(@NotNull String id) {
        return new SimpleType(id);
    }

    @NotNull String id();

}
