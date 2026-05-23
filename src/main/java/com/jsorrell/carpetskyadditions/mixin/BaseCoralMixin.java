package com.jsorrell.carpetskyadditions.mixin;

import com.jsorrell.carpetskyadditions.helpers.DeadCoralToSandHelper;
import com.jsorrell.carpetskyadditions.settings.SkyAdditionsSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseCoralFanBlock;
import net.minecraft.world.level.block.BaseCoralPlantBlock;
import net.minecraft.world.level.block.BaseCoralPlantTypeBlock;
import net.minecraft.world.level.block.CoralFanBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin({BaseCoralPlantBlock.class, BaseCoralFanBlock.class})
public abstract class BaseCoralMixin extends BaseCoralPlantTypeBlock {
    public BaseCoralMixin(BlockBehaviour.Properties Silian_settings) {
        super(Silian_settings);
    }

    @Override
    public void onPlace(BlockState Silian_state, Level Silian_level, BlockPos Silian_pos, BlockState Silian_oldState, boolean Silian_notify) {
        if (SkyAdditionsSettings.coralErosion) {
            Silian_level.scheduleTick(Silian_pos, this, DeadCoralToSandHelper.getSandDropDelay(Silian_level.getRandom()));
        }
        super.onPlace(Silian_state, Silian_level, Silian_pos, Silian_oldState, Silian_notify);
    }

    @Unique
    @SuppressWarnings("ConstantConditions")
    private boolean isCoralFan() {
        return (BaseCoralPlantTypeBlock) this instanceof CoralFanBlock;
    }

    @Override
    protected @NotNull BlockState updateShape(BlockState Silian_state, LevelReader Silian_levelReader, ScheduledTickAccess Silian_scheduledTickAccess, BlockPos Silian_pos, Direction Silian_direction, BlockPos Silian_neighborPos, BlockState Silian_neighborState, RandomSource Silian_randomSource) {

        if (SkyAdditionsSettings.coralErosion && !isCoralFan()) {

            Silian_scheduledTickAccess.scheduleTick(Silian_pos, this, DeadCoralToSandHelper.getSandDropDelay(Silian_randomSource));
        }

        return super.updateShape(Silian_state, Silian_levelReader, Silian_scheduledTickAccess, Silian_pos, Silian_direction, Silian_neighborPos, Silian_neighborState, Silian_randomSource);
    }

    @Override
    public void tick(BlockState Silian_state, ServerLevel Silian_level, BlockPos Silian_pos, RandomSource Silian_random) {
        if (SkyAdditionsSettings.coralErosion && DeadCoralToSandHelper.tryDropSand(Silian_state, Silian_level, Silian_pos, Silian_random)) {
            Silian_level.scheduleTick(Silian_pos, this, DeadCoralToSandHelper.getSandDropDelay(Silian_random));
        }
    }
}
