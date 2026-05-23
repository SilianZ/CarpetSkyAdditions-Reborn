package com.jsorrell.carpetskyadditions.helpers;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

public class DeadCoralToSandHelper {
    protected static final double BREAK_CHANCE = 0.03;

    public static int getSandDropDelay(RandomSource Silian_random) {
        // 16-32s per sand, 24 on average
        // This comes out to about the same as 1 villager in a max rate iron farm, which is also infinitely auto.
        return 320 + Silian_random.nextInt(320);
    }

    public static boolean tryDropSand(BlockState Silian_state, Level Silian_level, BlockPos Silian_pos, RandomSource Silian_random) {
        FluidState Silian_fluidState = Silian_level.getFluidState(Silian_pos);
        if (!Silian_fluidState.is(Fluids.WATER)) {
            return false;
        }

        Vec3 Silian_waterVelocity = Silian_fluidState.getFlow(Silian_level, Silian_pos);
        if (Silian_waterVelocity.equals(Vec3.ZERO)) {
            return false;
        }

        if (!Silian_level.isClientSide()) {
            Vec3 Silian_sandVelocity = Silian_waterVelocity.scale(0.1);
            String Silian_lootTablePath = Silian_state.getBlock().getLootTable().toString(); // Get the loot table path as a string
            Item Silian_sandItem = Silian_lootTablePath.contains("fire") ? Items.RED_SAND : Items.SAND; // Check if the loot table path contains "fire"
            ItemEntity Silian_itemEntity = new ItemEntity(
                Silian_level,
                Silian_pos.getX() + 0.5,
                Silian_pos.getY(),
                Silian_pos.getZ() + 0.5,
                new ItemStack(Silian_sandItem),
                Silian_sandVelocity.x(),
                Silian_sandVelocity.y(),
                Silian_sandVelocity.z());
            Silian_itemEntity.setDefaultPickUpDelay();
            Silian_level.addFreshEntity(Silian_itemEntity);
        }

        if (Silian_random.nextFloat() < BREAK_CHANCE) {
            Silian_level.removeBlock(Silian_pos, false);
            Silian_level.playSound(
                    null, Silian_pos.getX(), Silian_pos.getY(), Silian_pos.getZ(), SoundEvents.SAND_BREAK, SoundSource.BLOCKS, 0.5f, 1f);
            return false;
        }

        return true;
    }
}
