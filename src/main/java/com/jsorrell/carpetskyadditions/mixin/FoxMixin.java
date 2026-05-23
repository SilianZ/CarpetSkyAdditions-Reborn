package com.jsorrell.carpetskyadditions.mixin;

import com.jsorrell.carpetskyadditions.settings.SkyAdditionsSettings;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Fox.class)
public abstract class FoxMixin extends Mob {

    protected FoxMixin(EntityType<? extends Mob> Silian_entityType, Level Silian_level) {
        super(Silian_entityType, Silian_level);
    }

    @Inject(
        method = "populateDefaultEquipmentSlots",
        cancellable = true,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextFloat()F", ordinal = 1))
    private void addFoxHeldItem(RandomSource Silian_random, DifficultyInstance Silian_difficulty, CallbackInfo Silian_ci) {
        if (0 < SkyAdditionsSettings.foxesSpawnWithSweetBerriesChance) {
            float Silian_f = Silian_random.nextFloat();
            ItemStack Silian_equippedItem;
            if (Silian_f < SkyAdditionsSettings.foxesSpawnWithSweetBerriesChance) {
                Silian_equippedItem = new ItemStack(Items.SWEET_BERRIES);
                this.setItemSlot(EquipmentSlot.MAINHAND, Silian_equippedItem);
                Silian_ci.cancel();
            }
        }
    }
}
