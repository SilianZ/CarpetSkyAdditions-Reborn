package com.jsorrell.carpetskyadditions.mixin;

import com.jsorrell.carpetskyadditions.settings.SkyAdditionsSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SaplingBlock.class)
public abstract class SaplingBlockMixin extends VegetationBlock {

    public SaplingBlockMixin(BlockBehaviour.Properties Silian_settings) {
        super(Silian_settings);
    }

    @Unique
    private boolean saplingIsOnSand(BlockGetter Silian_level, BlockPos Silian_pos) {
        BlockState Silian_underBlock = Silian_level.getBlockState(Silian_pos.below());
        return Silian_underBlock.is(BlockTags.SAND);
    }

    @Unique
    @SuppressWarnings("ConstantConditions")
    private boolean isPropagule() {
        return (VegetationBlock) this instanceof MangrovePropaguleBlock;
    }

    @Override
    protected boolean mayPlaceOn(BlockState Silian_floor, BlockGetter Silian_level, BlockPos Silian_pos) {
        if (SkyAdditionsSettings.saplingsDieOnSand && !isPropagule() && Silian_floor.is(BlockTags.SAND))
            return true;
        return super.mayPlaceOn(Silian_floor, Silian_level, Silian_pos);
    }

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void killIfOnSand(
            BlockState Silian_blockState, ServerLevel Silian_level, BlockPos Silian_pos, RandomSource Silian_random, CallbackInfo Silian_ci) {
        if (SkyAdditionsSettings.saplingsDieOnSand && saplingIsOnSand(Silian_level, Silian_pos)) {
            if (Silian_random.nextFloat() < 0.2) {
                Silian_level.setBlock(Silian_pos, Blocks.DEAD_BUSH.defaultBlockState(), Block.UPDATE_ALL);
            }
            Silian_ci.cancel();
        }
    }

    @Inject(method = "isValidBonemealTarget", at = @At("HEAD"), cancellable = true)
    private void stopBonemealingOnSand(
            LevelReader Silian_level, BlockPos Silian_pos, BlockState Silian_state, /* boolean isClient, */ CallbackInfoReturnable<Boolean> Silian_cir) {
        if (SkyAdditionsSettings.saplingsDieOnSand && saplingIsOnSand(Silian_level, Silian_pos)) {
            Silian_cir.setReturnValue(false);
        }
    }
}
