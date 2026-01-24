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
package io.github.stev6.easymissions;

import io.github.stev6.easymissions.config.ConfigManager;
import io.github.stev6.easymissions.config.data.MissionConfig;
import io.github.stev6.easymissions.context.MissionContext;
import io.github.stev6.easymissions.event.MissionClaimEvent;
import io.github.stev6.easymissions.event.MissionProgressEvent;
import io.github.stev6.easymissions.mission.Mission;
import io.github.stev6.easymissions.mission.MissionPersistentDataType;
import io.github.stev6.easymissions.option.MissionOption;
import io.github.stev6.easymissions.type.MissionType;
import io.github.stev6.easymissions.type.TargetedMissionType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Stream;

@ApiStatus.Internal
public class MissionManager {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private final ConfigManager configManager;
    private final NamespacedKey dataKey;
    private final NamespacedKey invalidKey;
    private final EasyMissions plugin;

    public MissionManager(EasyMissions plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.dataKey = new NamespacedKey(plugin, "mission_data");
        this.invalidKey = new NamespacedKey(plugin, "invalid_config");
    }

    @SuppressWarnings("UnstableApiUsage")
    public ItemStack createMissionItem(MissionConfig config) {
        int req = Math.max(1, config.requirementRange().random());
        Mission m = Mission.create(config.key(), req);
        ItemStack i = new ItemStack(config.itemMaterial());

        i.editPersistentDataContainer(pdc -> pdc.set(dataKey, MissionPersistentDataType.INSTANCE, m));

        TagResolver tags = getMissionTags(m);
        MiniMessage mm = MINI_MESSAGE;

        List<Component> lore = config.lore().stream().map(line -> mm.deserialize(line, tags)).toList();
        Component displayName = mm.deserialize(config.name(), tags);

        i.unsetData(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        i.unsetData(DataComponentTypes.ENCHANTABLE);
        i.unsetData(DataComponentTypes.REPAIRABLE);
        i.unsetData(DataComponentTypes.GLIDER);
        i.unsetData(DataComponentTypes.TOOL);
        i.unsetData(DataComponentTypes.WEAPON);
        i.unsetData(DataComponentTypes.BLOCKS_ATTACKS);
        i.unsetData(DataComponentTypes.EQUIPPABLE);
        i.unsetData(DataComponentTypes.CONSUMABLE);
        i.setData(DataComponentTypes.UNBREAKABLE);
        i.setData(DataComponentTypes.REPAIR_COST, Integer.MAX_VALUE);
        i.setData(DataComponentTypes.MAX_STACK_SIZE, 1);
        i.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        i.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().addHiddenComponents(
                DataComponentTypes.DAMAGE,
                DataComponentTypes.UNBREAKABLE,
                DataComponentTypes.TOOL).build());

        i.setData(DataComponentTypes.RARITY, config.itemRarity());
        i.setData(DataComponentTypes.ITEM_NAME, Component.text("Easy Missions mission"));
        i.setData(DataComponentTypes.CUSTOM_NAME, displayName);
        var optionalModel = m.isCompleted() ? config.completedItemModel() : config.itemModel();
        optionalModel.ifPresent(key -> i.setData(DataComponentTypes.ITEM_MODEL, key));
        i.setData(DataComponentTypes.LORE, ItemLore.lore().lines(lore).build());

        return i;
    }

    @Nullable
    public Mission getMissionOrNull(ItemStack i) {
        if (i == null) return null;
        return i.getPersistentDataContainer().get(dataKey, MissionPersistentDataType.INSTANCE);
    }

    public boolean isMission(ItemStack i) {
        if (i == null) return false;
        return i.getPersistentDataContainer().has(dataKey);
    }

    public void findAndModifyFirstMission(Player p, MissionType type, Consumer<Mission> doThing) {
        findAndModifyFirstMission(p, type, null, doThing);
    }

