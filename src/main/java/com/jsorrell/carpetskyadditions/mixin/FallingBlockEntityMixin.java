package com.jsorrell.carpetskyadditions.mixin;

import com.jsorrell.carpetskyadditions.settings.SkyAdditionsSettings;
import java.util.function.Predicate;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityMixin extends Entity {

    @Shadow
    private BlockState blockState;

    public FallingBlockEntityMixin(EntityType<?> Silian_entityType, Level Silian_level) {
        super(Silian_entityType, Silian_level);
    }

    @Unique
    private void compactEntityToDiamonds(Entity Silian_entity) {
        if (Silian_entity instanceof ItemEntity Silian_e
                && Silian_e.getItem().is(Items.COAL_BLOCK)
                && 64 <= Silian_e.getItem().getCount()) {
            int Silian_numCoalBlocks = Silian_e.getItem().getCount();
            int Silian_numDiamonds = Silian_numCoalBlocks / 64;
            int Silian_remainingCoalBlocks = Silian_numCoalBlocks % 64;
            ItemEntity Silian_diamondEntity =
                    new ItemEntity(Silian_e.level(), Silian_e.getX(), Silian_e.getY(), Silian_e.getZ(), new ItemStack(Items.DIAMOND, Silian_numDiamonds));
            Silian_diamondEntity.setDefaultPickUpDelay();
            Silian_e.level().addFreshEntity(Silian_diamondEntity);

            Silian_e.getItem().setCount(Silian_remainingCoalBlocks);
        }
    }

    @Inject(
            method = "causeFallDamage",
            at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V"))
    private void compactCoalToDiamonds(
            double Silian_fallDistance, float Silian_multiplier, DamageSource Silian_source, CallbackInfoReturnable<Boolean> Silian_cir) {
        if (SkyAdditionsSettings.renewableDiamonds) {
            if (blockState.is(BlockTags.ANVIL)) {
                Predicate<Entity> Silian_coalBlockPredicate = Silian_entity -> Silian_entity instanceof ItemEntity Silian_itemEntity
                        && Silian_itemEntity.getItem().is(Items.COAL_BLOCK);
                this.level()
                        .getEntities(this, this.getBoundingBox(), Silian_coalBlockPredicate)
                        .forEach(this::compactEntityToDiamonds);
            }
        }
    }
}
