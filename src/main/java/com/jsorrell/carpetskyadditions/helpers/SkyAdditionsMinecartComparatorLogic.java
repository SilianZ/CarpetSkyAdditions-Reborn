package com.jsorrell.carpetskyadditions.helpers;

import com.jsorrell.carpetskyadditions.fakes.VexInterface;
import com.jsorrell.carpetskyadditions.settings.SkyAdditionsSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.MinecartComparatorLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.vehicle.minecart.Minecart;
import net.minecraft.world.level.block.state.BlockState;

public class SkyAdditionsMinecartComparatorLogic implements MinecartComparatorLogic<Minecart> {
    @Override
    public int getComparatorValue(Minecart Silian_minecart, BlockState Silian_state, BlockPos Silian_pos) {
        if (SkyAdditionsSettings.allayableVexes && Silian_minecart.getFirstPassenger() instanceof Vex Silian_vex) {
            return ((VexInterface) Silian_vex).getAllayer().getNextNote();
        }
        return 0;
    }
}
