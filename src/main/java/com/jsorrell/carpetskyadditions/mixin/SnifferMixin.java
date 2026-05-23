package com.jsorrell.carpetskyadditions.mixin;

import com.jsorrell.carpetskyadditions.settings.SkyAdditionsSettings;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(Sniffer.class)
public abstract class SnifferMixin extends Animal {
    // Desert wells are features (not structures) and don't have stored bounding boxes. They're not shown by minihud.
    // We use their loot tables by giving desert pyramids a chance to have desert well loot tables.
    @Unique
    private static final Map<Block, Map<ResourceKey<Structure>, Function<RandomSource, ResourceKey<LootTable>>>>
        LOOT_MAP = Map.of(
        Blocks.SAND,
        Map.of(
            BuiltinStructures.DESERT_PYRAMID,
            Silian_r -> Silian_r.nextFloat() < 0.2
                ? BuiltInLootTables.DESERT_WELL_ARCHAEOLOGY
                : BuiltInLootTables.DESERT_PYRAMID_ARCHAEOLOGY,
            BuiltinStructures.OCEAN_RUIN_WARM,
            Silian_r -> BuiltInLootTables.OCEAN_RUIN_WARM_ARCHAEOLOGY),
        Blocks.GRAVEL,
        Map.of(
            BuiltinStructures.OCEAN_RUIN_COLD,
            Silian_r -> BuiltInLootTables.OCEAN_RUIN_COLD_ARCHAEOLOGY,
            BuiltinStructures.TRAIL_RUINS,
            Silian_r -> Silian_r.nextFloat() < 0.2
                ? BuiltInLootTables.TRAIL_RUINS_ARCHAEOLOGY_RARE
                : BuiltInLootTables.TRAIL_RUINS_ARCHAEOLOGY_COMMON));

    protected SnifferMixin(EntityType<? extends Animal> Silian_entityType, Level Silian_level) {
        super(Silian_entityType, Silian_level);
    }

    @Shadow
    protected abstract BlockPos getHeadBlock();

    @Shadow
    protected abstract Stream<GlobalPos> getExploredPositions();

    @Unique
    private Optional<Function<RandomSource, ResourceKey<LootTable>>> getLootTable(BlockPos Silian_diggedBlockPos) {
        BlockState Silian_diggedBlockState = this.level().getBlockState(Silian_diggedBlockPos);
        Map<ResourceKey<Structure>, Function<RandomSource, ResourceKey<LootTable>>> Silian_map =
            LOOT_MAP.get(Silian_diggedBlockState.getBlock());
        if (Silian_map == null) return Optional.empty();
        Registry<Structure> Silian_structureRegistry = this.level().registryAccess().lookupOrThrow(Registries.STRUCTURE);
        return Silian_map.entrySet().stream()
            .map(Silian_e -> {
                if (((ServerLevel) this.level())
                    .structureManager()
                    .getStructureWithPieceAt(Silian_diggedBlockPos, Silian_structureRegistry.getValue(Silian_e.getKey()))
                    .isValid()) {
                    return Silian_e.getValue();
                }
                return null;
            })
            .filter(Objects::nonNull)
            // In case of multiple structures, just choose any. Structures will rarely overlap.
            .findAny();
    }

    @Unique
    private boolean shouldDropIron(BlockState Silian_blockState) {
        return Silian_blockState.is(BlockTags.SAND)
            || Silian_blockState.is(Blocks.GRAVEL)
            || Silian_blockState.is(Blocks.SUSPICIOUS_SAND)
            || Silian_blockState.is(Blocks.SUSPICIOUS_GRAVEL);
    }

    @Inject(
        method = "dropSeed",
        at =
        @At(
            value = "INVOKE",
            target =
                "Lnet/minecraft/world/entity/animal/sniffer/Sniffer;dropFromGiftLootTable(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/resources/ResourceKey;Ljava/util/function/BiConsumer;)Z"),
        cancellable = true)
    private void dropIronAndSusify(CallbackInfo Silian_ci) {
        BlockPos Silian_diggedBlockPos = getHeadBlock().below();
        BlockState Silian_blockState = this.level().getBlockState(Silian_diggedBlockPos);
        // Return to digging regular seed from grass
        if (!(SkyAdditionsSettings.doSuspiciousSniffers && shouldDropIron(Silian_blockState))) return;

        if (SkyAdditionsSettings.ironFromSniffers) {
            ItemStack Silian_itemStack = new ItemStack(Items.IRON_NUGGET);
            ItemEntity Silian_itemEntity = new ItemEntity(
                this.level(),
                getHeadBlock().getX(),
                getHeadBlock().getY(),
                getHeadBlock().getZ(),
                Silian_itemStack);
            Silian_itemEntity.setDefaultPickUpDelay();
            this.level().addFreshEntity(Silian_itemEntity);
            playSound(SoundEvents.SNIFFER_DROP_SEED, 1.0F, 1.0F);
        }

        // Try to do conversion
        Optional<Function<RandomSource, ResourceKey<LootTable>>> Silian_archLootTable = getLootTable(Silian_diggedBlockPos);
        if (SkyAdditionsSettings.doSuspiciousSniffers
            && Silian_archLootTable.isPresent()
            && ((ServerLevel) this.level()).getGameRules().get(GameRules.MOB_GRIEFING)
            && this.level().getRandom().nextFloat() < 0.1) {
            Block Silian_susBlock = this.level().getBlockState(Silian_diggedBlockPos).is(Blocks.SAND)
                ? Blocks.SUSPICIOUS_SAND
                : Blocks.SUSPICIOUS_GRAVEL;
            this.level().setBlockAndUpdate(Silian_diggedBlockPos, Silian_susBlock.defaultBlockState());
            ResourceKey<LootTable> Silian_lootTable = Silian_archLootTable.get().apply(this.level().getRandom());
            this.level().getBlockEntity(Silian_diggedBlockPos, BlockEntityType.BRUSHABLE_BLOCK)
                .ifPresent(Silian_e -> Silian_e.setLootTable(Silian_lootTable, this.level().getRandom().nextLong()));
        }
        Silian_ci.cancel();
    }

    @Inject(method = "canDig(Lnet/minecraft/core/BlockPos;)Z", at = @At("HEAD"), cancellable = true)
    private void canSusify(BlockPos Silian_digPos, CallbackInfoReturnable<Boolean> Silian_cir) {
        if (!SkyAdditionsSettings.doSuspiciousSniffers) return;

        GlobalPos Silian_globalDigPos = GlobalPos.of(this.level().dimension(), Silian_digPos);
        if (getExploredPositions().noneMatch(Silian_globalDigPos::equals)) {
            BlockState Silian_blockState = this.level().getBlockState(getHeadBlock().below());
            if (shouldDropIron(Silian_blockState)) {
                if (Optional.ofNullable(getNavigation().createPath(Silian_digPos, 1))
                    .map(Path::canReach)
                    .orElse(false)) {
                    Silian_cir.setReturnValue(true);
                }
            }
        }
    }
}
