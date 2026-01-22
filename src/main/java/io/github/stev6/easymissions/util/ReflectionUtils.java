package io.github.stev6.easymissions.util;

import com.google.common.reflect.ClassPath;
import io.github.stev6.easymissions.EasyMissions;
import io.github.stev6.easymissions.registry.MissionTypeRegistry;
import io.github.stev6.easymissions.type.MissionType;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReflectionUtils {

    public static void registerAll(Class<?> simpleTypesClass, MissionTypeRegistry registry, String pkgPath, JavaPlugin plugin) {
        Logger logger = plugin.getLogger();

        try {
            getMissionTypes(simpleTypesClass).forEach(registry::registerType);
        } catch (Exception e) {
            logger.severe("Failed to register simple types: " + e.getMessage());
        }

        try {
            ClassPath path = ClassPath.from(EasyMissions.class.getClassLoader());
            for (ClassPath.ClassInfo c : path.getTopLevelClasses(pkgPath)) {
                var clazz = c.load();
                if (Modifier.isAbstract(clazz.getModifiers()) || clazz.isInterface()) continue;
                if (MissionType.class.isAssignableFrom(clazz)) {
                    try {
                        List<MissionType> types = getMissionTypes(clazz);
                        if (!types.isEmpty()) registry.registerType(types.getFirst());
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
