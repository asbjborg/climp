package com.asbjborg.climp.entity;

import java.util.EnumSet;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

import com.asbjborg.climp.speech.ClimpSpeechManager;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
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
    private static final double COMMAND_TASK_RANGE_SQR = 16.0D * 16.0D;
    private static final double COMMAND_TASK_REACH_BLOCKS = 3.0D;
    private static final double COMMAND_RETURN_REACH_SQR = 3.0D * 3.0D;
    private static final double COMMAND_APPROACH_FALLBACK_REACH_BLOCKS = 4.5D;
    private static final int COMMAND_BREAK_DURATION_TICKS = 40;
    private static final int COMMAND_TASK_COOLDOWN_TICKS = 20 * 3;
    private static final int COMMAND_STAGE_TIMEOUT_TICKS = 20 * 20;
    private static final int COMMAND_NO_PATH_FAIL_TICKS = 20 * 2;

    private final ClimpSpeechManager speechManager = new ClimpSpeechManager();
    @Nullable
    private BlockPos commandTargetPos;
    private final Deque<BlockPos> commandQueuedTargets = new ArrayDeque<>();
    @Nullable
    private UUID commandRequesterId;
    private CommandTaskStage commandTaskStage = CommandTaskStage.NONE;
    private int commandBreakTicksRemaining;
    private int commandStageTicks;
    private int commandNoPathTicks;
    private int commandCooldownTicks;
    private boolean commandTaskSucceeded;
    private boolean commandRecallRequested;
    private boolean commandScanChompMode;
    private int commandClusterAnchorY;
    private ClimpSpeechManager.TaskFailureReason commandTaskFailureReason = ClimpSpeechManager.TaskFailureReason.UNREACHABLE;

    protected ClimpEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new CommandTargetGoal(this, 1.18D));
        this.goalSelector.addGoal(2, new FollowNearestPlayerGoal(this, 1.12D, 3.0F, 28.0F));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.9D));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.speechManager.tick(this);
        if (this.commandCooldownTicks > 0) {
            this.commandCooldownTicks--;
        }
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

    public boolean assignLogTask(ServerPlayer requester, BlockPos targetPos) {
        return this.assignLogTask(requester, List.of(targetPos));
    }

    public boolean assignLogTask(ServerPlayer requester, List<BlockPos> targetPositions) {
        if (this.level().isClientSide || !this.canAcceptCommandTask()) {
            return false;
        }

        if (targetPositions.isEmpty()) {
            return false;
        }

        BlockPos targetPos = targetPositions.get(0).immutable();
        if (this.distanceToSqr(targetPos.getCenter()) > COMMAND_TASK_RANGE_SQR) {
            return false;
        }

        this.commandTargetPos = targetPos.immutable();
        this.commandQueuedTargets.clear();
        for (int i = 1; i < targetPositions.size(); i++) {
            this.commandQueuedTargets.addLast(targetPositions.get(i).immutable());
        }
        this.commandRequesterId = requester.getUUID();
        this.commandScanChompMode = targetPositions.size() > 1;
        this.commandClusterAnchorY = targetPos.getY();
        this.setCommandTaskStage(CommandTaskStage.TO_TARGET);
        this.commandBreakTicksRemaining = 0;
        this.commandTaskSucceeded = false;
        this.commandRecallRequested = false;
        this.commandTaskFailureReason = ClimpSpeechManager.TaskFailureReason.UNREACHABLE;
        this.speechManager.onTaskStart(this, requester);
        return true;
    }

    public boolean hasCommandTask() {
        return this.commandTaskStage != CommandTaskStage.NONE;
    }

    public boolean requestImmediateRecall(ServerPlayer requester) {
        if (this.level().isClientSide || this.commandTaskStage == CommandTaskStage.NONE) {
            return false;
        }

        this.commandRequesterId = requester.getUUID();
        this.commandRecallRequested = true;
        this.commandTaskSucceeded = false;
        this.commandTaskFailureReason = ClimpSpeechManager.TaskFailureReason.UNREACHABLE;
        this.setCommandTaskStage(CommandTaskStage.RETURNING);
        this.commandBreakTicksRemaining = 0;
        this.commandQueuedTargets.clear();
        this.commandScanChompMode = false;
        this.commandClusterAnchorY = 0;
        this.clearBreakProgress();
        this.getNavigation().stop();
        return true;
    }

    public boolean canAcceptCommandTask() {
        return this.commandTaskStage == CommandTaskStage.NONE && this.commandCooldownTicks <= 0;
    }

    public boolean isOnCommandCooldown() {
        return this.commandTaskStage == CommandTaskStage.NONE && this.commandCooldownTicks > 0;
    }

    @Nullable
    private BlockPos getCommandTargetPos() {
        return this.commandTargetPos;
    }

    private void completeCommandTask() {
        this.clearBreakProgress();

        ServerPlayer requester = this.getCommandRequester();
        if (!this.commandRecallRequested && requester != null && requester.level() == this.level()) {
            if (this.commandTaskSucceeded) {
                this.speechManager.onTaskComplete(this, requester);
            } else {
                this.speechManager.onTaskFailed(this, requester, this.commandTaskFailureReason);
            }
        }

        this.commandTargetPos = null;
        this.commandQueuedTargets.clear();
        this.commandRequesterId = null;
        this.commandTaskStage = CommandTaskStage.NONE;
        this.commandBreakTicksRemaining = 0;
        this.commandStageTicks = 0;
        this.commandTaskSucceeded = false;
        this.commandRecallRequested = false;
        this.commandScanChompMode = false;
        this.commandClusterAnchorY = 0;
        this.commandTaskFailureReason = ClimpSpeechManager.TaskFailureReason.UNREACHABLE;
        this.commandCooldownTicks = COMMAND_TASK_COOLDOWN_TICKS;
        this.getNavigation().stop();
    }

    private boolean isTargetLog() {
        return this.commandTargetPos != null && this.level().getBlockState(this.commandTargetPos).is(BlockTags.LOGS);
    }

    private void beginBreakingTarget() {
        this.setCommandTaskStage(CommandTaskStage.BREAKING);
        this.commandBreakTicksRemaining = COMMAND_BREAK_DURATION_TICKS;
        this.getNavigation().stop();
    }

    private void markReturningToRequester(boolean succeeded, ClimpSpeechManager.TaskFailureReason failureReason) {
        this.commandTaskSucceeded = succeeded;
        if (!succeeded) {
            this.commandTaskFailureReason = failureReason;
        }
        this.setCommandTaskStage(CommandTaskStage.RETURNING);
        this.commandBreakTicksRemaining = 0;
        this.clearBreakProgress();
    }

    private boolean advanceToNextQueuedTarget() {
        this.clearBreakProgress();
        while (!this.commandQueuedTargets.isEmpty()) {
            BlockPos nextTarget = this.commandQueuedTargets.removeFirst();
            if (this.level().getBlockState(nextTarget).is(BlockTags.LOGS)) {
                this.commandTargetPos = nextTarget;
                this.setCommandTaskStage(CommandTaskStage.TO_TARGET);
                this.commandBreakTicksRemaining = 0;
                return true;
            }
        }
        return false;
    }

    private double getEffectiveTaskReachSqr(BlockPos target) {
        return this.getScaledReachSqr(COMMAND_TASK_REACH_BLOCKS, target);
    }

    private double getEffectiveApproachFallbackReachSqr(BlockPos target) {
        return this.getScaledReachSqr(COMMAND_APPROACH_FALLBACK_REACH_BLOCKS, target);
    }

    private double getScaledReachSqr(double baseReachBlocks, BlockPos target) {
        if (!this.commandScanChompMode) {
            return baseReachBlocks * baseReachBlocks;
        }

        int extraReachByHeight = Math.max(0, target.getY() - this.commandClusterAnchorY);
        double scaledReachBlocks = baseReachBlocks + extraReachByHeight;
        return scaledReachBlocks * scaledReachBlocks;
    }

    private void setCommandTaskStage(CommandTaskStage stage) {
        this.commandTaskStage = stage;
        this.commandStageTicks = 0;
        this.commandNoPathTicks = 0;
    }

    private boolean incrementAndCheckStageTimeout() {
        this.commandStageTicks++;
        return this.commandStageTicks > COMMAND_STAGE_TIMEOUT_TICKS;
    }

    @Nullable
    private ServerPlayer getCommandRequester() {
        if (this.commandRequesterId == null || this.level().getServer() == null) {
            return null;
        }
        return this.level().getServer().getPlayerList().getPlayer(this.commandRequesterId);
    }

    private void clearBreakProgress() {
        if (this.commandTargetPos != null && this.level() instanceof ServerLevel serverLevel) {
            serverLevel.destroyBlockProgress(this.getId(), this.commandTargetPos, -1);
        }
    }

    private enum CommandTaskStage {
        NONE,
        TO_TARGET,
        BREAKING,
        RETURNING
    }

    private static final class CommandTargetGoal extends Goal {
        private final ClimpEntity climp;
        private final double speedModifier;
        private int recalcPathTicks;

        private CommandTargetGoal(ClimpEntity climp, double speedModifier) {
            this.climp = climp;
            this.speedModifier = speedModifier;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return this.climp.hasCommandTask();
        }

        @Override
        public boolean canContinueToUse() {
            return this.climp.hasCommandTask();
        }

        @Override
        public void stop() {
            this.climp.clearBreakProgress();
        }

        @Override
        public void tick() {
            switch (this.climp.commandTaskStage) {
                case TO_TARGET -> tickToTarget();
                case BREAKING -> tickBreaking();
                case RETURNING -> tickReturning();
                case NONE -> {
                    // No-op.
                }
            }
        }

        private void tickToTarget() {
            BlockPos target = this.climp.getCommandTargetPos();
            if (target == null) {
                this.climp.completeCommandTask();
                return;
            }

            if (!this.climp.isTargetLog()) {
                if (!this.climp.advanceToNextQueuedTarget()) {
                    this.climp.markReturningToRequester(true, ClimpSpeechManager.TaskFailureReason.UNREACHABLE);
                }
                return;
            }

            this.climp.getLookControl().setLookAt(target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D);
            if (--this.recalcPathTicks <= 0) {
                this.recalcPathTicks = this.adjustedTickDelay(8);
                this.climp.getNavigation().moveTo(target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D, this.speedModifier);
            }

            double targetDistanceSqr = this.climp.distanceToSqr(target.getCenter());
            double effectiveTaskReachSqr = this.climp.getEffectiveTaskReachSqr(target);
            double effectiveFallbackReachSqr = this.climp.getEffectiveApproachFallbackReachSqr(target);
            if (targetDistanceSqr <= effectiveTaskReachSqr
                    || (this.climp.getNavigation().isDone() && targetDistanceSqr <= effectiveFallbackReachSqr)) {
                this.climp.beginBreakingTarget();
                return;
            }

            if (this.climp.getNavigation().isDone() && targetDistanceSqr > effectiveFallbackReachSqr) {
                this.climp.commandNoPathTicks++;
                if (this.climp.commandNoPathTicks >= COMMAND_NO_PATH_FAIL_TICKS) {
                    this.climp.markReturningToRequester(false, ClimpSpeechManager.TaskFailureReason.UNREACHABLE);
                    return;
                }
            } else {
                this.climp.commandNoPathTicks = 0;
            }

            if (this.climp.incrementAndCheckStageTimeout()) {
                this.climp.markReturningToRequester(false, ClimpSpeechManager.TaskFailureReason.UNREACHABLE);
            }
        }

        private void tickBreaking() {
            BlockPos target = this.climp.getCommandTargetPos();
            if (target == null) {
                this.climp.completeCommandTask();
                return;
            }

            if (!this.climp.isTargetLog()) {
                if (!this.climp.advanceToNextQueuedTarget()) {
                    this.climp.markReturningToRequester(true, ClimpSpeechManager.TaskFailureReason.UNREACHABLE);
                }
                return;
            }

            this.climp.getLookControl().setLookAt(target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D);
            if (this.climp.level() instanceof ServerLevel serverLevel) {
                int elapsed = COMMAND_BREAK_DURATION_TICKS - this.climp.commandBreakTicksRemaining;
                int progress = Math.min(9, (elapsed * 10) / COMMAND_BREAK_DURATION_TICKS);
                serverLevel.destroyBlockProgress(this.climp.getId(), target, progress);
            }

            this.climp.commandBreakTicksRemaining--;
            if (this.climp.commandBreakTicksRemaining <= 0) {
                if (this.climp.level() instanceof ServerLevel serverLevel && this.climp.isTargetLog()) {
                    serverLevel.destroyBlock(target, true, this.climp);
                }
                if (!this.climp.advanceToNextQueuedTarget()) {
                    this.climp.markReturningToRequester(true, ClimpSpeechManager.TaskFailureReason.UNREACHABLE);
                }
                return;
            }

            if (this.climp.incrementAndCheckStageTimeout()) {
                this.climp.markReturningToRequester(false, ClimpSpeechManager.TaskFailureReason.UNREACHABLE);
            }
        }

        private void tickReturning() {
            ServerPlayer requester = this.climp.getCommandRequester();
            if (requester == null || requester.level() != this.climp.level() || !requester.isAlive()) {
                this.climp.completeCommandTask();
                return;
            }

            this.climp.getLookControl().setLookAt(requester, 18.0F, this.climp.getMaxHeadXRot());
            if (--this.recalcPathTicks <= 0) {
                this.recalcPathTicks = this.adjustedTickDelay(8);
                this.climp.getNavigation().moveTo(requester, this.speedModifier);
            }

            if (this.climp.distanceToSqr(requester) <= COMMAND_RETURN_REACH_SQR) {
                this.climp.completeCommandTask();
                return;
            }

            if (this.climp.incrementAndCheckStageTimeout()) {
                this.climp.completeCommandTask();
            }
        }
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
