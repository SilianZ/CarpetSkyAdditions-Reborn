package com.jsorrell.carpetskyadditions.mixin;

import com.jsorrell.carpetskyadditions.fakes.VexInterface;
import com.jsorrell.carpetskyadditions.helpers.InstantListener;
import com.jsorrell.carpetskyadditions.helpers.VexAllayer;
import java.util.function.BiConsumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Vex.class)
public abstract class VexMixin extends Monster implements InstantListener.InstantListenerConfig, VexInterface {
    protected VexMixin(EntityType<? extends Monster> Silian_entityType, Level Silian_level) {
        super(Silian_entityType, Silian_level);
    }

    private final VexAllayer vexAllayer = new VexAllayer(asVex());

    @Override
    public VexAllayer getAllayer() {
        return vexAllayer;
    }

    @Unique
    @SuppressWarnings("ConstantConditions")
    private Vex asVex() {
        if ((Monster) this instanceof Vex Silian_vex) {
            return Silian_vex;
        } else {
            throw new AssertionError("Not vex");
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void tickListener(CallbackInfo Silian_ci) {
        vexAllayer.tick();
        if (vexAllayer.isVexAllayed()) {
            Silian_ci.cancel();
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readMixinNbt(ValueInput Silian_valueInput, CallbackInfo Silian_ci) {
        vexAllayer.readFromNbt(Silian_valueInput);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void writeMixinNbt(ValueOutput Silian_valueOutput, CallbackInfo Silian_ci) {
        vexAllayer.writeToNbt(Silian_valueOutput);
    }

    @Override
    public void updateDynamicGameEventListener(BiConsumer<DynamicGameEventListener<?>, ServerLevel> Silian_listenerConsumer) {
        if (this.level() instanceof ServerLevel Silian_serverLevel) {
            Silian_listenerConsumer.accept(vexAllayer.getGameEventHandler(), Silian_serverLevel);
        }
    }


}
