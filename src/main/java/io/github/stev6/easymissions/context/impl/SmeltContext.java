package io.github.stev6.easymissions.context.impl;

import io.github.stev6.easymissions.context.MissionContext;
import org.bukkit.Material;

public record SmeltContext(Material type) implements MissionContext {
}
