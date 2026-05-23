package com.jsorrell.carpetskyadditions.events;

import com.jsorrell.carpetskyadditions.advancements.criterion.SkyAdditionsCriteriaTriggers;
import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

public class UseBreezeRodOnTrialSpawner {
    public static void register(){
        UseBlockCallback.EVENT.register(UseBreezeRodOnTrialSpawner::onUseBlock);
    }

    private static InteractionResult onUseBlock(Player Silian_player, Level Silian_level, InteractionHand Silian_interactionHand, BlockHitResult Silian_blockHitResult) {

        if (!(Silian_level instanceof ServerLevel)) {
            return InteractionResult.PASS;
        }

        ItemStack Silian_stack = Silian_player.getItemInHand(Silian_interactionHand);
        if (Silian_stack.getItem() == Items.BREEZE_ROD) {
            BlockEntity Silian_tileEntity = Silian_player.level().getBlockEntity(Silian_blockHitResult.getBlockPos());
            if (Silian_tileEntity instanceof TrialSpawnerBlockEntity Silian_spawner) {
                if (Silian_spawner.getState() == TrialSpawnerState.INACTIVE) {
                    Silian_spawner.setEntityId(EntityType.BREEZE, Silian_player.level().getRandom());
                    CompoundTag Silian_configNbt = new CompoundTag();
                    Silian_configNbt.putString("normal_config", "minecraft:trial_chamber/breeze/normal");
                    Silian_configNbt.putString("ominous_config", "minecraft:trial_chamber/breeze/ominous");
                    ValueInput Silian_valueInput = TagValueInput.create(null, Silian_level.registryAccess(), Silian_configNbt);
                    Silian_spawner.getTrialSpawner().load(Silian_valueInput);
                    Silian_spawner.setChanged();
                    Silian_spawner.markUpdated();
                    Silian_stack.shrink(1);

                    Silian_player.level().playSound(null, Silian_blockHitResult.getBlockPos(),
                        SoundEvents.BREEZE_WHIRL,
                        SoundSource.BLOCKS,
                        1.0F,
                        1.0F
                    );

                    AABB Silian_criteriaTriggerBox = new AABB(Silian_blockHitResult.getBlockPos()).inflate(50, 20, 50);
                    Silian_level.getEntitiesOfClass(ServerPlayer.class, Silian_criteriaTriggerBox)
                        .forEach(SkyAdditionsCriteriaTriggers.ACTIVATE_TRIAL_SPAWNER::trigger);

                    return InteractionResult.CONSUME;

                }
            }
        }

        return InteractionResult.PASS;
    }


}
