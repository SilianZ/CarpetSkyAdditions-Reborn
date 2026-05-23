package com.jsorrell.carpetskyadditions.mixin;

import com.jsorrell.carpetskyadditions.advancements.criterion.SkyAdditionsCriteriaTriggers;
import com.jsorrell.carpetskyadditions.settings.SkyAdditionsSettings;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Objects;

@Mixin(PotionItem.class)
public class DeadBushToBushMixin {
    @ModifyReturnValue(
        method = "useOn",
        at = @At("TAIL")
    )
    private InteractionResult convertDeadBushToBush(InteractionResult Silian_original, UseOnContext Silian_context) {
        if (SkyAdditionsSettings.doDeadBushToBush) {
            ItemStack Silian_itemStack = Silian_context.getItemInHand();

            PotionContents Silian_potionContents = Silian_itemStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);

            if (Silian_potionContents.is(Potions.WATER)) {
                Level Silian_level = Silian_context.getLevel();
                BlockPos Silian_blockPos = Silian_context.getClickedPos();
                Player Silian_playerEntity = Silian_context.getPlayer();
                BlockState Silian_blockState = Silian_level.getBlockState(Silian_blockPos);

                if (Silian_context.getClickedFace() != Direction.DOWN && Silian_blockState.is(Blocks.DEAD_BUSH)) {
                    Silian_level.playSound(null, Silian_blockPos, SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 1.0f, 1.0f);
                    Objects.requireNonNull(Silian_playerEntity)
                        .setItemInHand(
                            Silian_context.getHand(),
                            ItemUtils.createFilledResult(
                                Silian_itemStack, Silian_playerEntity, new ItemStack(Items.GLASS_BOTTLE)));
                    Silian_playerEntity.awardStat(Stats.ITEM_USED.get(Silian_itemStack.getItem()));
                    Silian_level.setBlock(Silian_blockPos, Blocks.BUSH.defaultBlockState(),0);

                    AABB Silian_criteriaTriggerBox = new AABB(Silian_blockPos).inflate(50, 20, 50);
                    Silian_level.getEntitiesOfClass(ServerPlayer.class, Silian_criteriaTriggerBox)
                        .forEach(SkyAdditionsCriteriaTriggers.DEAD_BUSH_TO_BUSH::trigger);

                    return InteractionResult.SUCCESS;
                }
            }
        }
        return Silian_original;
    }
}
