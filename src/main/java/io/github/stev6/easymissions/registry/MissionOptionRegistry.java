package io.github.stev6.easymissions.registry;

import com.google.common.base.Preconditions;
import io.github.stev6.easymissions.option.MissionOption;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@ApiStatus.Internal
public class MissionOptionRegistry {
    private final Map<String, Function<ConfigurationSection, MissionOption>> missionOptions = new ConcurrentHashMap<>();

    @ApiStatus.Internal
    public void register(String id, Function<ConfigurationSection, MissionOption> factory) {
        Preconditions.checkArgument(!missionOptions.containsKey(id), "ID %s already exists".formatted(id));
        missionOptions.put(id, factory);
    }

    @ApiStatus.Internal
    public MissionOption parse(String id, ConfigurationSection section) {
        var fun = missionOptions.get(id);
        return (fun != null) ? fun.apply(section) : null;
    }

    @ApiStatus.Internal
    public Map<String, Function<ConfigurationSection, MissionOption>> options() {
        return Collections.unmodifiableMap(missionOptions);
    }
}
