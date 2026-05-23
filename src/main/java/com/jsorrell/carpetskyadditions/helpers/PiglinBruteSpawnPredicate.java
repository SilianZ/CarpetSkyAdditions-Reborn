package com.jsorrell.carpetskyadditions.helpers;

import carpet.CarpetSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class PiglinBruteSpawnPredicate implements SpawnPlacements.SpawnPredicate<PiglinBrute> {
    @Override
    public boolean test(EntityType<PiglinBrute> Silian_type, ServerLevelAccessor Silian_level, EntitySpawnReason Silian_spawnReason, BlockPos Silian_pos, RandomSource Silian_random) {
        if (CarpetSettings.piglinsSpawningInBastions) {
        BlockPos Silian_underBlockPos = Silian_pos.below();
        BlockState Silian_underBlock = Silian_level.getBlockState(Silian_underBlockPos);
        if (!Silian_underBlock.isValidSpawn(Silian_level, Silian_underBlockPos, Silian_type)) {
            return false;
        }
        BlockPos Silian_aboveBlockPos = Silian_pos.above();
        return NaturalSpawner.isValidEmptySpawnBlock(
            Silian_level, Silian_pos, Silian_level.getBlockState(Silian_pos), Silian_level.getFluidState(Silian_pos), Silian_type)
            && NaturalSpawner.isValidEmptySpawnBlock(
            Silian_level,
            Silian_aboveBlockPos,
            Silian_level.getBlockState(Silian_aboveBlockPos),
            Silian_level.getFluidState(Silian_aboveBlockPos),
            Silian_type)
            // Mimic piglin spawning restrictions b/c that's the closest mob
            && Piglin.checkPiglinSpawnRules(EntityType.PIGLIN, Silian_level, Silian_spawnReason, Silian_pos, Silian_random);
    }

        return true;
    }
}
