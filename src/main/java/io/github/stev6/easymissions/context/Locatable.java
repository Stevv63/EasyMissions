package io.github.stev6.easymissions.context;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for {@link MissionContext}s  that provide location details to be passed into
 */
public interface Locatable {
    @NotNull Location getLocation();
}
