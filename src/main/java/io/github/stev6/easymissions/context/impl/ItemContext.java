package io.github.stev6.easymissions.context.impl;

import io.github.stev6.easymissions.context.MissionContext;
import org.bukkit.inventory.ItemStack;

public record ItemContext(ItemStack item) implements MissionContext {
}
