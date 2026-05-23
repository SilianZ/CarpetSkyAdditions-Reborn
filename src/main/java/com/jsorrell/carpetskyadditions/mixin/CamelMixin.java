package com.jsorrell.carpetskyadditions.mixin;

import com.jsorrell.carpetskyadditions.fakes.CamelInterface;
import com.jsorrell.carpetskyadditions.helpers.TraderCamelHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camel.class)
public abstract class CamelMixin extends AbstractHorse implements CamelInterface {

    protected CamelMixin(EntityType<? extends AbstractHorse> Silian_entityType, Level Silian_level) {
        super(Silian_entityType, Silian_level);
    }

    @Invoker("makeBrain")
    abstract Brain<Camel> carpetSkyAdditions$invokeMakeBrain(Brain.Packed packed);

    @Unique
    @SuppressWarnings("ConstantConditions")
    private Camel asCamel() {
        if ((AbstractHorse) this instanceof Camel Silian_camel) {
            return Silian_camel;
        } else {
            throw new AssertionError("Not camel");
        }
    }

    @Unique
    public boolean isTraderCamel() {
        return TraderCamelHelper.isTraderCamel(asCamel());
    }

    @Inject(method = "canAddPassenger", at = @At("HEAD"), cancellable = true)
    public void canAddPassengerToTraderCamel(Entity Silian_passenger, CallbackInfoReturnable<Boolean> Silian_cir) {
        if (isTraderCamel()) {
            Silian_cir.setReturnValue(false);
        }
    }

    @Inject(method = "isFood", at = @At("HEAD"), cancellable = true)
    public void isFoodForTraderCamel(ItemStack Silian_stack, CallbackInfoReturnable<Boolean> Silian_cir) {
        if (isTraderCamel()) {
            Silian_cir.setReturnValue(false);
        }
    }

    @Unique
    public void carpetSkyAdditions$makeTraderCamel() {
        brain = TraderCamelHelper.TraderCamelAI.makeBrain(carpetSkyAdditions$invokeMakeBrain(Brain.Packed.EMPTY));
    }

    @Unique
    public void carpetSkyAdditions$makeStandaloneCamel() {
        brain = carpetSkyAdditions$invokeMakeBrain(Brain.Packed.EMPTY);
    }

}
