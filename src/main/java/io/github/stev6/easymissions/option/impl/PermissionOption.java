    package io.github.stev6.easymissions.option.impl;

    import io.github.stev6.easymissions.context.MissionContext;
    import io.github.stev6.easymissions.mission.Mission;
    import io.github.stev6.easymissions.option.MissionOption;
    import org.bukkit.configuration.ConfigurationSection;
    import org.bukkit.entity.Player;
    import org.bukkit.inventory.ItemStack;
    import org.jetbrains.annotations.NotNull;

    import java.util.HashSet;
    import java.util.Set;

    /**
     * An example and convenience option that checks if a player has a specific permission.
     */
    public class PermissionOption implements MissionOption {

        private final Set<String> permissions;

        // this constructor is required to match the Function<ConfigurationSection, MissionOption> parameter
        public PermissionOption(ConfigurationSection section) {
            this.permissions = new HashSet<>(section.getStringList("values"));
        }

        // apply your checks here
        @Override
        public boolean check(@NotNull Player player, @NotNull Mission mission, @NotNull ItemStack item, @NotNull MissionContext context) {
            if (permissions.isEmpty()) return true;
            for (String p : permissions) if (player.hasPermission(p)) return true;

            return false;
        }
    }
