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

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Another part of the core, It's the {@link Mission}'s {@link PersistentDataType} to serialize/deserialize the {@link Mission}
 */

@ApiStatus.Internal
public class MissionPersistentDataType implements PersistentDataType<byte[], Mission> {

    public static final MissionPersistentDataType INSTANCE = new MissionPersistentDataType();

    private MissionPersistentDataType() {}

    @Override
    public @NotNull Class<byte[]> getPrimitiveType() {
        return byte[].class;
    }

    @Override
    public @NotNull Class<Mission> getComplexType() {
        return Mission.class;
    }

    @Override
    public byte @NotNull [] toPrimitive(@NotNull Mission complex, @NotNull PersistentDataAdapterContext context) {


        byte[] configID = complex.getConfigID().getBytes(StandardCharsets.UTF_8);
        UUID uuid = complex.getUUID();
        int size = Integer.BYTES * 2 + Long.BYTES * 2 + 1 + configID.length;

        ByteBuffer bb = ByteBuffer.allocate(size);
        bb.putInt(complex.getRequirement());
        bb.putInt(complex.getProgress());
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        bb.put((byte) (complex.isCompleted() ? 1 : 0));
        bb.put(configID);

        return bb.array();
    }

    @Override
    public @NotNull Mission fromPrimitive(
            byte @NotNull [] primitive, @NotNull PersistentDataAdapterContext context) {
        ByteBuffer bb = ByteBuffer.wrap(primitive);

        // meow used to be here

        int requirement = bb.getInt();
        int progress = bb.getInt();
        long mostSignificant = bb.getLong();
        long leastSignificant = bb.getLong();
        UUID uuid = new UUID(mostSignificant, leastSignificant);
        boolean completed = bb.get() != 0;
        byte[] configIDBytes = new byte[bb.remaining()];
        bb.get(configIDBytes);
        String configID = new String(configIDBytes, StandardCharsets.UTF_8);

        return Mission.recreate(configID, requirement, completed, progress, uuid);
    }
}
