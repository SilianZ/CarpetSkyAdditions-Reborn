package com.jsorrell.carpetskyadditions.mixin;

import com.jsorrell.carpetskyadditions.settings.SkyAdditionsSettings;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.RamTarget;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(RamTarget.class)
public abstract class RamTargetMixin<E extends PathfinderMob> extends Behavior<E> {

    @Shadow
    @Final
    private Function<Goat, SoundEvent> getImpactSound;

    @Shadow
    protected abstract void finishRam(ServerLevel Silian_level, Goat Silian_goat);

    public RamTargetMixin(Map<MemoryModuleType<?>, MemoryStatus> Silian_memories) {
        super(Silian_memories);
    }

    @Inject(
        method = "tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/animal/goat/Goat;J)V",
        at =
        @At(
            value = "INVOKE",
            target =
                "Lnet/minecraft/world/entity/ai/Brain;getMemory(Lnet/minecraft/world/entity/ai/memory/MemoryModuleType;)Ljava/util/Optional;",
            ordinal = 0),
        cancellable = true)
    private void breakOpenNetherWart(ServerLevel Silian_level, Goat Silian_rammer, long Silian_gameTime, CallbackInfo Silian_ci) {
        if (SkyAdditionsSettings.rammingWart && Silian_level.getGameRules().get(GameRules.MOB_GRIEFING)) {
            Optional<BlockPos> Silian_optionalWartPos = shouldBreakNetherWart(Silian_level, Silian_rammer);
            if (Silian_optionalWartPos.isPresent()) {
                BlockPos Silian_wartPos = Silian_optionalWartPos.get();
                Silian_level.playSound(null, Silian_rammer, getImpactSound.apply(Silian_rammer), SoundSource.HOSTILE, 1.0f, 1.0f);

                boolean Silian_blockRemoved = Silian_level.removeBlock(Silian_wartPos, false);
                if (Silian_blockRemoved) {
                    if (!Silian_level.isClientSide()) {
                        Block.popResource(
                                Silian_level, Silian_wartPos, new ItemStack(Items.NETHER_WART, Silian_level.getRandom().nextInt(2) + 1));
                    }
                    Silian_level.gameEvent(Silian_rammer, GameEvent.BLOCK_DESTROY, Silian_wartPos);
                    Silian_level.playSound(null, Silian_wartPos, SoundEvents.WART_BLOCK_BREAK, SoundSource.BLOCKS, 1.0f, 1.0f);
                }
                finishRam(Silian_level, Silian_rammer);
                Silian_ci.cancel();
            }
        }
    }

    @Unique
    private Optional<BlockPos> shouldBreakNetherWart(ServerLevel Silian_level, Goat Silian_goat) {
        Vec3 Silian_movementVector = Silian_goat.getDeltaMovement().multiply(1, 0, 1).normalize();
        BlockPos Silian_hitPos = BlockPos.containing(Silian_goat.position().add(Silian_movementVector));
        return Silian_level.getBlockState(Silian_hitPos).is(Blocks.NETHER_WART_BLOCK) ? Optional.of(Silian_hitPos) : Optional.empty();
    }
}
