package com.jsorrell.carpetskyadditions.helpers;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class DeepslateConversionHelper {
    public static final Holder<Potion> CONVERSION_POTION = Potions.THICK;

    public static Optional<BlockState> canConvert(BlockState Silian_from) {
        if (Silian_from.is(Blocks.STONE)) {
            return Optional.of(Blocks.DEEPSLATE.defaultBlockState());
        }
        return Optional.empty();
    }

    protected static double chanceFromDurationMultiplier(double Silian_mult) {
        return Mth.clamp(2.0 * Silian_mult, 0, 1);
    }

    public static double getSplashConversionChance(double Silian_distance) {
        // vanilla calculation -- don't change
        double Silian_mult = Mth.clamp(1.0 - Silian_distance / 4.0, 0, 1);

        return chanceFromDurationMultiplier(Silian_mult);
    }

    public static void convertDeepslateAtSplash(Level Silian_level, Vec3 Silian_hitPos) {
        BlockPos.betweenClosedStream(AABB.ofSize(Silian_hitPos, 8.25, 4.25, 8.25)).forEach(Silian_pos -> {
            BlockState Silian_state = Silian_level.getBlockState(Silian_pos);
            Optional<BlockState> Silian_optionalConvertedState = canConvert(Silian_state);
            if (Silian_optionalConvertedState.isPresent()) {
                double Silian_distance = Math.sqrt(Silian_pos.getCenter().distanceToSqr(Silian_hitPos));
                if (Silian_level.getRandom().nextDouble() < getSplashConversionChance(Silian_distance)) {
                    Silian_level.setBlockAndUpdate(Silian_pos, Silian_optionalConvertedState.get());
                }
            }
        });
    }

    public static void convertDeepslateInCloud(Level Silian_level, AABB Silian_box) {
        BlockPos.betweenClosedStream(Silian_box).forEach(Silian_pos -> {
            BlockState Silian_state = Silian_level.getBlockState(Silian_pos);
            Optional<BlockState> Silian_optionalConvertedState = canConvert(Silian_state);
            Silian_optionalConvertedState.ifPresent(Silian_blockState -> Silian_level.setBlockAndUpdate(Silian_pos, Silian_blockState));
        });
    }

    public static boolean convertDeepslateWithBottle(Level Silian_level, BlockPos Silian_blockPos, BlockPos Silian_eventPos) {
        BlockState Silian_state = Silian_level.getBlockState(Silian_blockPos);
        Optional<BlockState> Silian_optionalConvertedState = canConvert(Silian_state);
        if (Silian_optionalConvertedState.isPresent()) {
            if (Silian_level instanceof ServerLevel Silian_serverLevel) {
                for (int Silian_i = 0; Silian_i < 5; ++Silian_i) {
                    Silian_serverLevel.sendParticles(
                            ParticleTypes.SPLASH,
                            (double) Silian_eventPos.getX() + Silian_level.getRandom().nextDouble(),
                            Silian_eventPos.getY() + 1,
                            (double) Silian_eventPos.getZ() + Silian_level.getRandom().nextDouble(),
                            1,
                            0.0,
                            0.0,
                            0.0,
                            1.0);
                }
            }
            Silian_level.playSound(null, Silian_eventPos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
            Silian_level.gameEvent(null, GameEvent.FLUID_PLACE, Silian_eventPos);
            Silian_level.setBlockAndUpdate(Silian_blockPos, Silian_optionalConvertedState.get());
            return true;
        }
        return false;
    }
}
