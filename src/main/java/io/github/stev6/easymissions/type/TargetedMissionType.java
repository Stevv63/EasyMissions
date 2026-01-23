package io.github.stev6.easymissions.type;

import io.github.stev6.easymissions.EasyMissions;
import io.github.stev6.easymissions.context.MissionContext;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public interface TargetedMissionType<C extends MissionContext, D extends MissionTarget<C>> extends MissionType {

    @NotNull D parse(@NotNull ConfigurationSection section);

    default boolean matches(@NotNull D data, @NotNull C context) {
        return data.matches(context);
    }

    @SuppressWarnings("unchecked")
    default boolean matchesRaw(Object data, Object context) {
        try {
            return matches((D) data, (C) context);
        } catch (ClassCastException e) {
            EasyMissions.getInstance().getLogger().severe("Cast error: " + e.getMessage());
            EasyMissions.getInstance().getLogger().severe("For type: " + id());
            return false;
        }
    }
}
