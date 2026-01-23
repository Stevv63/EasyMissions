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

package io.github.stev6.easymissions.listener;

import io.github.stev6.easymissions.MissionManager;
import io.github.stev6.easymissions.context.impl.EnchantContext;
import io.github.stev6.easymissions.context.impl.ItemContext;
import io.github.stev6.easymissions.type.impl.Craft;
import io.github.stev6.easymissions.type.impl.Disenchant;
import io.github.stev6.easymissions.type.impl.Enchant;
import io.github.stev6.easymissions.type.impl.Repair;
import io.github.stev6.easymissions.util.ListenerUtils;
import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.github.stev6.easymissions.util.ListenerUtils.getAllEnchants;
import static io.github.stev6.easymissions.util.ListenerUtils.getEnchantLevels;

public record InventoryListener(MissionManager m) implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGrindstoneInventoryClick(InventoryClickEvent e) {
        if (isInvalidClick(e.getAction())) return;
        if (e.getClickedInventory() instanceof GrindstoneInventory inv) {
            if (e.getSlotType() != SlotType.RESULT) return;
            Player p = ((Player) e.getWhoClicked());
            ItemStack result = e.getCurrentItem();
            if (result == null) return;
            if (e.getClick().isShiftClick() && !ListenerUtils.canItemFit(p.getInventory(), result)) return;
            if (getEnchantLevels(inv.getUpperItem()) + getEnchantLevels(inv.getLowerItem()) <= 0) return;

            m.findAndModifyFirstMission(p, Disenchant.INSTANCE, new ItemContext(result), mission -> mission.incrementProgress(1));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAnvilInventoryClick(InventoryClickEvent e) {
        if (isInvalidClick(e.getAction())) return;
        if (e.getClickedInventory() instanceof AnvilInventory inv) {
            if (!(e.getWhoClicked() instanceof Player p)) return;
            ItemStack result = e.getCurrentItem();
            ItemStack input = inv.getFirstItem();
            if (e.getSlotType() != SlotType.RESULT) return;
            if (result == null || input == null) return;
            if (e.getClick().isShiftClick() && !ListenerUtils.canItemFit(p.getInventory(), result)) return;

            int restored = getItemDurability(result) - getItemDurability(input);


            if (restored > 0)
                m.findAndModifyFirstMission(p, Repair.INSTANCE, new ItemContext(input), mission -> mission.incrementProgress(restored));

            var oldEnchants = getAllEnchants(input);
            var enchants = getAllEnchants(result);

            var finalEnchants = enchants.entrySet().stream()
                    .filter(ench -> !oldEnchants.containsKey(ench.getKey()) || ench.getValue() > oldEnchants.get(ench.getKey()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            var ctx = new EnchantContext(finalEnchants, result, true);
            m.findAndModifyFirstMission(p, Enchant.INSTANCE, ctx, mission -> mission.incrementProgress(1));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraft(CraftItemEvent e) {
        if (e.getAction() == InventoryAction.NOTHING) return;
        if (e.getSlotType() != InventoryType.SlotType.RESULT) return;
        ItemStack recipeOutput = e.getCurrentItem();
        if (recipeOutput == null) return;
        if (!(e.getWhoClicked() instanceof Player p)) return;
        final int amount;

        if (e.getClick().isShiftClick())
            amount = calculateCraft(p.getInventory(), recipeOutput.getAmount(), recipeOutput, e.getInventory().getMatrix());
        else amount = recipeOutput.getAmount();

        if (amount <= 0) return;
        m.findAndModifyFirstMission(
                p,
                Craft.INSTANCE,
                new ItemContext(recipeOutput),
                mission -> mission.incrementProgress(amount));
    }

    private int calculateCraft(PlayerInventory inv, int output, ItemStack result, ItemStack... matrix) {
        int possible = output * Arrays.stream(matrix).filter(Objects::nonNull).mapToInt(ItemStack::getAmount).min().orElse(0);
        int availableSpace = ListenerUtils.getAvailableSpace(inv, result);
        int crafts = (availableSpace + output - 1) / output;
        int calculatedAmount = crafts * output;
        return Math.min(possible, calculatedAmount);
    }


    private boolean isInvalidClick(InventoryAction c) {
        return switch (c) {
            case PICKUP_ALL, PICKUP_HALF, MOVE_TO_OTHER_INVENTORY, HOTBAR_SWAP, DROP_ONE_SLOT, DROP_ALL_SLOT -> false;
            default -> true;
        };
    }


    @SuppressWarnings("UnstableApiUsage")
    private int getItemDurability(ItemStack i) {
        var dmg = i.getData(DataComponentTypes.DAMAGE);
        var maxDmg = i.getData(DataComponentTypes.MAX_DAMAGE);
        if (dmg == null || maxDmg == null) return 0;
        return maxDmg - dmg;
    }
}
