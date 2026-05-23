package com.jsorrell.carpetskyadditions.gen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record LocatableStructureFeatureConfiguration(Identifier structure, BlockPos pos)
        implements FeatureConfiguration {
    public static final Codec<LocatableStructureFeatureConfiguration> CODEC =
            RecordCodecBuilder.create(Silian_instance -> Silian_instance.group(
                            Identifier.CODEC.fieldOf("structure").forGetter(Silian_config -> Silian_config.structure),
                            BlockPos.CODEC.fieldOf("pos").forGetter(Silian_config -> Silian_config.pos))
                    .apply(Silian_instance, LocatableStructureFeatureConfiguration::new));
}
