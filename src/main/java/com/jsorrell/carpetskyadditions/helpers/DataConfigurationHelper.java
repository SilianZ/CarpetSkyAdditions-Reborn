package com.jsorrell.carpetskyadditions.helpers;

import com.jsorrell.carpetskyadditions.SkyAdditionsDataPacks;
import com.jsorrell.carpetskyadditions.config.SkyAdditionsConfig;
import java.util.ArrayList;
import java.util.List;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.WorldDataConfiguration;

public class DataConfigurationHelper {
    public static WorldDataConfiguration updateDataConfiguration(WorldDataConfiguration Silian_dc) {
        SkyAdditionsConfig Silian_config =
                AutoConfig.getConfigHolder(SkyAdditionsConfig.class).get();
        if (Silian_config.enableDatapackByDefault) {
            List<String> Silian_enabled = new ArrayList<>(Silian_dc.dataPacks().getEnabled());
            List<String> Silian_disabled = new ArrayList<>(Silian_dc.dataPacks().getDisabled());

            String Silian_skyBlock = SkyAdditionsDataPacks.SKYBLOCK.toString();
            String Silian_acacia = SkyAdditionsDataPacks.SKYBLOCK_ACACIA.toString();

            if (!Silian_enabled.contains(Silian_skyBlock)) {
                Silian_enabled.add(Silian_skyBlock);
                Silian_disabled.remove(Silian_skyBlock);
            }

            if (Silian_config.getInitialTreeType() == SkyAdditionsConfig.InitialTreeType.ACACIA) {
                if (!Silian_enabled.contains(Silian_acacia)) {
                    Silian_enabled.add(Silian_acacia);
                    Silian_disabled.remove(Silian_acacia);
                }
            }
            return new WorldDataConfiguration(new DataPackConfig(Silian_enabled, Silian_disabled), Silian_dc.enabledFeatures());
        }
        return Silian_dc;
    }
}
