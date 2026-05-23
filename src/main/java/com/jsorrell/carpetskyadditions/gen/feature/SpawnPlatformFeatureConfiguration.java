package com.jsorrell.carpetskyadditions.gen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record SpawnPlatformFeatureConfiguration(
        LocatableStructureFeatureConfiguration platformConfig, boolean spawnRelative) implements FeatureConfiguration {
    public static final Codec<SpawnPlatformFeatureConfiguration> CODEC =
            RecordCodecBuilder.create(Silian_instance -> Silian_instance.group(
                            LocatableStructureFeatureConfiguration.CODEC
                                    .fieldOf("platform")
                                    .forGetter(Silian_config -> Silian_config.platformConfig),
                            Codec.BOOL.fieldOf("spawn_relative").forGetter(Silian_config -> Silian_config.spawnRelative))
                    .apply(Silian_instance, SpawnPlatformFeatureConfiguration::new));
}
