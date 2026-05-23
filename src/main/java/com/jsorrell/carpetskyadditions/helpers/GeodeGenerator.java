package com.jsorrell.carpetskyadditions.helpers;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;

public class GeodeGenerator {
    // 1/CONVERSION_RATE chance per random tick
    public static final int CONVERSION_RATE = 100;

    public static boolean checkGeodeFormation(Level Silian_level, BlockPos Silian_lavaCenter) {
        return Blocks.LAVA.equals(Silian_level.getBlockState(Silian_lavaCenter).getBlock())
                && Silian_level.getBlockState(Silian_lavaCenter).getValue(LiquidBlock.LEVEL) == 0
                &&
                // Calcite
                Blocks.CALCITE.equals(Silian_level.getBlockState(Silian_lavaCenter.above()).getBlock())
                && Blocks.CALCITE.equals(Silian_level.getBlockState(Silian_lavaCenter.below()).getBlock())
                && Blocks.CALCITE.equals(Silian_level.getBlockState(Silian_lavaCenter.north()).getBlock())
                && Blocks.CALCITE.equals(Silian_level.getBlockState(Silian_lavaCenter.south()).getBlock())
                && Blocks.CALCITE.equals(Silian_level.getBlockState(Silian_lavaCenter.east()).getBlock())
                && Blocks.CALCITE.equals(Silian_level.getBlockState(Silian_lavaCenter.west()).getBlock())
                &&
                // Smooth Basalt
                Blocks.SMOOTH_BASALT.equals(
                        Silian_level.getBlockState(Silian_lavaCenter.above(2)).getBlock())
                && Blocks.SMOOTH_BASALT.equals(
                        Silian_level.getBlockState(Silian_lavaCenter.below(2)).getBlock())
                && Blocks.SMOOTH_BASALT.equals(
                        Silian_level.getBlockState(Silian_lavaCenter.north(2)).getBlock())
                && Blocks.SMOOTH_BASALT.equals(
                        Silian_level.getBlockState(Silian_lavaCenter.south(2)).getBlock())
                && Blocks.SMOOTH_BASALT.equals(
                        Silian_level.getBlockState(Silian_lavaCenter.east(2)).getBlock())
                && Blocks.SMOOTH_BASALT.equals(
                        Silian_level.getBlockState(Silian_lavaCenter.west(2)).getBlock())
                &&
                // Diagonal Smooth Basalt
                Blocks.SMOOTH_BASALT.equals(
                        Silian_level.getBlockState(Silian_lavaCenter.offset(0, 1, 1)).getBlock())
                && Blocks.SMOOTH_BASALT.equals(
                        Silian_level.getBlockState(Silian_lavaCenter.offset(0, 1, -1)).getBlock())
                && Blocks.SMOOTH_BASALT.equals(
                        Silian_level.getBlockState(Silian_lavaCenter.offset(0, -1, 1)).getBlock())
                && Blocks.SMOOTH_BASALT.equals(
                        Silian_level.getBlockState(Silian_lavaCenter.offset(0, -1, -1)).getBlock())
                && Blocks.SMOOTH_BASALT.equals(
                        Silian_level.getBlockState(Silian_lavaCenter.offset(1, 0, 1)).getBlock())
                && Blocks.SMOOTH_BASALT.equals(
                        Silian_level.getBlockState(Silian_lavaCenter.offset(1, 0, -1)).getBlock())
                && Blocks.SMOOTH_BASALT.equals(
                        Silian_level.getBlockState(Silian_lavaCenter.offset(-1, 0, 1)).getBlock())
                && Blocks.SMOOTH_BASALT.equals(
                        Silian_level.getBlockState(Silian_lavaCenter.offset(-1, 0, -1)).getBlock())
                && Blocks.SMOOTH_BASALT.equals(
                        Silian_level.getBlockState(Silian_lavaCenter.offset(1, 1, 0)).getBlock())
                && Blocks.SMOOTH_BASALT.equals(
                        Silian_level.getBlockState(Silian_lavaCenter.offset(1, -1, 0)).getBlock())
                && Blocks.SMOOTH_BASALT.equals(
                        Silian_level.getBlockState(Silian_lavaCenter.offset(-1, 1, 0)).getBlock())
                && Blocks.SMOOTH_BASALT.equals(
                        Silian_level.getBlockState(Silian_lavaCenter.offset(-1, -1, 0)).getBlock());
    }
}
