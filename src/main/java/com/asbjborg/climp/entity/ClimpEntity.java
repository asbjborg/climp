package com.asbjborg.climp.entity;

import java.util.EnumSet;
import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
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
import net.minecraft.server.level.ServerPlayer;

/**
 * Climp entity with MVP follow behavior.
 */
public class ClimpEntity extends PathfinderMob {
    private static final String[] IDLE_COMMENTS = new String[] {
            "I am not lost. I am orbiting.",
            "That rock looked valuable. Emotionally.",
            "I could optimize this route, but then we would miss the ambiance.",
            "I am moderately helpful. Structurally flexible."
    };
    private static final String[] HIT_COMMENTS = new String[] {
            "Rude. I am decorative and emotionally available.",
            "Ow. That was my best angle.",
            "Violence noted. Friendship pending.",
            "If this is a trust exercise, I am failing it."
    };

    private int commentCooldownTicks = 20 * 20;
    private int hitCommentCooldownTicks = 0;

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
        if (this.hitCommentCooldownTicks > 0) {
            this.hitCommentCooldownTicks--;
        }
        this.tryIdleComment();
    }

    private void tryIdleComment() {
        if (this.level().isClientSide || this.commentCooldownTicks-- > 0) {
            return;
        }

        Player nearest = this.level().getNearestPlayer(this, 7.0D);
        if (!(nearest instanceof ServerPlayer player) || nearest.isSpectator()) {
            return;
        }

        // Keep lines rare and mostly when Climp is settled near the player.
        boolean climpIsSettled = this.getNavigation().isDone() && this.distanceToSqr(nearest) < 25.0D;
        if (!climpIsSettled || this.random.nextInt(16) != 0) {
            this.commentCooldownTicks = 20 * 8;
            return;
        }

        String line = IDLE_COMMENTS[this.random.nextInt(IDLE_COMMENTS.length)];
        player.sendSystemMessage(Component.literal("Climp: " + line));
        this.commentCooldownTicks = 20 * 35;
    }

    @Override
    public boolean isPushable() {
        // Companion should not physically shove players while idling/following.
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean didHurt = super.hurt(source, amount);
        if (!didHurt || this.level().isClientSide || this.hitCommentCooldownTicks > 0) {
            return didHurt;
        }

        if (source.getEntity() instanceof ServerPlayer player) {
            String line = HIT_COMMENTS[this.random.nextInt(HIT_COMMENTS.length)];
            player.sendSystemMessage(Component.literal("Climp: " + line));
            this.hitCommentCooldownTicks = 20 * 8;
            // Delay idle chatter for a bit after a hit reaction.
            this.commentCooldownTicks = Math.max(this.commentCooldownTicks, 20 * 10);
        }

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
