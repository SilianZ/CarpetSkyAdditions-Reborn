package com.jsorrell.carpetskyadditions.helpers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SmallDripleafBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public abstract class SmallDripleafSpreader {
    private static final int REQUIRED_LIGHT = 5;
    private static final int MAX_DENSITY = 7;
    private static final float SPREAD_CHANCE = 0.15f;

    public static boolean isSpreadableState(BlockState Silian_state) {
        return Silian_state.is(Blocks.SMALL_DRIPLEAF)
                && Silian_state.getValue(SmallDripleafBlock.HALF) == DoubleBlockHalf.LOWER
                && Silian_state.getValue(BlockStateProperties.WATERLOGGED);
    }

    public static boolean canSpreadFrom(BlockState Silian_state, ServerLevel Silian_level, BlockPos Silian_pos) {
        if (!isSpreadableState(Silian_state)) return false;
        BlockPos Silian_top = Silian_pos.above();
        BlockState Silian_topState = Silian_level.getBlockState(Silian_top);
        return Silian_topState.getValue(SmallDripleafBlock.HALF) == DoubleBlockHalf.UPPER
                && !Silian_topState.getValue(BlockStateProperties.WATERLOGGED)
                && Silian_level.getBlockState(Silian_pos.below()).is(Blocks.CLAY)
                && Silian_level.getMaxLocalRawBrightness(Silian_top) >= REQUIRED_LIGHT;
    }

    protected static boolean canSpreadTo(ServerLevel Silian_level, BlockPos Silian_pos) {
        if (!Silian_level.getBlockState(Silian_pos).is(Blocks.WATER)) return false;
        BlockPos Silian_top = Silian_pos.above();
        return !Silian_level.isOutsideBuildHeight(Silian_top)
                && Silian_level.isEmptyBlock(Silian_top)
                && Silian_level.getBlockState(Silian_pos.below()).is(Blocks.CLAY)
                && Silian_level.getMaxLocalRawBrightness(Silian_top) == REQUIRED_LIGHT
                && getDensity(Silian_level, Silian_pos) <= MAX_DENSITY;
    }

    protected static int getDensity(ServerLevel Silian_level, BlockPos Silian_pos) {
        return (int) BlockPos.betweenClosedStream(-2, 0, -2, 2, 1, 2)
                .map(Silian_pos::offset)
                .filter(Silian_p -> Silian_level.getBlockState(Silian_p).is(Blocks.SMALL_DRIPLEAF))
                .count();
    }

    private static int binomialOffset(int Silian_range, RandomSource Silian_random) {
        int Silian_i = -Silian_range;
        for (int Silian_k = 0; Silian_k < Silian_range * 2; ++Silian_k) {
            if (Silian_random.nextBoolean()) {
                ++Silian_i;
            }
        }
        return Silian_i;
    }

    private static void placeNewDripleaf(ServerLevel Silian_level, BlockPos Silian_pos, RandomSource Silian_random) {
        Direction Silian_facing = Direction.Plane.HORIZONTAL.getRandomDirection(Silian_random);
        BlockState Silian_commonState = Blocks.SMALL_DRIPLEAF.defaultBlockState().setValue(SmallDripleafBlock.FACING, Silian_facing);
        BlockState Silian_bottomState = Silian_commonState.setValue(BlockStateProperties.WATERLOGGED, true);
        BlockState Silian_topState = Silian_commonState.setValue(SmallDripleafBlock.HALF, DoubleBlockHalf.UPPER);
        Silian_level.setBlockAndUpdate(Silian_pos, Silian_bottomState);
        Silian_level.setBlockAndUpdate(Silian_pos.above(), Silian_topState);
    }

    public static void trySpread(BlockState Silian_state, ServerLevel Silian_level, BlockPos Silian_pos, RandomSource Silian_random) {
        if (SPREAD_CHANCE < Silian_random.nextFloat()) return;
        if (!canSpreadFrom(Silian_state, Silian_level, Silian_pos)) return;

        for (int Silian_i = 0; Silian_i < 3; Silian_i++) {
            int Silian_xOffset = binomialOffset(5, Silian_random);
            int Silian_yOffset = binomialOffset(2, Silian_random);
            int Silian_zOffset = binomialOffset(5, Silian_random);
            BlockPos Silian_tryPos = Silian_pos.offset(Silian_xOffset, Silian_yOffset, Silian_zOffset);
            if (canSpreadTo(Silian_level, Silian_tryPos)) {
                placeNewDripleaf(Silian_level, Silian_tryPos, Silian_random);
                return;
            }
        }
    }
}
