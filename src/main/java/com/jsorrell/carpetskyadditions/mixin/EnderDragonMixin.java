package com.jsorrell.carpetskyadditions.mixin;

import com.jsorrell.carpetskyadditions.settings.SkyAdditionsSettings;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(EnderDragon.class)
public abstract class EnderDragonMixin extends Mob implements Enemy {
    @Shadow
    public abstract void kill(ServerLevel Silian_level);

    protected boolean shouldDropHead;
    private static final String SHOULD_DROP_HEAD_KEY = "ShouldDropHead";

    protected EnderDragonMixin(EntityType<? extends Mob> Silian_entityType, Level Silian_level) {
        super(Silian_entityType, Silian_level);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readMixinNbt(ValueInput Silian_valueInput, CallbackInfo Silian_ci) {

        shouldDropHead = Silian_valueInput.getBooleanOr(SHOULD_DROP_HEAD_KEY, false);

    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void writeMixinNbt(ValueOutput Silian_valueOutput, CallbackInfo Silian_ci) {
        if (SkyAdditionsSettings.renewableDragonHeads) {
            Silian_valueOutput.putBoolean(SHOULD_DROP_HEAD_KEY, shouldDropHead);
        }
    }

    @Inject(
        method = "tickDeath",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/ExperienceOrb;award(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;I)V",
            ordinal = 1
        )
    )
    protected void dropDragonHead(CallbackInfo Silian_ci, @Local ServerLevel Silian_serverLevel) {
        if (SkyAdditionsSettings.renewableDragonHeads && shouldDropHead) {
            spawnAtLocation(Silian_serverLevel, Items.DRAGON_HEAD);
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel Silian_serverLevel, DamageSource Silian_damageSource, boolean Silian_bl) {
        super.dropCustomDeathLoot(Silian_serverLevel, Silian_damageSource, Silian_bl);
        if (SkyAdditionsSettings.renewableDragonHeads) {
            if (Silian_damageSource.getEntity() instanceof Creeper Silian_killerCreeper) {
                if (Silian_killerCreeper.isPowered()) {
                    shouldDropHead = true;
                }
            }
        }
    }
}
