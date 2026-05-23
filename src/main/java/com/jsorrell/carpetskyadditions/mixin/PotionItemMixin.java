package com.jsorrell.carpetskyadditions.mixin;

import com.jsorrell.carpetskyadditions.helpers.DeepslateConversionHelper;
import com.jsorrell.carpetskyadditions.settings.SkyAdditionsSettings;
import java.util.Objects;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PotionItem.class)
public class PotionItemMixin {
    @ModifyReturnValue(
        method = "useOn",
        at = @At("TAIL")
    )
    private InteractionResult convertStoneToDeeplslate(InteractionResult Silian_original, UseOnContext Silian_context) {
        if (SkyAdditionsSettings.doRenewableDeepslate) {
            ItemStack Silian_itemStack = Silian_context.getItemInHand();

            PotionContents Silian_potionContents = Silian_itemStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);

            if (Silian_potionContents.is( DeepslateConversionHelper.CONVERSION_POTION)) {
                Level Silian_level = Silian_context.getLevel();
                BlockPos Silian_blockPos = Silian_context.getClickedPos();
                Player Silian_playerEntity = Silian_context.getPlayer();
                if (Silian_context.getClickedFace() != Direction.DOWN
                    && DeepslateConversionHelper.convertDeepslateWithBottle(Silian_level, Silian_blockPos, Silian_blockPos)) {
                    Silian_level.playSound(null, Silian_blockPos, SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 1.0f, 1.0f);
                    Objects.requireNonNull(Silian_playerEntity)
                        .setItemInHand(
                            Silian_context.getHand(),
                            ItemUtils.createFilledResult(
                                Silian_itemStack, Silian_playerEntity, new ItemStack(Items.GLASS_BOTTLE)));
                    Silian_playerEntity.awardStat(Stats.ITEM_USED.get(Silian_itemStack.getItem()));
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return Silian_original;
    }
}
