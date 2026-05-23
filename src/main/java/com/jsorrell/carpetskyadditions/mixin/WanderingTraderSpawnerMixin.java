package com.jsorrell.carpetskyadditions.mixin;

import com.jsorrell.carpetskyadditions.helpers.TraderCamelHelper;
import com.jsorrell.carpetskyadditions.settings.SkyAdditionsSettings;
import java.util.Optional;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTraderSpawner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.saveddata.WanderingTraderData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(WanderingTraderSpawner.class)
public abstract class WanderingTraderSpawnerMixin {
    @Unique
    private int currentSpawnTimer;

    @Shadow
    private int tickDelay;

    @Shadow
    @Final
    private RandomSource random;

    @Shadow
    protected abstract boolean spawn(ServerLevel Silian_level);

    @Shadow
    private WanderingTraderData getTraderData() {
        throw new AssertionError();
    }

    @Unique
    private boolean usesDefaultSettings() {
        return SkyAdditionsSettings.wanderingTraderSpawnRate == 24000
            && SkyAdditionsSettings.maxWanderingTraderSpawnChance == 0.075;
    }

    @Unique
    private boolean hasEnoughSpace(BlockGetter Silian_level, BlockPos Silian_pos) {
        for (BlockPos Silian_blockPos : BlockPos.betweenClosed(Silian_pos.offset(-1, 0, -1), Silian_pos.offset(1, 2, 1))) {
            if (!Silian_level.getBlockState(Silian_blockPos)
                .getCollisionShape(Silian_level, Silian_blockPos)
                .isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Inject(method = "spawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/npc/wanderingtrader/WanderingTraderSpawner;hasEnoughSpace(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Z"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    private void spawnTrader(
        ServerLevel Silian_serverLevel,
        CallbackInfoReturnable<Boolean> Silian_cir,
        @Local Player Silian_player,
        @Local(ordinal = 0) BlockPos Silian_playerPos,
        @Local int Silian_i,
        @Local PoiManager Silian_poiManager,
        @Local Optional<BlockPos> Silian_optional,
        @Local(ordinal = 1) BlockPos Silian_playerOrMeetingPos,
        @Local(ordinal = 2) BlockPos Silian_spawnPos) {
        if (!TraderCamelHelper.tradersRideCamelsAt(Silian_serverLevel, Silian_spawnPos)) {
            return;
        }

        if (hasEnoughSpace(Silian_serverLevel, Silian_spawnPos)) {
            if (Silian_serverLevel.getBiome(Silian_spawnPos).is(BiomeTags.WITHOUT_WANDERING_TRADER_SPAWNS)) {
                Silian_cir.setReturnValue(false);
                return;
            }

            Camel Silian_traderCamel = EntityType.CAMEL.spawn(Silian_serverLevel, Silian_spawnPos, EntitySpawnReason.EVENT);
            if (Silian_traderCamel != null) {
                WanderingTrader Silian_wanderingTrader = EntityType.WANDERING_TRADER.create(Silian_serverLevel, EntitySpawnReason.EVENT);
                if (Silian_wanderingTrader != null) {
                    Silian_wanderingTrader.setDespawnDelay(48000);

                    Silian_traderCamel.equipItemIfPossible(Silian_serverLevel, Items.SADDLE.getDefaultInstance());
                    Silian_wanderingTrader.setPos(Silian_traderCamel.getX(), Silian_traderCamel.getY(), Silian_traderCamel.getZ());
                    Silian_wanderingTrader.setYRot(Silian_traderCamel.getYRot());
                    Silian_wanderingTrader.setXRot(0.0F);
                    Silian_wanderingTrader.startRiding(Silian_traderCamel);
                    Silian_wanderingTrader.setWanderTarget(Silian_playerOrMeetingPos);
                    Silian_serverLevel.addFreshEntity(Silian_wanderingTrader);
                    Silian_cir.setReturnValue(true);
                    return;
                }
                Silian_cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void tick(ServerLevel Silian_level, boolean Silian_spawnEnemies, CallbackInfo Silian_ci) {
        if (usesDefaultSettings()) {
            return;
        }

        Silian_ci.cancel();

        if (!Silian_level.getGameRules().get(GameRules.SPAWN_WANDERING_TRADERS)) {
            return;
        }

        WanderingTraderData Silian_data = getTraderData();
        int Silian_spawnDelay = Silian_data.spawnDelay();

        if (SkyAdditionsSettings.wanderingTraderSpawnRate < Silian_spawnDelay) {
            Silian_spawnDelay = SkyAdditionsSettings.wanderingTraderSpawnRate;
            Silian_data.setSpawnDelay(Silian_spawnDelay);
            currentSpawnTimer = Math.min(1200, Silian_spawnDelay);
            tickDelay = currentSpawnTimer;
        }

        if (--tickDelay > 0) {
            return;
        }

        Silian_spawnDelay -= currentSpawnTimer;
        boolean Silian_trySpawn = Silian_spawnDelay <= 0;

        Silian_spawnDelay = Silian_trySpawn ? SkyAdditionsSettings.wanderingTraderSpawnRate : Silian_spawnDelay;
        currentSpawnTimer = Math.min(1200, Silian_spawnDelay);
        tickDelay = currentSpawnTimer;

        Silian_data.setSpawnDelay(Silian_spawnDelay);

        if (Silian_trySpawn && Silian_level.getGameRules().get(GameRules.SPAWN_MOBS)) {
            int Silian_spawnChance = Silian_data.spawnChance();
            if (random.nextInt(100 < Silian_spawnChance ? 1000 : 100) < Silian_spawnChance && spawn(Silian_level)) {
                Silian_data.setSpawnChance(25);
            } else {
                Silian_data.setSpawnChance(
                    Mth.clamp(
                        Silian_spawnChance + 25,
                        25,
                        (int) Math.round(SkyAdditionsSettings.maxWanderingTraderSpawnChance * 1000d)));
            }
        }
    }
}
