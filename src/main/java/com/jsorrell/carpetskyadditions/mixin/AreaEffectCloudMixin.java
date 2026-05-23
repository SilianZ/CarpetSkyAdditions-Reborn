package com.jsorrell.carpetskyadditions.mixin;

import com.jsorrell.carpetskyadditions.helpers.DeepslateConversionHelper;
import com.jsorrell.carpetskyadditions.settings.SkyAdditionsSettings;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AreaEffectCloud.class)
public abstract class AreaEffectCloudMixin extends Entity {

    @Shadow
    private PotionContents potionContents;

    public AreaEffectCloudMixin(EntityType<?> Silian_entityType, Level Silian_level) {
        super(Silian_entityType, Silian_level);
    }


    @Inject(method = "tick", at = @At("HEAD"))
    private void convertDeepslateOnTick(CallbackInfo Silian_ci) {
        if (SkyAdditionsSettings.renewableDeepslateFromSplash) {
            if (this.potionContents.is(DeepslateConversionHelper.CONVERSION_POTION))
                DeepslateConversionHelper.convertDeepslateInCloud(this.level(), this.getBoundingBox());
        }
    }
}
