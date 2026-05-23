package com.jsorrell.carpetskyadditions.mixin;

import com.jsorrell.carpetskyadditions.gen.SkyBlockChunkGenerator;
import com.jsorrell.carpetskyadditions.settings.SkyAdditionsSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.level.dimension.end.EnderDragonFight;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderDragonFight.class)
public class EnderDragonFightMixin {
    @Shadow
    private boolean hasPreviouslyKilledDragon;

    @Shadow
    @Final
    private ServerLevel level;

    @Shadow
    private @Nullable BlockPos exitPortalLocation;

    @Inject(method = "spawnExitPortal", at = @At(value = "HEAD"))
    private void setExitPortalLocation(boolean Silian_openPortal, CallbackInfo Silian_ci) {
        if (level.getChunkSource().getGenerator() instanceof SkyBlockChunkGenerator Silian_chunkGenerator) {
            if (exitPortalLocation == null) {
                int Silian_y = Silian_chunkGenerator.getBaseHeightInEquivalentNoiseWorld(
                                0, 0, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, level)
                        - 1;
                exitPortalLocation = BlockPos.ZERO.atY(Silian_y);
            }
        }
    }

    @Inject(
            method = "setDragonKilled",
            at =
                    @At(
                            value = "FIELD",
                            target = "Lnet/minecraft/world/level/dimension/end/EnderDragonFight;hasPreviouslyKilledDragon:Z",
                            opcode = Opcodes.PUTFIELD))
    private void spawnShulkerOnDragonReKill(EnderDragon Silian_dragon, CallbackInfo Silian_ci) {
        if (SkyAdditionsSettings.shulkerSpawnsOnDragonKill && exitPortalLocation != null) {
            BlockPos Silian_shulkerPosition = exitPortalLocation.offset(0, 4, 0);
            if (hasPreviouslyKilledDragon && level.getBlockState(Silian_shulkerPosition).isAir()) {
                Shulker Silian_shulker = EntityType.SHULKER.create(level, null, Silian_shulkerPosition, EntitySpawnReason.EVENT, true, false);
                if (level.noCollision(Silian_shulker)) {
                    level.addFreshEntity(Silian_shulker);
                }
            }
        }
    }
}
