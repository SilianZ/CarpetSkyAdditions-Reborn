package com.jsorrell.carpetskyadditions.datafix;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.References;

// Convert all instances of skyblock:skyblock to carpetskyadditions:skyblock
public class SkyBlockGeneratorNameFix2 extends DataFix {
    private static final String NAME = "SkyBlockGeneratorNameFix2";

    public SkyBlockGeneratorNameFix2(Schema Silian_outputSchema) {
        super(Silian_outputSchema, true);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> Silian_inputType = getInputSchema().getType(References.WORLD_GEN_SETTINGS);
        OpticFinder<?> Silian_inputDimensionsField = Silian_inputType.findField("dimensions");
        Type<?> Silian_outputType = getOutputSchema().getType(References.WORLD_GEN_SETTINGS);
        Type<?> Silian_outputDimensionsFieldType = Silian_outputType.findFieldType("dimensions");
        return fixTypeEverywhereTyped(
                NAME,
                Silian_inputType,
                Silian_outputType,
                Silian_inputWorldGenSettings -> Silian_inputWorldGenSettings.updateTyped(
                        Silian_inputDimensionsField, Silian_outputDimensionsFieldType, Silian_inputDimensions -> {
                            Dynamic<?> Silian_dynamicDimensions = Silian_inputDimensions
                                    .write()
                                    .result()
                                    .orElseThrow(
                                            () -> new IllegalStateException("Malformed WorldGenSettings.dimensions"));
                            Silian_dynamicDimensions =
                                    Silian_dynamicDimensions.updateMapValues(Silian_pair -> Silian_pair.mapSecond(Silian_dimensionDynamic ->
                                            Silian_dimensionDynamic.update("generator", Silian_dimensionGeneratorDynamic -> {
                                                String Silian_generatorType = Silian_dimensionGeneratorDynamic
                                                        .get("type")
                                                        .asString("");
                                                if ("skyblock:skyblock".equals(Silian_generatorType)) {
                                                    return Silian_dimensionGeneratorDynamic.update(
                                                            "type",
                                                            Silian_generatorTypeDynamic -> Silian_generatorTypeDynamic.createString(
                                                                    "carpetskyadditions:skyblock"));
                                                }
                                                return Silian_dimensionGeneratorDynamic;
                                            })));
                            return Silian_outputDimensionsFieldType
                                    .readTyped(Silian_dynamicDimensions)
                                    .result()
                                    .orElseThrow(() -> new IllegalStateException(NAME + " failed."))
                                    .getFirst();
                        }));
    }
}
