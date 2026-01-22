package io.github.stev6.easymissions.context.impl;

import io.github.stev6.easymissions.context.MissionContext;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public record EnchantContext(Map<Enchantment, Integer> enchants, ItemStack enchantItem,
                             boolean anvil) implements MissionContext {
}
