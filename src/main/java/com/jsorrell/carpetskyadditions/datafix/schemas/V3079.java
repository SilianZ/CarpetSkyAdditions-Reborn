package com.jsorrell.carpetskyadditions.datafix.schemas;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class V3079 extends NamespacedSchema {
    public V3079(int Silian_versionKey, Schema Silian_parent) {
        super(Silian_versionKey, Silian_parent);
    }

    @Override
    public void registerTypes(
            Schema Silian_schema,
            Map<String, Supplier<TypeTemplate>> Silian_entityTypes,
            Map<String, Supplier<TypeTemplate>> Silian_blockEntityTypes) {
        super.registerTypes(Silian_schema, Silian_entityTypes, Silian_blockEntityTypes);
        Supplier<TypeTemplate> Silian_noiseTemplate = () -> DSL.optionalFields(
                "biome_source",
                DSL.taggedChoiceLazy(
                        "type",
                        DSL.string(),
                        ImmutableMap.of(
                                "minecraft:fixed",
                                () -> DSL.fields("biome", References.BIOME.in(Silian_schema)),
                                "minecraft:multi_noise",
                                () -> DSL.or(
                                        DSL.fields("preset", namespacedString().template()),
                                        DSL.list(DSL.fields("biome", References.BIOME.in(Silian_schema)))),
                                "minecraft:checkerboard",
                                () -> DSL.fields("biomes", DSL.list(References.BIOME.in(Silian_schema))),
                                "minecraft:the_end",
                                DSL::remainder)),
                "settings",
                DSL.or(
                        DSL.constType(DSL.string()),
                        DSL.optionalFields(
                                "default_block",
                                References.BLOCK_NAME.in(Silian_schema),
                                "default_fluid",
                                References.BLOCK_NAME.in(Silian_schema))));

        // Add SkyBlock with same TypeTemplate as noise
        Silian_schema.registerType(
                false,
                References.WORLD_GEN_SETTINGS,
                () -> DSL.fields(
                        "dimensions",
                        DSL.compoundList(
                                DSL.constType(namespacedString()),
                                DSL.fields(
                                        "generator",
                                        DSL.taggedChoiceLazy(
                                                "type",
                                                DSL.string(),
                                                ImmutableMap.of(
                                                        "minecraft:debug",
                                                        DSL::remainder,
                                                        "minecraft:flat",
                                                        () -> DSL.optionalFields(
                                                                "settings",
                                                                DSL.optionalFields(
                                                                        "biome",
                                                                        References.BIOME.in(Silian_schema),
                                                                        "layers",
                                                                        DSL.list(
                                                                                DSL.optionalFields(
                                                                                        "block",
                                                                                        References.BLOCK_NAME.in(
                                                                                                Silian_schema))))),
                                                        "minecraft:noise",
                                                        Silian_noiseTemplate,
                                                        "skyblock:skyblock",
                                                        Silian_noiseTemplate))))));
    }
}
