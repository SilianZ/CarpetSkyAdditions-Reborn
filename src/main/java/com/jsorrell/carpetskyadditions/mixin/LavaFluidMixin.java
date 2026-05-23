package com.jsorrell.carpetskyadditions.mixin;

import com.jsorrell.carpetskyadditions.advancements.criterion.SkyAdditionsCriteriaTriggers;
import com.jsorrell.carpetskyadditions.helpers.GeodeGenerator;
import com.jsorrell.carpetskyadditions.settings.SkyAdditionsSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.LavaFluid;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LavaFluid.class)
public class LavaFluidMixin {
    @Inject(method = "randomTick", at = @At(value = "HEAD"))
    private void tryCreateGeode(ServerLevel Silian_level, BlockPos Silian_pos, FluidState Silian_fluidState, RandomSource Silian_random, CallbackInfo Silian_ci) {
        if (SkyAdditionsSettings.renewableBuddingAmethysts) {
            if (Silian_random.nextInt(GeodeGenerator.CONVERSION_RATE) == 0) {
                if (GeodeGenerator.checkGeodeFormation(Silian_level, Silian_pos)) {
                    Silian_level.setBlockAndUpdate(Silian_pos, Blocks.BUDDING_AMETHYST.defaultBlockState());
                    Silian_level.playSound(
                            null,
                            Silian_pos,
                            SoundEvents.LAVA_EXTINGUISH,
                            SoundSource.BLOCKS,
                            0.5f,
                            2.6f + (Silian_random.nextFloat() - Silian_random.nextFloat()) * 0.8f);
                    Silian_level.playSound(
                            null,
                            Silian_pos,
                            SoundEvents.AMETHYST_BLOCK_PLACE,
                            SoundSource.BLOCKS,
                            1.0f,
                            0.5f + Silian_level.getRandom().nextFloat() * 1.2f);

                    AABB Silian_criteriaTriggerBox = new AABB(Silian_pos).inflate(50, 20, 50);
                    Silian_level.getEntitiesOfClass(ServerPlayer.class, Silian_criteriaTriggerBox)
                            .forEach(SkyAdditionsCriteriaTriggers.GENERATE_GEODE::trigger);
                }
            }
        }
    }
}
