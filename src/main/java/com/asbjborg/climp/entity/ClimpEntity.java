package com.asbjborg.climp.entity;

import java.util.EnumSet;
import javax.annotation.Nullable;

import com.asbjborg.climp.speech.ClimpSpeechManager;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Climp entity with MVP follow behavior.
 */
public class ClimpEntity extends PathfinderMob {
    private final ClimpSpeechManager speechManager = new ClimpSpeechManager();

    protected ClimpEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new FollowNearestPlayerGoal(this, 1.12D, 3.0F, 28.0F));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.9D));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.speechManager.tick(this);
    }

    @Override
    public boolean isPushable() {
        // Companion should not physically shove players while idling/following.
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean didHurt = super.hurt(source, amount);
        this.speechManager.onHit(this, source, didHurt);
        return didHurt;
    }

    private static final class FollowNearestPlayerGoal extends Goal {
        private final ClimpEntity climp;
        private final double speedModifier;
        private final float stopDistance;
        private final float maxDistance;
        @Nullable
        private Player targetPlayer;
        private int recalcPathTicks;

        private FollowNearestPlayerGoal(ClimpEntity climp, double speedModifier, float stopDistance, float maxDistance) {
            this.climp = climp;
            this.speedModifier = speedModifier;
            this.stopDistance = stopDistance;
            this.maxDistance = maxDistance;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            Player nearest = this.climp.level().getNearestPlayer(this.climp, this.maxDistance);
            if (nearest == null || nearest.isSpectator()) {
                return false;
            }

            this.targetPlayer = nearest;
            return this.climp.distanceToSqr(nearest) > (double) (this.stopDistance * this.stopDistance);
        }

        @Override
        public boolean canContinueToUse() {
            if (this.targetPlayer == null || !this.targetPlayer.isAlive() || this.targetPlayer.isSpectator()) {
                return false;
            }

            double distanceSqr = this.climp.distanceToSqr(this.targetPlayer);
            return distanceSqr > (double) (this.stopDistance * this.stopDistance)
                    && distanceSqr < (double) (this.maxDistance * this.maxDistance);
        }

        @Override
        public void stop() {
            this.targetPlayer = null;
            this.climp.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (this.targetPlayer == null) {
                return;
            }

            this.climp.getLookControl().setLookAt(this.targetPlayer, 18.0F, this.climp.getMaxHeadXRot());
            if (--this.recalcPathTicks <= 0) {
                this.recalcPathTicks = this.adjustedTickDelay(10);
                this.climp.getNavigation().moveTo(this.targetPlayer, this.speedModifier);
            }
        }
    }
}
