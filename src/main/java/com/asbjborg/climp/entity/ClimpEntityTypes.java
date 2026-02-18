package com.asbjborg.climp.entity;

import com.asbjborg.climp.ClimpMod;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ClimpEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, ClimpMod.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<ClimpEntity>> CLIMP = ENTITY_TYPES.register(
            "climp",
            () -> EntityType.Builder.of(ClimpEntity::new, MobCategory.CREATURE)
                    .sized(1.1F, 1.6F)
                    .build("climp:climp"));

    private ClimpEntityTypes() {
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }
}
