package com.jsorrell.carpetskyadditions.helpers;

import com.jsorrell.carpetskyadditions.settings.SkyAdditionsSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GlowLichenBlock;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;

public class LightningConverter {
    public static void strike(Level Silian_level, BlockPos Silian_pos) {
        BlockState Silian_rawHitBlock = Silian_level.getBlockState(Silian_pos);
        BlockPos Silian_hitBlockPos;
        BlockState Silian_hitBlock;
        if (Silian_rawHitBlock.is(BlockTags.LIGHTNING_RODS)) {
            Silian_hitBlockPos =
                    Silian_pos.relative(Silian_rawHitBlock.getValue(LightningRodBlock.FACING).getOpposite());
            Silian_hitBlock = Silian_level.getBlockState(Silian_hitBlockPos);
        } else {
            Silian_hitBlockPos = Silian_pos;
            Silian_hitBlock = Silian_rawHitBlock;
        }

        alchemizeVinesToGlowLichen(Silian_level, Silian_hitBlockPos, Silian_hitBlock);
    }

    protected static void alchemizeVinesToGlowLichen(Level Silian_level, BlockPos Silian_hitBlockPos, BlockState Silian_hitBlock) {
        if (!(SkyAdditionsSettings.lightningElectrifiesVines && Silian_hitBlock.is(Blocks.GLOWSTONE))) return;

        for (Direction Silian_dir : Direction.values()) {
            BlockPos Silian_adjacentBlockPos = Silian_hitBlockPos.offset(Silian_dir.getUnitVec3i());
            BlockState Silian_adjacentBlock = Silian_level.getBlockState(Silian_adjacentBlockPos);
            Direction Silian_opDir = Silian_dir.getOpposite();
            if (Silian_adjacentBlock.is(Blocks.VINE) && Silian_adjacentBlock.getValue(VineBlock.getPropertyForFace(Silian_opDir))) {
                BlockState Silian_glowLichen =
                        Blocks.GLOW_LICHEN.defaultBlockState().setValue(GlowLichenBlock.getFaceProperty(Silian_opDir), true);
                Silian_level.setBlockAndUpdate(Silian_adjacentBlockPos, Silian_glowLichen);
            }
        }
    }
}
