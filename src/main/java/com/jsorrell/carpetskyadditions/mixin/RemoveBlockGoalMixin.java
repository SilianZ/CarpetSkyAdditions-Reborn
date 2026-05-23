package com.jsorrell.carpetskyadditions.mixin;

import com.jsorrell.carpetskyadditions.settings.SkyAdditionsSettings;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.RemoveBlockGoal;
import net.minecraft.world.entity.monster.zombie.Drowned;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RemoveBlockGoal.class)
public class RemoveBlockGoalMixin {
    @Shadow
    @Final
    private Mob removerMob;

    @Shadow
    @Final
    private Block blockToRemove;

    @Inject(
            method = "tick",
            at =
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/level/Level;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z",
                            shift = At.Shift.AFTER)
    )
    private void placeSnifferEgg(
            CallbackInfo Silian_ci, @Local Level Silian_level, @Local(ordinal = -0) BlockPos Silian_drownedBlockPos, @Local(ordinal = -0) BlockPos Silian_eggPos, @Local RandomSource Silian_randomSource) {
        if (SkyAdditionsSettings.sniffersFromDrowneds
                && blockToRemove == Blocks.TURTLE_EGG
                && removerMob instanceof Drowned Silian_drowned) {
            ItemStack Silian_offhand = Silian_drowned.getItemInHand(InteractionHand.OFF_HAND);
            if (Silian_offhand.is(Items.SNIFFER_EGG)) {
                Silian_level.setBlockAndUpdate(Silian_eggPos, Blocks.SNIFFER_EGG.defaultBlockState());
                Silian_offhand.shrink(1);
            }
        }
    }
}