    public <C extends MissionContext> void findAndModifyFirstMission(Player p, MissionType type, C ctx, Consumer<Mission> doThing) {
        for (int idx = 0; idx < p.getInventory().getSize(); idx++) {
            if (idx == 36) idx = 40; // skip to offhand
            ItemStack i = p.getInventory().getItem(idx);
            if (i == null) continue;
            Mission m = getMissionOrNull(i);
            if (m == null || m.isCompleted()) continue;
            MissionConfig config = configManager.getMissions().get(m.getConfigID());

            if (config == null) {
                handleBrokenMission(i, m.getConfigID());
                continue;
            }

            try {
                if (config.type() != type) continue;
                if (config.blacklistedWorlds().contains(p.getWorld().getUID())) continue;

                if (type instanceof TargetedMissionType<?, ?> targetedType && config.data().isPresent()
                        && !targetedType.matchesRaw(config.data().get(), ctx)) continue;

                boolean passedConditions = true;

                for (MissionOption option : config.options()) {
                    if (!option.check(p, m, i, ctx)) {
                        passedConditions = false;
                        break;
                    }
                }

                if (!passedConditions) continue;

                int oldProgress = m.getProgress();
                boolean oldCompleted = m.isCompleted();

                doThing.accept(m);

                MissionProgressEvent event = new MissionProgressEvent(p, m, i, oldProgress, m.getProgress());
                if (!event.callEvent()) {
                    m.setProgress(oldProgress);
                    return;
                }

                m.setProgress(event.getNewProgress());
                if (m.getProgress() >= m.getRequirement()) m.setCompleted(true);

                if (m.getProgress() != oldProgress || m.isCompleted() != oldCompleted) {
                    updateMissionWithDataComponents(i, m);
                }

                return;
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "An unexpected error occurred while processing mission '" + config.key() + "' for player " + p.getName(), e);
            }
        }
    }

    @Nullable
    public MissionConfig weightedRandomMission(@NotNull Map<String, Integer> categoryWeight) {
        int total = 0;
        for (Integer i : categoryWeight.values()) if (i > 0) total += i;
        if (total <= 0) return null;
        int rand = ThreadLocalRandom.current().nextInt(total);
        int gained = 0;

        for (var category : categoryWeight.entrySet()) {
            Integer i = category.getValue();
            if (i <= 0) continue;
            gained += i;
            if (gained > rand) return categoryRandomMission(category.getKey());
        }
        return null;
    }

    @Nullable
    public MissionConfig categoryRandomMission(@NotNull String category) {
        List<MissionConfig> missions = configManager.getMissions().values().stream()
                .filter(m -> m.category().equalsIgnoreCase(category))
                .toList();

        if (missions.isEmpty()) return null;
        return missions.get(ThreadLocalRandom.current().nextInt(missions.size()));
    }

    @SuppressWarnings("UnstableApiUsage")
    public void updateMissionWithDataComponents(@NotNull ItemStack i, @NotNull Mission m) {
        MissionConfig config = getMissionConfigOrNull(m);
        if (config == null) return;
        i.editPersistentDataContainer(pdc -> {
            pdc.remove(invalidKey);
            pdc.set(dataKey, MissionPersistentDataType.INSTANCE, m);
        });

        TagResolver tags = getMissionTags(m);
        MiniMessage mm = MINI_MESSAGE;

        Component displayName = mm.deserialize(
                m.isCompleted() ? config.completedName() : config.name(),
                tags);

        List<Component> lore = (m.isCompleted() ? config.completedLore() : config.lore())
                .stream()
                .map(line -> mm.deserialize(line, tags))
                .toList();

        i.setData(DataComponentTypes.CUSTOM_NAME, displayName);
        i.setData(DataComponentTypes.LORE, ItemLore.lore().lines(lore).build());

        var optionalModel = m.isCompleted() ? config.completedItemModel() : config.itemModel();
        optionalModel.ifPresentOrElse(
                key -> i.setData(DataComponentTypes.ITEM_MODEL, key),
                () -> i.setData(DataComponentTypes.ITEM_MODEL, i.getType().getKey())
        );
    }

    public void giveRewards(ItemStack i, Player p) {
        Mission mission = getMissionOrNull(i);
        if (mission == null) return;
        MissionConfig config = getMissionConfigOrNull(mission);
        if (config == null) return;

        MissionClaimEvent event = new MissionClaimEvent(p, mission, i, config.rewards());
        if (!event.callEvent()) return;

        p.getInventory().removeItemAnySlot(i);

        for (String reward : config.rewards()) {
            String command = reward.replace("<player>", p.getName());
            if (command.startsWith("say ")) p.sendRichMessage(command.substring(4).trim());
            else plugin.runCommand(command, Bukkit.getConsoleSender());
        }
    }

    public boolean editMission(@NotNull ItemStack i, @NotNull Consumer<Mission> doThing) {
        Mission m = getMissionOrNull(i);
        if (m == null) return false;

        boolean oldCompleted = m.isCompleted();
        doThing.accept(m);
        if (oldCompleted == m.isCompleted() && m.getProgress() >= m.getRequirement()) m.setCompleted(true);
        if (oldCompleted == m.isCompleted() && m.getProgress() < m.getRequirement() && m.isCompleted())
            m.setCompleted(false);

        updateMissionWithDataComponents(i, m);
        return true;
    }

    public TagResolver getMissionTags(@NotNull Mission m) {
        int percentage = (int) ((double) m.getProgress() / m.getRequirement() * 100);
        MissionConfig c = getMissionConfigOrNull(m);

        return TagResolver.resolver(
                Placeholder.unparsed("uuid", m.getUUID().toString()),
                Placeholder.unparsed("type", c != null ? c.type().id() : "Unknown"),
                Placeholder.unparsed("progress", String.valueOf(m.getProgress())),
                Placeholder.unparsed("requirement", String.valueOf(m.getRequirement())),
                Placeholder.unparsed("category", c != null ? c.category() : "Unknown"),
                Placeholder.unparsed("task", c != null ? c.taskDescription() : ""),
                Placeholder.unparsed("percentage", String.valueOf(percentage)),
                Placeholder.unparsed("config_id", m.getConfigID()),
                Placeholder.unparsed("completed", String.valueOf(m.isCompleted()))
        );
    }

    @SuppressWarnings("UnstableApiUsage")
    private void handleBrokenMission(ItemStack i, String id) {
        if (i.getPersistentDataContainer().has(invalidKey, PersistentDataType.BYTE)) return;
        if (getMissionOrNull(i) == null) return;
        i.editPersistentDataContainer(pdc -> pdc.set(invalidKey, PersistentDataType.BYTE, (byte) 1));
        plugin.getLogger().severe("Config entry \"" + id + "\" is missing/invalid, please check your config if this is not intentional!");
        List<Component> lore = Stream.of("<red>MISSION HAS INVALID CONFIG ID: </red>" + id).map(MINI_MESSAGE::deserialize).toList();
        i.setData(DataComponentTypes.LORE, ItemLore.lore().addLines(lore).build());
        i.setData(DataComponentTypes.CUSTOM_NAME, MINI_MESSAGE.deserialize("BROKEN MISSION"));
    }

    @Nullable
    public MissionConfig getMissionConfigOrNull(@NotNull Mission m) {
        return configManager.getMissions().get(m.getConfigID());
    }
}
