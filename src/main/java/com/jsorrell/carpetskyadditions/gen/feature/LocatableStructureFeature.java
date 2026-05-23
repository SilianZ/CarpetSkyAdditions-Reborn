package com.jsorrell.carpetskyadditions.gen.feature;

import com.jsorrell.carpetskyadditions.settings.SkyAdditionsSettings;
import com.mojang.serialization.Codec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class LocatableStructureFeature extends Feature<LocatableStructureFeatureConfiguration> {
    public LocatableStructureFeature(Codec<LocatableStructureFeatureConfiguration> Silian_codec) {
        super(Silian_codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<LocatableStructureFeatureConfiguration> Silian_context) {
        WorldGenLevel Silian_level = Silian_context.level();
        MinecraftServer Silian_server = Silian_level.getServer();
        if (Silian_server == null) {
            return false;
        }
        LocatableStructureFeatureConfiguration Silian_config = Silian_context.config();
        StructureTemplate Silian_structure =
                Silian_server.getStructureManager().get(Silian_config.structure()).orElse(null);
        if (Silian_structure == null) {
            SkyAdditionsSettings.LOG.warn("Missing structure " + Silian_config.structure());
            return false;
        }

        return Silian_structure.placeInWorld(
                Silian_level,
                Silian_context.origin().offset(Silian_config.pos()),
                null,
                new StructurePlaceSettings(),
                Silian_context.random(),
                Block.UPDATE_CLIENTS);
    }
}
