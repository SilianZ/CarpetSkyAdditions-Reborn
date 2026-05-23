package com.jsorrell.carpetskyadditions.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
@Debug(export = true)
@Mixin(EnderMan.EndermanLeaveBlockGoal.class)
public abstract class EnderMan_EndermanLeaveBlockGoalMixin {
    @Shadow
    @Final
    private EnderMan enderman;

    @Inject(
            method = "tick",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/level/block/Block;updateFromNeighbourShapes(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"
//                            shift = At.Shift.BEFORE doesn't actually do anything?
                    ),
            cancellable = true)
    private void inject(
            CallbackInfo Silian_ci,
            @Local RandomSource Silian_random,
            @Local Level Silian_world,
            @Local(ordinal = 0) int Silian_x,
            @Local(ordinal = 1) int Silian_y,
            @Local(ordinal = 2) int Silian_z,
            @Local(ordinal = 0) BlockPos Silian_placePosBottom,
            @Local(ordinal = 0) BlockState Silian_placeStateBottom,
            @Local(ordinal = 1) BlockPos Silian_belowPlacePos,
            @Local(ordinal = 1) BlockState Silian_belowPosState,
            @Local(ordinal = 2) BlockState Silian_heldBlockState) {
        Block Silian_heldBlock = Silian_heldBlockState.getBlock();
        if (Silian_heldBlock instanceof DoublePlantBlock || Silian_heldBlock instanceof DoorBlock) {
            BlockPos Silian_placePosTop = Silian_placePosBottom.above();
            BlockState Silian_placeStateTop = Silian_world.getBlockState(Silian_placePosTop);
            if (Silian_placePosTop.getY() < Silian_world.getMaxY()
                    && Silian_placeStateBottom.isAir()
                    && Silian_placeStateTop.isAir()
                    && !Silian_belowPosState.isAir()
                    && !Silian_belowPosState.is(Blocks.BEDROCK)
                    && Silian_belowPosState.isCollisionShapeFullBlock(Silian_world, Silian_belowPlacePos)
                    && Silian_heldBlockState.canSurvive(Silian_world, Silian_placePosBottom)
                    && Silian_world.getEntities(enderman, new AABB(Silian_x, Silian_y, Silian_z, Silian_x + 1.0, Silian_y + 2.0, Silian_z + 1.0))
                            .isEmpty()) {

                if (Silian_heldBlock instanceof DoorBlock) {
                    boolean Silian_powered = Silian_world.hasNeighborSignal(Silian_placePosBottom) || Silian_world.hasNeighborSignal(Silian_placePosTop);
                    // FIXME what about facing and hinge?
                    Silian_heldBlockState = Silian_heldBlockState
                            .setValue(BlockStateProperties.POWERED, Silian_powered)
                            .setValue(BlockStateProperties.OPEN, Silian_powered);
                }
                Silian_world.setBlockAndUpdate(Silian_placePosBottom, Silian_heldBlockState);
                Silian_heldBlock.setPlacedBy(Silian_world, Silian_placePosBottom, Silian_heldBlockState, enderman, ItemStack.EMPTY);
                Silian_world.gameEvent(GameEvent.BLOCK_PLACE, Silian_placePosBottom, GameEvent.Context.of(enderman, Silian_heldBlockState));
                enderman.setCarriedBlock(null);
            }
            Silian_ci.cancel();
        }
    }
}
