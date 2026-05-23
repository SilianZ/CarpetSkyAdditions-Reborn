package com.jsorrell.carpetskyadditions.mixin;

import com.jsorrell.carpetskyadditions.settings.SkyAdditionsSettings;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.warden.SonicBoom;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.dolphin.Dolphin;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SonicBoom.class)
public class SonicBoomMixin {
    @Inject(method = "lambda$tick$2", at = @At(value = "TAIL"), remap = false)
    private static void dropEchoShard(Warden Silian_warden, ServerLevel Silian_level, LivingEntity Silian_target, CallbackInfo Silian_ci) {
        if (SkyAdditionsSettings.renewableEchoShards) {
            if (Silian_target instanceof Dolphin || Silian_target instanceof Bat) {
                if (Silian_target.isDeadOrDying()) {
                    Silian_target.spawnAtLocation(Silian_level, Items.ECHO_SHARD);
                }
            }
        }
    }
}
