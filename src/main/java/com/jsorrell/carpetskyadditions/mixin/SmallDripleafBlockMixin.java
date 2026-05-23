package com.jsorrell.carpetskyadditions.mixin;

import com.jsorrell.carpetskyadditions.helpers.SmallDripleafSpreader;
import com.jsorrell.carpetskyadditions.settings.SkyAdditionsSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.SmallDripleafBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SmallDripleafBlock.class)
public abstract class SmallDripleafBlockMixin extends DoublePlantBlock {
    public SmallDripleafBlockMixin(Properties Silian_properties) {
        super(Silian_properties);
    }

    @Override
    public boolean isRandomlyTicking(BlockState Silian_state) {
        return SmallDripleafSpreader.isSpreadableState(Silian_state);
    }

    @Override
    public void randomTick(BlockState Silian_state, ServerLevel Silian_level, BlockPos Silian_pos, RandomSource Silian_random) {
        if (!SkyAdditionsSettings.spreadingSmallDripleaves) return;
        SmallDripleafSpreader.trySpread(Silian_state, Silian_level, Silian_pos, Silian_random);
    }
}
