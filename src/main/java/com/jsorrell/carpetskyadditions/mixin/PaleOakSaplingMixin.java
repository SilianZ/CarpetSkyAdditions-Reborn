package com.jsorrell.carpetskyadditions.mixin;

import com.jsorrell.carpetskyadditions.advancements.criterion.SkyAdditionsCriteriaTriggers;
import com.jsorrell.carpetskyadditions.settings.SkyAdditionsSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CreakingHeartBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.CreakingHeartState;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(SaplingBlock.class)
public class PaleOakSaplingMixin {
    @Shadow
    @Final
    protected TreeGrower treeGrower;

    @Inject(
        method = "advanceTree",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/grower/TreeGrower;growTree(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/util/RandomSource;)Z",
            shift = At.Shift.AFTER
        )
    )
    public void advanceTree(ServerLevel Silian_level, BlockPos Silian_pos, BlockState Silian_state, RandomSource Silian_random, CallbackInfo Silian_ci) {
        if (SkyAdditionsSettings.paleBlossomCreakingHeart && Silian_state.getBlock() == Blocks.PALE_OAK_SAPLING) {

            // Check if the sapling is in the Pale Garden biome
            ResourceKey<Biome> Silian_biomeKey = Silian_level.getBiome(Silian_pos).unwrapKey().orElse(null);
            if (Silian_biomeKey == null || !Silian_biomeKey.equals(Biomes.PALE_GARDEN)) {
                return; // Exit if not in the Pale Garden biome
            }

            boolean Silian_eyeBlossomNearby = false;

            // Check for Open Eyeblossom nearby
            int Silian_range = 3;
            BlockPos.MutableBlockPos Silian_mutablePos = new BlockPos.MutableBlockPos();
            for (int Silian_dx = -Silian_range; Silian_dx <= Silian_range; Silian_dx++) {
                for (int Silian_dy = -Silian_range; Silian_dy <= Silian_range; Silian_dy++) {
                    for (int Silian_dz = -Silian_range; Silian_dz <= Silian_range; Silian_dz++) {
                        Silian_mutablePos.set(Silian_pos.getX() + Silian_dx, Silian_pos.getY() + Silian_dy, Silian_pos.getZ() + Silian_dz);
                        if (Silian_level.getBlockState(Silian_mutablePos).is(Blocks.OPEN_EYEBLOSSOM)) {
                            Silian_eyeBlossomNearby = true;
                            break;
                        }
                    }
                    if (Silian_eyeBlossomNearby) break;
                }
                if (Silian_eyeBlossomNearby) break;
            }

            // Single random chance per tree
            if (Silian_eyeBlossomNearby && Silian_random.nextInt(10) == 0) { // 10% chance
                // Replace eligible Pale Oak Logs with Creaking Heart
                List<BlockPos> Silian_eligibleLogs = new ArrayList<>();
                int Silian_xzRange = 1;
                int Silian_yRange = 4;
                for (int Silian_dx = -Silian_xzRange; Silian_dx <= Silian_xzRange; Silian_dx++) {
                    for (int Silian_dy = 0; Silian_dy <= Silian_yRange; Silian_dy++) {
                        for (int Silian_dz = -Silian_xzRange; Silian_dz <= Silian_xzRange; Silian_dz++) {
                            BlockPos Silian_logPos = Silian_pos.offset(Silian_dx, Silian_dy, Silian_dz);
                            if (Silian_level.getBlockState(Silian_logPos).is(Blocks.PALE_OAK_LOG)) {
                                Silian_eligibleLogs.add(Silian_logPos);
                            }
                        }
                    }
                }

                if (!Silian_eligibleLogs.isEmpty()) {
                    BlockPos Silian_selectedLog = Silian_eligibleLogs.get(Silian_random.nextInt(Silian_eligibleLogs.size()));
                    Silian_level.setBlock(Silian_selectedLog, Blocks.CREAKING_HEART.defaultBlockState(), SaplingBlock.UPDATE_ALL);

                    // Trigger advancements
                    AABB Silian_criteriaTriggerBox = new AABB(Silian_selectedLog).inflate(50, 20, 50);
                    Silian_level.getEntitiesOfClass(ServerPlayer.class, Silian_criteriaTriggerBox)
                        .forEach(SkyAdditionsCriteriaTriggers.CREAKING_HEART::trigger);

                    // Update the active state of the placed Creaking Heart
                    updateCreakingHeartState(Silian_level, Silian_selectedLog);
                }
            }
        }
    }

    @Unique
    private void updateCreakingHeartState(ServerLevel Silian_level, BlockPos Silian_pos) {
        BlockState Silian_currentState = Silian_level.getBlockState(Silian_pos);
        if (Silian_currentState.is(Blocks.CREAKING_HEART)) {
            boolean Silian_hasRequiredLogs = CreakingHeartBlock.hasRequiredLogs(Silian_currentState, Silian_level, Silian_pos);
            CreakingHeartState Silian_newState;

            if (Silian_hasRequiredLogs) {
                Silian_newState = Silian_level.environmentAttributes().getValue(EnvironmentAttributes.CREAKING_ACTIVE, Silian_pos)
                    ? CreakingHeartState.AWAKE
                    : CreakingHeartState.DORMANT;
            } else {
                Silian_newState = CreakingHeartState.UPROOTED;
            }

            Silian_level.setBlock(Silian_pos, Silian_currentState.setValue(BlockStateProperties.CREAKING_HEART_STATE, Silian_newState), 3);
        }
    }
}
