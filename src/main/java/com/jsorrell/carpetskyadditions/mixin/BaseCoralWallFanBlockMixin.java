package com.jsorrell.carpetskyadditions.mixin;

import com.jsorrell.carpetskyadditions.helpers.DeadCoralToSandHelper;
import com.jsorrell.carpetskyadditions.settings.SkyAdditionsSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseCoralFanBlock;
import net.minecraft.world.level.block.BaseCoralWallFanBlock;
import net.minecraft.world.level.block.CoralWallFanBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BaseCoralWallFanBlock.class)
public class BaseCoralWallFanBlockMixin extends BaseCoralFanBlock {
    public BaseCoralWallFanBlockMixin(BlockBehaviour.Properties Silian_settings) {
        super(Silian_settings);
    }

    @Unique
    @SuppressWarnings("ConstantConditions")
    private boolean isCoralWallFan() {
        return (BaseCoralFanBlock) this instanceof CoralWallFanBlock;
    }

    @Inject(method = "updateShape", at = @At(value = "HEAD"))
    private void scheduleTickOnBlockUpdate(
        BlockState Silian_blockState, LevelReader Silian_levelReader, ScheduledTickAccess Silian_scheduledTickAccess, BlockPos Silian_blockPos, Direction Silian_direction, BlockPos Silian_blockPos2, BlockState Silian_blockState2, RandomSource Silian_randomSource,
            CallbackInfoReturnable<BlockState> Silian_cir) {
        if (SkyAdditionsSettings.coralErosion && !isCoralWallFan()) {
            Silian_scheduledTickAccess.scheduleTick(Silian_blockPos, this, DeadCoralToSandHelper.getSandDropDelay(Silian_randomSource));
        }
    }
}
