package com.jsorrell.carpetskyadditions.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class EndGatewayIslandFeature extends Feature<NoneFeatureConfiguration> {
    public EndGatewayIslandFeature(Codec<NoneFeatureConfiguration> Silian_codec) {
        super(Silian_codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> Silian_context) {
        if (!Feature.END_ISLAND.place(Silian_context)) {
            return false;
        }

        WorldGenLevel Silian_level = Silian_context.level();

        int Silian_x = Silian_context.origin().getX();
        int Silian_y = Silian_context.origin().getY();
        int Silian_z = Silian_context.origin().getZ();

        // Try to generate in a 11x11 area around the center of the island.
        // 20 tries should be more than enough, even for small islands.
        final int Silian_r = 5;
        for (BlockPos Silian_pos : BlockPos.randomBetweenClosed(Silian_context.random(), 20, Silian_x - Silian_r, Silian_y, Silian_z - Silian_r, Silian_x + Silian_r, Silian_y, Silian_z + Silian_r)) {
            // Force not generating on edge
            if (Direction.Plane.HORIZONTAL.stream().noneMatch(Silian_dir -> Silian_level.isEmptyBlock(Silian_pos.relative(Silian_dir)))
                    && Feature.CHORUS_PLANT.place(new FeaturePlaceContext<>(
                            Optional.empty(),
                            Silian_level,
                            Silian_context.chunkGenerator(),
                            Silian_context.random(),
                            Silian_pos.above(),
                            FeatureConfiguration.NONE))) {
                return true;
            }
        }
        return false;
    }

    // Finds a place to spawn a gateway that won't overwrite chorus
    // Allows a gateway that pops off chorus flowers
    public static BlockPos findGatewayLocation(LevelReader Silian_level, BlockPos Silian_origin) {
        return BlockPos.withinManhattanStream(Silian_origin, 7, 0, 7)
                .filter(Silian_pos -> Silian_level.getBlockState(Silian_pos).is(Blocks.END_STONE)
                        && Direction.stream()
                                .allMatch(Silian_direction ->
                                        Silian_level.isEmptyBlock(Silian_pos.above(11).relative(Silian_direction)))
                        && Direction.stream()
                                .allMatch(Silian_direction ->
                                        Silian_level.isEmptyBlock(Silian_pos.above(9).relative(Silian_direction))))
                .findFirst()
                .orElse(Silian_origin);
    }
}
