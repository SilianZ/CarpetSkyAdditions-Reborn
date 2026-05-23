package com.jsorrell.carpetskyadditions.mixin;

import com.jsorrell.carpetskyadditions.settings.SkyAdditionsSettings;
import java.util.Set;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.AbstractHugeMushroomFeature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.AlterGroundDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractHugeMushroomFeature.class)
public class AbstractHugeMushroomFeatureMixin {
    @Unique
    private void generateMycelium(WorldGenLevel Silian_level, RandomSource Silian_random, BlockPos Silian_pos) {
        AlterGroundDecorator Silian_decorator = new AlterGroundDecorator(BlockStateProvider.simple(Blocks.MYCELIUM));
        Silian_decorator.place(
            new TreeDecorator.Context(
                Silian_level,
                (Silian_blockPos, Silian_blockState) -> Silian_level.setBlock(Silian_blockPos, Silian_blockState, Block.UPDATE_ALL),
                Silian_random,
                Set.of(Silian_pos),
                Set.of(),
                Set.of()
            )
        );
    }

    @Inject(method = "place", at = @At("TAIL"))
    private void generateMycelium(
            CallbackInfoReturnable<Boolean> Silian_cir,
            @Local WorldGenLevel Silian_level,
            @Local RandomSource Silian_random,
            @Local BlockPos Silian_pos
    ) {
        if (SkyAdditionsSettings.hugeMushroomsSpreadMycelium) {
            generateMycelium(Silian_level, Silian_random, Silian_pos);
        }
    }
}
