package com.jsorrell.carpetskyadditions.mixin;

import com.jsorrell.carpetskyadditions.helpers.DeepslateConversionHelper;
import com.jsorrell.carpetskyadditions.settings.SkyAdditionsSettings;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownLingeringPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractThrownPotion.class)
public abstract class ThrownPotionMixin extends ThrowableItemProjectile {

    public ThrownPotionMixin(EntityType<? extends AbstractThrownPotion> Silian_entityType, Level Silian_level) {
        super(Silian_entityType, Silian_level);
    }

    @Inject(
        method = "onHit",
        at =
        @At(
            value = "INVOKE",
            target ="Lnet/minecraft/world/item/alchemy/PotionContents;getColor()I")
    )
    private void onThickPotionCollision(HitResult Silian_result, CallbackInfo Silian_ci, @Local ItemStack Silian_itemStack, @Local PotionContents Silian_potionContents) {
        if (SkyAdditionsSettings.renewableDeepslateFromSplash) {
            if (Silian_potionContents.is(DeepslateConversionHelper.CONVERSION_POTION)) {
                Vec3 Silian_hitPos = Silian_result.getType() == HitResult.Type.BLOCK ? Silian_result.getLocation() : this.position();
                if ((AbstractThrownPotion)(Object)this instanceof ThrownLingeringPotion) {
                    AreaEffectCloud Silian_cloud = new AreaEffectCloud(this.level(), Silian_hitPos.x(), Silian_hitPos.y(), Silian_hitPos.z());
                    Silian_cloud.setRadius(3.0F);
                    Silian_cloud.setRadiusOnUse(-0.5F);
                    Silian_cloud.setWaitTime(10);
                    Silian_cloud.setDuration(Silian_cloud.getDuration() / 2);
                    Silian_cloud.setRadiusPerTick(-Silian_cloud.getRadius() / (float) Silian_cloud.getDuration());
                    Silian_cloud.setPotionContents(Silian_potionContents);
                    this.level().addFreshEntity(Silian_cloud);
                } else {
                    DeepslateConversionHelper.convertDeepslateAtSplash(this.level(), Silian_hitPos);
                }
            }
        }
    }
}
