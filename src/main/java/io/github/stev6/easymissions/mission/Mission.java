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

package io.github.stev6.easymissions.mission;

import com.google.common.base.Preconditions;
import io.github.stev6.easymissions.EasyMissionsAPI;
import io.github.stev6.easymissions.config.data.MissionConfig;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * The heart of the plugin, this contains the serializable object holding all important matcher on the {@link org.bukkit.inventory.ItemStack}
 * associated with the mission
 * <p>
 * Direct changes to fields here (such as {@link #setProgress(int)} or {@link #incrementProgress(int)})will not be updated
 * unless you are using a modification method provided by the {@link EasyMissionsAPI}
 *
 * @see MissionPersistentDataType
 */

public class Mission {

    private final UUID uuid;
    private String configID;
    private int requirement;
    private boolean completed;
    private int progress;

    private Mission(
            @NotNull String configID,
            int requirement,
            boolean completed,
            int progress,
            @NotNull UUID uuid) {
        this.configID = configID;
        this.requirement = requirement;
        this.completed = completed;
        this.progress = progress;
        this.uuid = uuid;
    }

    @ApiStatus.Internal
    @NotNull
    public static Mission create(@NotNull String configID, int requirement) {
        return new Mission(configID, requirement, false, 0, UUID.randomUUID());
    }

    @ApiStatus.Internal
    @NotNull
    public static Mission recreate(
            @NotNull String configID,
            int requirement,
            boolean completed,
            int progress,
            @NotNull UUID uuid) {
        return new Mission(configID, requirement, completed, progress, uuid);
    }

    /**
     * Checks whether the {@link Mission} is completed or not
     *
     * @return {@code true} if it is completed {@code false} if it isn't
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Sets the {@link #completed} state of the {@link Mission}
     */
    public void setCompleted(boolean b) {
        this.completed = b;
    }

    /**
     * Gets the {@link #progress} of the {@link Mission}
     *
     * @return {@link #progress} ranging from 0 to {@link #requirement}
     */
    public int getProgress() {
        return progress;
    }

    /**
     * Sets the {@link #progress} to the given value
     * <p>
     * You should not be using this unless this is an admin command of some sort, or you want to change the progress
     * completely rather than increment
     * otherwise, see {@link #incrementProgress(int)}
     *
     * @param progress the new {@link #progress}
     * @implNote the progress will be clamped to the {@link #requirement} no matter what
     */
    public void setProgress(int progress) {
        this.progress = Math.max(0, Math.min(progress, this.requirement));
    }

    /**
     * Increments the {@link #progress} by a given value, caps at {@link #requirement}
     * Negative numbers are not allowed
     *
     * @param i number to increment {@link #progress} by
     */
    public void incrementProgress(int i) {
        Preconditions.checkArgument(i >= 0, "Cannot increment by negative numbers");
        setProgress(this.progress + i);
    }

    /**
     * Decrements the {@link #progress} by a given value, caps at {@code 0}
     * Negative numbers are not allowed
     *
     * @param i number to decrement {@link #progress} by
     */
    public void decrementProgress(int i) {
        Preconditions.checkArgument(i >= 0, "Cannot decrement by negative numbers");
        setProgress(this.progress - i);
    }

    /**
     * Gets the {@link #configID} of the {@link Mission}
     *
     * @return the {@link #configID}
     */
    @NotNull
    public String getConfigID() {
        return configID;
    }

    /**
     * Sets the {@link #configID} to a new one, should only be used for broken config migration/admin contexts
     * as it will change a lot of the mission's behaviour. See {@link MissionConfig}
     * to find out what will be changed
     *
     * @param configID the new {@link #configID}
     */
    public void setConfigID(@NotNull String configID) {
        this.configID = configID;
    }

    /**
     * Gets the {@link #uuid} of the {@link Mission}
     *
     * @return the {@link #uuid}
     */
    @NotNull
    public UUID getUUID() {
        return uuid;
    }

    /**
     * Gets the {@link #requirement} of the {@link Mission}
     *
     * @return the {@link #requirement}
     */
    public int getRequirement() {
        return requirement;
    }

    /**
     * Sets the {@link #requirement} to a new one, should only be used in admin/debug states as it mutates {@link #progress} if too low
     *
     * @param requirement the new requirement
     * @implNote {@link #progress} will be set to {@link #requirement} if the new requirement is less than the progress
     */
    public void setRequirement(int requirement) {
        this.requirement = Math.max(1, requirement);
        if (this.progress > this.requirement) {
            this.progress = this.requirement;
        }
    }
}
