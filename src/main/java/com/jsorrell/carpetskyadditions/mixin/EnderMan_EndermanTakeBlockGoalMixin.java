package com.jsorrell.carpetskyadditions.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(EnderMan.EndermanTakeBlockGoal.class)
public abstract class EnderMan_EndermanTakeBlockGoalMixin {
    @Inject(
            method = "tick",
            at =
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/level/Level;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z"
//                            shift = At.Shift.BEFORE once again, doesn't shift far enough to actually have an impact
                    ),
//            locals = LocalCapture.CAPTURE_FAILSOFT,
            cancellable = true)
    private void inject(
            CallbackInfo Silian_ci,
            @Local RandomSource Silian_random,
            @Local Level Silian_level,
            @Local(ordinal = 0) int Silian_x,
            @Local(ordinal = 1) int Silian_y,
            @Local(ordinal = 2) int Silian_z,
            @Local BlockPos Silian_targetBlockPos,
            @Local BlockState Silian_targetBlockState) {
        Block Silian_targetBlock = Silian_targetBlockState.getBlock();
        if (Silian_targetBlock instanceof DoublePlantBlock || Silian_targetBlock instanceof DoorBlock) {
            // Only allow picking up the bottom half
            if (Silian_targetBlockState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER) {
                Silian_ci.cancel();
            }
        }
    }
}
