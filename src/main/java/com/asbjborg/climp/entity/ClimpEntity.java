package com.asbjborg.climp.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;

/**
 * Initial Climp mob shell. AI goals/behavior are added in later steps.
 */
public class ClimpEntity extends PathfinderMob {
    protected ClimpEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }
}
