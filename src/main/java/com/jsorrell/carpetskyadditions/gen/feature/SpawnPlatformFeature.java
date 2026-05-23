package com.jsorrell.carpetskyadditions.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class SpawnPlatformFeature extends Feature<SpawnPlatformFeatureConfiguration> {
    public SpawnPlatformFeature(Codec<SpawnPlatformFeatureConfiguration> Silian_codec) {
        super(Silian_codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<SpawnPlatformFeatureConfiguration> Silian_context) {
        SpawnPlatformFeatureConfiguration Silian_config = Silian_context.config();
        // Always absolute with Y
        BlockPos Silian_origin = Silian_config.spawnRelative() ? Silian_context.origin().atY(0) : BlockPos.ZERO;

        return SkyAdditionsFeatures.LOCATABLE_STRUCTURE.place(
                Silian_config.platformConfig(), Silian_context.level(), Silian_context.chunkGenerator(), Silian_context.random(), Silian_origin);
    }
}
