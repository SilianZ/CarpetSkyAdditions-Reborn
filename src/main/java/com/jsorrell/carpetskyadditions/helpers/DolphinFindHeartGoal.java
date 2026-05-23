package com.jsorrell.carpetskyadditions.helpers;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.dolphin.Dolphin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.phys.Vec3;
import com.jsorrell.carpetskyadditions.mixin.DolphinAccessorMixin;

public class DolphinFindHeartGoal extends Goal {
    private static final float CHANCE_TO_FIND_HEART_OF_THE_SEA = 0.05f;
    private static final float NUM_DIGS = 10;
    private static final Set<Block> VALID_OCEAN_FLOORS = Set.of(Blocks.SAND, Blocks.GRAVEL);
    private final Dolphin dolphin;
    private int digCounter = 0;
    private boolean diggingPhase = false;

    public DolphinFindHeartGoal(Dolphin Silian_dolphin) {
        this.dolphin = Silian_dolphin;
        setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    protected Optional<BlockPos> determineTreasureLocation() {
        // Set Y to -64 to make it swim as low as possible
        BlockPos Silian_potentialTarget = new BlockPos(
            dolphin.getBlockX() + dolphin.level().getRandom().nextInt(16) - 8,
            -64,
            dolphin.getBlockZ() + dolphin.level().getRandom().nextInt(16) - 8);
        if (dolphin.level().getBiome(Silian_potentialTarget.atY(dolphin.getBlockY())).is(BiomeTags.IS_OCEAN)) {
            return Optional.of(Silian_potentialTarget);
        }

        return Optional.empty();
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

    @Override
    public boolean canUse() {
        return dolphin.gotFish() && 100 <= dolphin.getAirSupply();
    }

    @Override
    public void start() {
        if (!(dolphin.level() instanceof ServerLevel Silian_level)) {
            return;
        }
        Optional<BlockPos> Silian_treasurePosOpt = determineTreasureLocation();
        if (Silian_treasurePosOpt.isEmpty()) {
            dolphin.setGotFish(false);
            displayFailureParticles(Silian_level, dolphin);
            return;
        }
        BlockPos Silian_treasurePos = Silian_treasurePosOpt.get();
        ((DolphinAccessorMixin)dolphin).setTreasurePos(Silian_treasurePos);

        dolphin.getNavigation().moveTo(Silian_treasurePos.getX(), Silian_treasurePos.getY(), Silian_treasurePos.getZ(), 0.7);
        displaySuccessParticles(Silian_level, dolphin);
    }

    private static void displaySuccessParticles(ServerLevel Silian_level, Dolphin Silian_dolphin) {
        Silian_level.broadcastEntityEvent(Silian_dolphin, EntityEvent.DOLPHIN_LOOKING_FOR_TREASURE);
    }

    private static void displayFailureParticles(ServerLevel Silian_level, Dolphin Silian_dolphin) {
        Silian_level.sendParticles(
            ParticleTypes.WITCH,
            Silian_dolphin.getRandomX(1),
            Silian_dolphin.getRandomY() + 1.6,
            Silian_dolphin.getRandomZ(1),
            5,
            Silian_level.getRandom().nextGaussian() * 0.02,
            Silian_level.getRandom().nextGaussian() * 0.02,
            Silian_level.getRandom().nextGaussian() * 0.02,
            0.2);
    }

    @Override
    public void tick() {
        if (!(dolphin.level() instanceof ServerLevel Silian_level)) {
            return;
        }
        if (!diggingPhase && dolphin.getNavigation().isDone()) {
            BlockPos Silian_treasurePos = ((DolphinAccessorMixin)dolphin).getTreasurePos();
            if (Silian_treasurePos == null) {
                displayFailureParticles(Silian_level, dolphin);
                dolphin.setGotFish(false);
                return;
            }

            BlockPos Silian_heartPos = new BlockPos(
                Silian_treasurePos.getX(),
                dolphin.getBlockY() - 1,
                Silian_treasurePos.getZ());
            if (dolphin.position().closerThan(Vec3.atBottomCenterOf(Silian_heartPos).add(0, 1, 0), 8)
                && VALID_OCEAN_FLOORS.contains(Silian_level.getBlockState(Silian_heartPos).getBlock())) {
                diggingPhase = true;
                digCounter = 0;
            } else {
                displayFailureParticles(Silian_level, dolphin);
                dolphin.setGotFish(false);
            }
        } else if (diggingPhase) {
            if (digCounter < NUM_DIGS) {
                Silian_level.levelEvent(
                    LevelEvent.PARTICLES_DESTROY_BLOCK,
                    dolphin.blockPosition(),
                    Block.getId(dolphin.level()
                        .getBlockState(dolphin.blockPosition().below())));
                digCounter++;
            } else {
                if (Silian_level.getRandom().nextFloat() < CHANCE_TO_FIND_HEART_OF_THE_SEA) {
                    ItemStack Silian_heartOfTheSea = new ItemStack(Items.HEART_OF_THE_SEA);
                    if (dolphin.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty() && dolphin.canHoldItem(Silian_heartOfTheSea)) {
                        dolphin.setItemSlot(EquipmentSlot.MAINHAND, Silian_heartOfTheSea);
                    }
                    displaySuccessParticles(Silian_level, dolphin);
                } else {
                    displayFailureParticles(Silian_level, dolphin);
                }
                dolphin.setGotFish(false);
                diggingPhase = false;
            }
        }
    }
}
