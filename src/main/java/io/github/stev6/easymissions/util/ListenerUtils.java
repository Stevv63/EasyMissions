/*
 * EasyMissions – A Minecraft missions plugin.
 * Copyright (C) 2025 Stev6
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.stev6.easymissions.util;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemEnchantments;
import io.papermc.paper.datacomponent.item.PotionContents;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class ListenerUtils {

    public static final int SUGARCANE_CACTUS_MAX = 6;
    public static final int BAMBOO_MAX = 16;
    public static final int CHORUS_MAX = 64; // just a safe limit

    public static int getEnchantLevels(ItemStack i) {
        if (i == null) return 0;
        return getAllEnchants(i).values().stream().mapToInt(Integer::intValue).sum();
    }

    public static PotionType getPotionTypeOrNull(ItemStack i) {
        if (i == null) return null;
        PotionContents data = i.getData(DataComponentTypes.POTION_CONTENTS);
        return data != null ? data.potion() : null;
    }

    public static boolean isAnnoyingAgeAbleBlock(Block b) {
        return switch (b.getType()) {
            case CACTUS, SUGAR_CANE, BAMBOO -> true;
            default -> false;
        };
    }

    public static boolean canItemFit(PlayerInventory inv, ItemStack i) {
        return getAvailableSpace(inv, i) >= i.getAmount();
    }

    public static int getAvailableSpace(PlayerInventory inv, ItemStack item) {
        int space = 0;
        for (int idx = 0; idx < 36; idx++) {
            ItemStack i = inv.getItem(idx);
            if (i == null) space += item.getMaxStackSize();
            else if (i.isSimilar(item)) space += i.getMaxStackSize() - i.getAmount();
        }
        return space;
    }

    public static boolean isChorus(Block block) {
        return switch (block.getType()) {
            case CHORUS_FLOWER, CHORUS_PLANT -> true;
            default -> false;
        };
    }

    public static List<PotionEffectType> getAllPotionEffects(ItemStack i) {
        if (i == null) return null;
        PotionContents data = i.getData(DataComponentTypes.POTION_CONTENTS);
        return data != null ? data.allEffects().stream().map(PotionEffect::getType).toList() : null;
    }

    public static Map<Enchantment, Integer> getAllEnchants(ItemStack i) {
        if (i == null) return Collections.emptyMap();
        Map<Enchantment, Integer> map = new HashMap<>(i.getEnchantments());

        ItemEnchantments stored = i.getData(DataComponentTypes.STORED_ENCHANTMENTS);
        if (stored != null) map.putAll(stored.enchantments());

        return map;
    }
}

