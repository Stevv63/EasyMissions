package io.github.stev6.easymissions.context;

import io.github.stev6.easymissions.EasyMissionsAPI;
import io.github.stev6.easymissions.MissionManager;
import io.github.stev6.easymissions.type.MissionTarget;
import io.github.stev6.easymissions.type.MissionType;
import io.github.stev6.easymissions.type.TargetedMissionType;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

/**
 * Marker interface for contexts that will be passed in progression methods.
 * <p>
 * A Context object is created <b>when an event is fired</b> (e.g., BlockBreakEvent).
 * It wraps the necessary objects (Blocks, Entities, ItemStacks) so that
 * the {@link MissionTarget} can check if the mission requirements are met.
 *
 * <p>If your called event has different location details and Player/Entity locations don't suffice, implement {@link Locatable}
 * and its method.
 *
 * <h3>Example Implementation</h3>
 * <pre>{@code
 * public record BlockContext(Block block) implements MissionContext, Locatable {
 *
 *     @Override
 *     public Location getLocation() {
 *         return block.getLocation();
 *     }
 * }
 * }</pre>
 *
 * @see MissionManager#findAndModifyFirstMission(Player, MissionType, MissionContext, Consumer)
 * @see EasyMissionsAPI#findAndProgressMission(Player, TargetedMissionType, MissionContext, Consumer)
 * @see Locatable
 */
public interface MissionContext {
}
