package io.github.stev6.easymissions.type;

import io.github.stev6.easymissions.context.MissionContext;

public interface MissionTarget<C extends MissionContext> {
    boolean matches(C context);
}
