package io.github.stev6.easymissions.type;

import io.github.stev6.easymissions.EasyMissionsAPI;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for a mission type, mission types can be registered using the API method {@link EasyMissionsAPI#registerType(MissionType...)}
 * @see TargetedMissionType
 * @see EasyMissionsAPI
 */
@FunctionalInterface
public interface MissionType {

    /**
     * Convenience method to create a simple, targetless mission type.
     * @param id The name of the type
     * @return a mission type
     */
    static @NotNull MissionType simple(@NotNull String id) {
        return new SimpleType(id);
    }

    /**
     * String identifier for the type, used for registering in the registry and displaying placeholders
     * @return The name of the type
     */
    @NotNull String id();
}
