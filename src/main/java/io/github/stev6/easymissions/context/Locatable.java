package io.github.stev6.easymissions.context;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public interface Locatable {
    @NotNull Location getLocation();
}
