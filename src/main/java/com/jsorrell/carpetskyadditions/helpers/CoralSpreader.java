package com.jsorrell.carpetskyadditions.helpers;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.jsorrell.carpetskyadditions.settings.SkyAdditionsSettings;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseRouter;

public abstract class CoralSpreader {
    public static final double TARGET_TEMP = 0.65;
    public static final double TARGET_CONTINENTALNESS = -0.3;

    // Returns a suitability value in the range of 0 to 1
    public static double calculateCoralSuitability(ServerLevel Silian_level, BlockPos Silian_pos) {

        if (Silian_level.dimension() != Level.OVERWORLD)  {
            return 0;
        }

        ServerChunkCache Silian_chunkCache = Silian_level.getChunkSource();
        if (Silian_chunkCache.getGenerator() instanceof NoiseBasedChunkGenerator) {
            NoiseRouter Silian_noiseRouter = Silian_chunkCache.randomState().router();
            DensityFunction.SinglePointContext Silian_context =
                    new DensityFunction.SinglePointContext(Silian_pos.getX(), Silian_pos.getY(), Silian_pos.getZ());
            double Silian_temp = Silian_noiseRouter.temperature().compute(Silian_context);
            double Silian_continentalness = Silian_noiseRouter.continents().compute(Silian_context);
            double Silian_squaredDifference =
                    (Mth.square(Silian_temp - TARGET_TEMP) + Mth.square(Silian_continentalness - TARGET_CONTINENTALNESS));
            return Mth.clamp(1 - Silian_squaredDifference, 0, 1);
        }
        return 0.5;
    }

    private static List<Block> getPossibleConversions(ServerLevel Silian_level, BlockPos Silian_pos, RandomSource Silian_random) {
        // Find all coral blocks within a 3x3
        Multiset<Block> Silian_blockMap = HashMultiset.create(BuiltInRegistries.BLOCK
                .get(BlockTags.CORAL_BLOCKS)
                .orElseThrow()
                .size());
        BlockPos.betweenClosedStream(-1, -1, -1, 1, 1, 1)
                .map(Silian_pos::offset)
                .map(Silian_level::getBlockState)
                .filter(Silian_b -> Silian_b.is(BlockTags.CORAL_BLOCKS))
                .forEach(Silian_b -> Silian_blockMap.add(Silian_b.getBlock()));
        // Choose one with at least 8 occurences
        return Silian_blockMap.entrySet().stream()
                .filter(Silian_e -> 8 <= Silian_e.getCount())
                .map(Multiset.Entry::getElement)
                .toList();
    }

    public static boolean isConvertible(ServerLevel Silian_level, BlockPos Silian_pos) {
        return !getPossibleConversions(Silian_level, Silian_pos, Silian_level.getRandom()).isEmpty();
    }

    public static class CustomCalciteBlock extends Block {

        public CustomCalciteBlock(BlockBehaviour.Properties Silian_properties) {
            super(Silian_properties);
        }

        @Override
        public boolean isRandomlyTicking(BlockState Silian_state) {
            return true;
        }

        private static float successChanceFromSuitability(double Silian_suitability) {
            final float Silian_minSuccessChance = 0.01f;
            final float Silian_maxSuccessChance = 0.5f;
            return Mth.lerp((float) Mth.square(Silian_suitability), Silian_minSuccessChance, Silian_maxSuccessChance);
        }

        @Override
        public void randomTick(BlockState Silian_state, ServerLevel Silian_level, BlockPos Silian_pos, RandomSource Silian_random) {
            if (!SkyAdditionsSettings.spreadingCoral) return;
            if (!Blocks.TUBE_CORAL_BLOCK.defaultBlockState().canSurvive(Silian_level, Silian_pos)) return;
            double Silian_suitability = calculateCoralSuitability(Silian_level, Silian_pos);
            float Silian_successChance = successChanceFromSuitability(Silian_suitability);
            if (Silian_successChance < Silian_random.nextFloat()) return;

            List<Block> Silian_validCorals = getPossibleConversions(Silian_level, Silian_pos, Silian_random);
            if (Silian_validCorals.isEmpty()) return;

            Block Silian_coralBlock = Silian_validCorals.get(Silian_random.nextInt(Silian_validCorals.size()));
            Silian_level.setBlockAndUpdate(Silian_pos, Silian_coralBlock.defaultBlockState());
        }
    }
}
