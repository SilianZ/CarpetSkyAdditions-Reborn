package com.jsorrell.carpetskyadditions.mixin;

import com.jsorrell.carpetskyadditions.settings.SkyAdditionsSettings;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.BlockUtil;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.portal.PortalForcer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

@Mixin(PortalForcer.class)
public class PortalForcerMixin {
    @Shadow
    @Final
    private ServerLevel level;

    @Unique
    private BlockState getPortalSkirtBlock(BlockPos Silian_pos) {
        if (level.getBiome(Silian_pos).is(Biomes.CRIMSON_FOREST)) {
            return Blocks.CRIMSON_NYLIUM.defaultBlockState();
        } else if (level.getBiome(Silian_pos).is(Biomes.WARPED_FOREST)) {
            return Blocks.WARPED_NYLIUM.defaultBlockState();
        }
        return Blocks.NETHERRACK.defaultBlockState();
    }

    @Inject(
            method = "createPortal",
            at =
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/core/Direction;getClockWise()Lnet/minecraft/core/Direction;",
                            ordinal = 0),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void addNetherrack(
            BlockPos Silian_pos,
            Direction.Axis Silian_axis,
            CallbackInfoReturnable<Optional<BlockUtil.FoundRectangle>> Silian_cir,
            @Local Direction Silian_direction,
            @Local(ordinal = 0) double Silian_d,
            @Local(ordinal = 1) BlockPos Silian_blockPos,
            @Local(ordinal = 1) double Silian_e,
            @Local(ordinal = 2) BlockPos Silian_blockPos2,
            @Local WorldBorder Silian_worldBorder) {
        if (SkyAdditionsSettings.renewableNetherrack) {
            if (!Silian_worldBorder.isWithinBounds(Silian_blockPos)) {
                return;
            }

            BlockPos.MutableBlockPos Silian_mutablePos = new BlockPos.MutableBlockPos();
            Direction Silian_rotatedDirection = Silian_direction.getClockWise();
            for (int Silian_i = -1; Silian_i < 3; ++Silian_i) { // i coordinate parallel to portal
                for (int Silian_j = -2; Silian_j < 3; ++Silian_j) { // j coordinate perpendicular to portal
                    if ((Math.abs(Silian_j) == 1 && (Silian_i == -1 || Silian_i == 2)) || (Math.abs(Silian_j) == 2 && (Silian_i == 0 || Silian_i == 1))) {
                        Silian_mutablePos.setWithOffset(
                                Silian_blockPos,
                                Silian_direction.getStepX() * Silian_i + Silian_rotatedDirection.getStepX() * Silian_j,
                                -1,
                                Silian_direction.getStepZ() * Silian_i + Silian_rotatedDirection.getStepZ() * Silian_j);
                        level.setBlockAndUpdate(Silian_mutablePos, getPortalSkirtBlock(Silian_mutablePos));
                    }
                }
            }
        }
    }
}
