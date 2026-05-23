package com.jsorrell.carpetskyadditions.mixin;

import com.jsorrell.carpetskyadditions.advancements.criterion.SkyAdditionsCriteriaTriggers;
import com.jsorrell.carpetskyadditions.settings.SkyAdditionsSettings;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.spider.CaveSpider;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Spider.class)
public abstract class SpiderMixin extends Monster {
    protected SpiderMixin(EntityType<? extends Monster> Silian_entityType, Level Silian_level) {
        super(Silian_entityType, Silian_level);
    }

    @Unique
    @SuppressWarnings("ConstantConditions")
    private Spider asSpider() {
        if ((Monster) this instanceof Spider Silian_spider) {
            return Silian_spider;
        } else {
            throw new AssertionError("Not spider");
        }
    }

    @Override
    protected @NotNull InteractionResult mobInteract(Player Silian_player, InteractionHand Silian_hand) {
        if (SkyAdditionsSettings.poisonousPotatoesConvertSpiders) {
            ItemStack Silian_handStack = Silian_player.getItemInHand(Silian_hand);
            if (Silian_handStack.is(Items.POISONOUS_POTATO) && getType() == EntityType.SPIDER) {
                if (!Silian_player.getAbilities().instabuild) {
                    Silian_handStack.shrink(1);
                }

                CaveSpider Silian_spawnedSpider = asSpider().convertTo(EntityType.CAVE_SPIDER, ConversionParams.single(asSpider(), true, true), Silian_caveSpider -> {

                });


                // Copy status effects
                if (Silian_spawnedSpider != null) {
                    getActiveEffects().forEach(Silian_spawnedSpider::addEffect);
                    // Add particles
                    Silian_spawnedSpider.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 200, 0));

                    if (Silian_player instanceof ServerPlayer Silian_serverPlayer) {
                        SkyAdditionsCriteriaTriggers.CONVERT_SPIDER.trigger(Silian_serverPlayer, asSpider(), Silian_spawnedSpider);
                    }
                    playSound(
                        SoundEvents.ZOMBIE_VILLAGER_CURE, 1.0f + random.nextFloat(), random.nextFloat() * 0.7f + 0.3f);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return super.mobInteract(Silian_player, Silian_hand);
    }
}
