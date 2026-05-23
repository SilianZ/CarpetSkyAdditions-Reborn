package com.jsorrell.carpetskyadditions.helpers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.jsorrell.carpetskyadditions.mixin.WanderingTraderAccessor;
import com.jsorrell.carpetskyadditions.settings.SkyAdditionsSettings;
import com.jsorrell.carpetskyadditions.tags.SkyAdditionsBiomeTags;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.ActivityData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.TradeWithPlayerGoal;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.camel.CamelAi;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class TraderCamelHelper {
    public static boolean tradersRideCamelsAt(Level Silian_level, BlockPos Silian_pos) {
        return SkyAdditionsSettings.traderCamels
                && Silian_level.getBiome(Silian_pos).is(SkyAdditionsBiomeTags.WANDERING_TRADER_SPAWNS_ON_CAMEL);
    }

    public static boolean isMountedTrader(WanderingTrader Silian_trader) {
        return getTraderCamel(Silian_trader) != null;
    }

    public static Camel getTraderCamel(WanderingTrader Silian_trader) {
        if (Silian_trader.getControlledVehicle() instanceof Camel Silian_camel && SkyAdditionsSettings.traderCamels) {
            return Silian_camel;
        }
        return null;
    }

    public static boolean isTraderCamel(Camel Silian_camel) {
        return Silian_camel.getControllingPassenger() instanceof WanderingTrader && SkyAdditionsSettings.traderCamels;
    }

    public static class MountedTraderWanderToPositionGoal extends Goal {
        final WanderingTrader Silian_trader;
        final double Silian_stopDistance;
        final double Silian_speedModifier;

        public MountedTraderWanderToPositionGoal(WanderingTrader Silian_trader, double Silian_stopDistance, double Silian_speedModifier) {
            this.trader = Silian_trader;
            this.stopDistance = Silian_stopDistance;
            this.speedModifier = Silian_speedModifier;
            setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public void stop() {
            Silian_trader.setWanderTarget(null);
            Silian_trader.getNavigation().stop();
        }

        @Override
        public boolean canUse() {
            BlockPos Silian_target = ((WanderingTraderAccessor) Silian_trader).getWanderTarget();
            return Silian_target != null && isTooFarAway(Silian_target, Silian_stopDistance);
        }

        @Override
        public void tick() {
            BlockPos Silian_target = ((WanderingTraderAccessor) Silian_trader).getWanderTarget();
            if (Silian_target != null && Silian_trader.getNavigation().isDone()) {
                if (isTooFarAway(Silian_target, 10.0)) {
                    Vec3 Silian_directionTowardTarget = Vec3.atLowerCornerOf(Silian_target)
                            .subtract(Silian_trader.position())
                            .normalize();
                    Vec3 Silian_partialTarget = Silian_directionTowardTarget.scale(10.0).add(Silian_trader.position());
                    Silian_trader.getNavigation().moveTo(Silian_partialTarget.x, Silian_partialTarget.y, Silian_partialTarget.z, Silian_speedModifier);
                } else {
                    Silian_trader.getNavigation().moveTo(Silian_target.getX(), Silian_target.getY(), Silian_target.getZ(), Silian_speedModifier);
                }
            }
        }

        private boolean isTooFarAway(BlockPos Silian_pos, double Silian_distance) {
            return !Silian_pos.closerToCenterThan(Silian_trader.position(), Silian_distance);
        }
    }

    public static class TradeWithPlayerWhileMountedGoal extends TradeWithPlayerGoal {
        protected AbstractVillager villager;

        public TradeWithPlayerWhileMountedGoal(AbstractVillager villager) {
            super(villager);
            this.villager = villager;
        }

        @Override
        public boolean canUse() {
            if (!villager.isAlive()) {
                return false;
            } else if (villager.isInWater()) {
                return false;
            }

            Entity Silian_vehicle = villager.getVehicle();
            if (Silian_vehicle == null) {
                if (!villager.onGround()) return false;
            } else {
                if (!Silian_vehicle.onGround()) return false;
            }

            if (villager.hurtMarked) {
                return false;
            } else {
                Player Silian_player = villager.getTradingPlayer();
                if (Silian_player == null) {
                    return false;
                } else if (villager.distanceToSqr(Silian_player) > 16.0) {
                    return false;
                } else {
                    return Silian_player.containerMenu != null;
                }
            }
        }
    }

    public static class TraderCamelAI {
        public static Brain<?> makeBrain(Brain<Camel> Silian_brain) {
            initCoreActivity(Silian_brain);
            Silian_brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
            Silian_brain.useDefaultActivity();
            return Silian_brain;
        }

        private static void initCoreActivity(Brain<Camel> Silian_brain) {
            ActivityData<Camel> Silian_core =
                ActivityData.create(
                    Activity.CORE,
                    0,
                    ImmutableList.of(
                        new Swim<>(0.8F),
                        new CamelAi.CamelPanic(4.0F),
                        new LookAtTargetSink(45, 90),
                        new MoveToTargetSink()));
            Silian_brain.addActivity(
                Silian_core.activityType(),
                Silian_core.behaviorPriorityPairs(),
                Silian_core.conditions(),
                Silian_core.memoriesToEraseWhenStopped());
        }
    }
}
