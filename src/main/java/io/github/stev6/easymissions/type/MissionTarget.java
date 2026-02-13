package io.github.stev6.easymissions.type;

import io.github.stev6.easymissions.config.data.MissionConfig;
import io.github.stev6.easymissions.context.MissionContext;

/**
 * Represents the <b>data</b> for a specific {@link MissionConfig}.
 * <p>
 * This object is created <b>when</b> a mission config is being parsed. It holds the requirements for it
 * (e.g., "Must break DIAMOND_ORE") to check against the context passed from an event.
 *
 * <h3>Example Implementation</h3>
 * <pre>{@code
 * public record BreakData(Set<Material> allowedMaterials) implements MissionTarget<BlockContext> {
 *
 *     @Override
 *     public boolean matches(BlockContext context) {
 *         // Check if the block broken (context) matches the config (allowedMaterials)
 *         return allowedMaterials.contains(context.block().getType());
 *     }
 * }
 * }</pre>
 * @param <C> The {@link MissionContext} this target validates.
 */
public interface MissionTarget<C extends MissionContext> {
    boolean matches(C context);
}
