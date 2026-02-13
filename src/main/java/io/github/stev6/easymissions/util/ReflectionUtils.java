package io.github.stev6.easymissions.util;

import com.google.common.reflect.ClassPath;
import io.github.stev6.easymissions.registry.MissionTypeRegistry;
import io.github.stev6.easymissions.type.MissionType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReflectionUtils {

    /**
     * Automatically register all your types in a package for you, may be unstable/or break, but it helps deduplicate code
     * @param simpleTypesClass Your class containing the simple types if you wish to pass one or null if you don't have one
     * @param registry An instance of the {@link MissionTypeRegistry} from an API method
     * @param pkgPath The package containing all your type classes, like {@code example.example.example.types}
     * @param plugin Instance of your plugin for logging and registering
     */
    public static void registerAll(@Nullable Class<?> simpleTypesClass, @NotNull MissionTypeRegistry registry, @NotNull String pkgPath, @NotNull JavaPlugin plugin) {
        Logger logger = plugin.getLogger();

        if (simpleTypesClass != null) {
            try {
                getMissionTypes(simpleTypesClass).forEach(registry::registerType);
            } catch (Exception e) {
                logger.severe("Failed to register simple types: " + e.getMessage());
            }
        }

        try {
            ClassPath path = ClassPath.from(plugin.getClass().getClassLoader());
            for (ClassPath.ClassInfo c : path.getTopLevelClasses(pkgPath)) {
                var clazz = c.load();
                if (Modifier.isAbstract(clazz.getModifiers()) || clazz.isInterface()) continue;
                if (MissionType.class.isAssignableFrom(clazz)) {
                    try {
                        List<MissionType> types = getMissionTypes(clazz);
                        if (!types.isEmpty()) types.forEach(registry::registerType);
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Failed to use reflection to load mission type: " + clazz.getName(), e);
                    }
                }
            }
        } catch (Exception e) {
            logger.severe("Failed to load mission types: " + e.getMessage());
        }
    }

    private static List<MissionType> getMissionTypes(Class<?> clazz) throws IOException {
        List<MissionType> types = new ArrayList<>();
        for (Field f : clazz.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isStatic(mod) && Modifier.isFinal(mod) && Modifier.isPublic(mod)) {
                try {
                    Object value = f.get(null);
                    if (value instanceof MissionType type) types.add(type);
                } catch (Exception e) {
                    throw new IOException("Failed to get singleton mission type: " + f.getName(), e);
                }
            }
        }
        return types;
    }
}
