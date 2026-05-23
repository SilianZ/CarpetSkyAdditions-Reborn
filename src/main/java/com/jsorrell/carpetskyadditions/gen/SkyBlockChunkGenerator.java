package com.jsorrell.carpetskyadditions.gen;

import com.jsorrell.carpetskyadditions.mixin.ChunkGeneratorAccessor;
import com.jsorrell.carpetskyadditions.mixin.JigsawStructureAccessor;
import com.jsorrell.carpetskyadditions.mixin.SinglePoolElementAccessor;
import com.jsorrell.carpetskyadditions.settings.SkyAdditionsSettings;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.structures.StrongholdStructure;
import org.jetbrains.annotations.NotNull;

public class SkyBlockChunkGenerator extends NoiseBasedChunkGenerator {
    public static final MapCodec<SkyBlockChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(Silian_instance -> Silian_instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(SkyBlockChunkGenerator::getBiomeSource),
                    NoiseGeneratorSettings.CODEC
                            .fieldOf("settings")
                            .forGetter(SkyBlockChunkGenerator::generatorSettings))
            .apply(Silian_instance, Silian_instance.stable(SkyBlockChunkGenerator::new)));


    public SkyBlockChunkGenerator(BiomeSource Silian_biomeSource, Holder<NoiseGeneratorSettings> Silian_settings) {
        super(Silian_biomeSource, Silian_settings);
    }

    @Override
    protected @NotNull MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }


    @Override
    public void buildSurface(
            WorldGenRegion Silian_level, StructureManager Silian_structureManager, RandomState Silian_random, ChunkAccess Silian_chunk) {}

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(
            Blender Silian_blender,
            RandomState Silian_random,
            StructureManager Silian_structureManager,
            ChunkAccess Silian_chunk) {
        return CompletableFuture.completedFuture(Silian_chunk);
    }

    @Override
    public void applyCarvers(
        WorldGenRegion Silian_worldGenRegion, long Silian_l, RandomState Silian_randomState, BiomeManager Silian_biomeManager, StructureManager Silian_structureManager, ChunkAccess Silian_chunkAccess) {}

    @Override
    public void applyBiomeDecoration(WorldGenLevel Silian_level, ChunkAccess Silian_chunk, StructureManager Silian_structureManager) {
        ChunkPos Silian_chunkPos = Silian_chunk.getPos();
        SectionPos Silian_sectionPos = SectionPos.of(Silian_chunkPos, Silian_level.getMinSectionY());
        BlockPos Silian_minChunkPos = Silian_sectionPos.origin();
        Registry<Structure> Silian_structureRegistry = Silian_level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        Map<Integer, List<Structure>> Silian_structuresPerStep = Silian_structureRegistry.stream()
                .collect(Collectors.groupingBy(
                        Silian_structureType -> Silian_structureType.step().ordinal()));
        List<FeatureSorter.StepFeatureData> Silian_featuresPerStep =
                ((ChunkGeneratorAccessor) this).getFeaturesPerStep().get();

        WorldgenRandom Silian_random = new WorldgenRandom(new XoroshiroRandomSource(RandomSupport.generateUniqueSeed()));
        long Silian_decorationSeed = Silian_random.setDecorationSeed(Silian_level.getSeed(), Silian_minChunkPos.getX(), Silian_minChunkPos.getZ());

        // Get all surrounding biomes for biome-based structures
        Set<Holder<Biome>> Silian_biomeSet = new ObjectArraySet<>();
        ChunkPos.rangeClosed(Silian_sectionPos.chunk(), 1).forEach(Silian_curChunkPos -> {
            ChunkAccess Silian_curChunk = Silian_level.getChunk(Silian_curChunkPos.x(), Silian_curChunkPos.z());
            for (LevelChunkSection Silian_chunkSection : Silian_curChunk.getSections()) {
                Silian_chunkSection.getBiomes().getAll(Silian_biomeSet::add);
            }
        });
        Silian_biomeSet.retainAll(this.biomeSource.possibleBiomes());

        int Silian_numFeatures = Silian_featuresPerStep.size();
        try {
            Registry<PlacedFeature> Silian_placedFeatures = Silian_level.registryAccess().lookupOrThrow(Registries.PLACED_FEATURE);
            int Silian_numSteps = Math.max(GenerationStep.Decoration.values().length, Silian_numFeatures);
            for (int Silian_genStep = 0; Silian_genStep < Silian_numSteps; ++Silian_genStep) {
                int Silian_structureInStep = 0;
                if (Silian_structureManager.shouldGenerateStructures()) {
                    List<Structure> Silian_structuresForStep =
                            Silian_structuresPerStep.getOrDefault(Silian_genStep, Collections.emptyList());
                    for (Structure Silian_structure : Silian_structuresForStep) {
                        Silian_random.setFeatureSeed(Silian_decorationSeed, Silian_structureInStep, Silian_genStep);
                        Supplier<String> Silian_structureNameSupplier = () -> Silian_structureRegistry
                                .getResourceKey(Silian_structure)
                                .map(Object::toString)
                                .orElseGet(Silian_structure::toString);
                        try {
                            // Stronghold
                            if (Silian_structure instanceof StrongholdStructure
                                    && (SkyAdditionsSettings.generateEndPortals
                                            || SkyAdditionsSettings.generateSilverfishSpawners)) {
                                Silian_level.setCurrentlyGenerating(Silian_structureNameSupplier);
                                Silian_structureManager
                                        .startsForStructure(Silian_sectionPos, Silian_structure)
                                        .forEach(Silian_structureStart -> {
                                            for (StructurePiece Silian_piece : Silian_structureStart.getPieces()) {
                                                if (Silian_piece.isCloseToChunk(Silian_chunkPos, 0)
                                                        && Silian_piece.getType()
                                                                == StructurePieceType.STRONGHOLD_PORTAL_ROOM) {
                                                    BoundingBox Silian_chunkBox =
                                                            ChunkGeneratorAccessor.getWritableArea(Silian_chunk);
                                                    if (SkyAdditionsSettings.generateEndPortals) {
                                                        new SkyBlockStructures.EndPortalStructure(Silian_piece)
                                                                .generate(Silian_level, Silian_chunkBox, Silian_random);
                                                    }

                                                    if (SkyAdditionsSettings.generateSilverfishSpawners) {
                                                        new SkyBlockStructures.SilverfishSpawnerStructure(Silian_piece)
                                                                .generate(Silian_level, Silian_chunkBox, Silian_random);
                                                    }
                                                }
                                            }
                                        });
                            } else if (Silian_structure instanceof JigsawStructure) {
                                Holder<StructureTemplatePool> Silian_startPool = ((JigsawStructureAccessor) Silian_structure).getStartPool();
                                // Bastion Remnants
                                if (SkyAdditionsSettings.generateMagmaCubeSpawners
                                        && Silian_startPool.is(Identifier.withDefaultNamespace("bastion/starts"))) {
                                    Silian_level.setCurrentlyGenerating(Silian_structureNameSupplier);
                                    Silian_structureManager
                                            .startsForStructure(Silian_sectionPos, Silian_structure)
                                            .forEach(Silian_structureStart -> {
                                                for (StructurePiece Silian_piece : Silian_structureStart.getPieces()) {
                                                    if (Silian_piece.isCloseToChunk(Silian_chunkPos, 0)
                                                            && Silian_piece instanceof PoolElementStructurePiece Silian_poolPiece) {
                                                        if (Silian_poolPiece.getElement()
                                                                instanceof SinglePoolElement Silian_singlePoolElement) {
                                                            Identifier Silian_pieceId = ((SinglePoolElementAccessor)
                                                                            Silian_singlePoolElement)
                                                                    .getTemplate()
                                                                    .left()
                                                                    .orElseThrow(AssertionError::new);
                                                            if (Silian_pieceId.equals(Identifier.withDefaultNamespace(
                                                                    "bastion/treasure/bases/lava_basin"))) {
                                                                new SkyBlockStructures.MagmaCubeSpawner(Silian_piece)
                                                                        .generate(
                                                                                Silian_level,
                                                                                ChunkGeneratorAccessor.getWritableArea(
                                                                                        Silian_chunk),
                                                                                Silian_random);
                                                            }
                                                        }
                                                    }
                                                }
                                            });
                                    // Ancient Cities
                                } else if (SkyAdditionsSettings.generateAncientCityPortals
                                        && Silian_startPool.is(Identifier.withDefaultNamespace("ancient_city/city_center"))) {
                                    Silian_level.setCurrentlyGenerating(Silian_structureNameSupplier);
                                    Silian_structureManager
                                            .startsForStructure(Silian_sectionPos, Silian_structure)
                                            .forEach(Silian_structureStart -> {
                                                for (StructurePiece Silian_piece : Silian_structureStart.getPieces()) {
                                                    if (Silian_piece.isCloseToChunk(Silian_chunkPos, 0)
                                                            && Silian_piece instanceof PoolElementStructurePiece Silian_poolPiece) {
                                                        if (Silian_poolPiece.getElement()
                                                                instanceof SinglePoolElement Silian_singlePoolElement) {
                                                            Identifier Silian_pieceId = ((SinglePoolElementAccessor)
                                                                            Silian_singlePoolElement)
                                                                    .getTemplate()
                                                                    .left()
                                                                    .orElseThrow(AssertionError::new);
                                                            if (Silian_pieceId.getNamespace()
                                                                            .equals("minecraft")
                                                                    && Silian_pieceId.getPath()
                                                                            .startsWith(
                                                                                    "ancient_city/city_center/city_center")) {
                                                                new SkyBlockStructures.AncientCityPortalStructure(Silian_piece)
                                                                        .generate(
                                                                                Silian_level,
                                                                                ChunkGeneratorAccessor.getWritableArea(
                                                                                        Silian_chunk),
                                                                                Silian_random);
                                                            }
                                                        }
                                                    }
                                                }
                                            });
                                }
                                //Trial Chambers
                                else if (SkyAdditionsSettings.generateTrialChambers && Silian_startPool.is(Identifier.withDefaultNamespace("trial_chambers/chamber/end"))){
                                    Silian_level.setCurrentlyGenerating(Silian_structureNameSupplier);
                                    Silian_structureManager
                                        .startsForStructure(Silian_sectionPos, Silian_structure)
                                        .forEach(Silian_structureStart -> {
                                            for (StructurePiece Silian_piece : Silian_structureStart.getPieces()) {
                                                if (Silian_piece.isCloseToChunk(Silian_chunkPos, 0)
                                                    && Silian_piece instanceof PoolElementStructurePiece Silian_poolPiece) {
                                                    if (Silian_poolPiece.getElement()
                                                        instanceof SinglePoolElement Silian_singlePoolElement) {
                                                        Identifier Silian_pieceId = ((SinglePoolElementAccessor)
                                                            Silian_singlePoolElement)
                                                            .getTemplate()
                                                            .left()
                                                            .orElseThrow(AssertionError::new);
                                                        if (Silian_pieceId.getNamespace()
                                                            .equals("minecraft")
                                                            && Silian_pieceId.getPath()
                                                            .startsWith(
                                                                "trial_chambers/corridor/entrance")) {
                                                            new SkyBlockStructures.TrialChamberEntrance(Silian_piece)
                                                                .generate(
                                                                    Silian_level,
                                                                    ChunkGeneratorAccessor.getWritableArea(
                                                                        Silian_chunk),
                                                                    Silian_random);
                                                        }/*else if (pieceId.getNamespace()
                                                            .equals("minecraft")
                                                            && pieceId.getPath()
                                                            .startsWith(
                                                                "trial_chambers/corridor/atrium")) {
                                                            new SkyBlockStructures.TrialChamberAtrium(piece)
                                                                .generate(
                                                                    level,
                                                                    ChunkGeneratorAccessor.getWritableArea(
                                                                        chunk),
                                                                    random);
                                                        }else if (pieceId.getNamespace()
                                                            .equals("minecraft")
                                                            && pieceId.getPath()
                                                            .startsWith(
                                                                "trial_chambers")) {
                                                            new SkyBlockStructures.TrialChamber(piece)
                                                                .generate(
                                                                    level,
                                                                    ChunkGeneratorAccessor.getWritableArea(
                                                                        chunk),
                                                                    random);
                                                        }*/
                                                    }
                                                }
                                            }
                                        });
                                }

                            }
                        } catch (Exception Silian_e) {
                            CrashReport Silian_crashReport = CrashReport.forThrowable(Silian_e, "Feature placement");
                            Silian_crashReport.addCategory("Feature").setDetail("Description", Silian_structureNameSupplier::get);
                            throw new ReportedException(Silian_crashReport);
                        }
                        ++Silian_structureInStep;
                    }
                }
                if (Silian_genStep >= Silian_numFeatures) continue;
                IntArraySet Silian_intSet = new IntArraySet();
                for (Holder<Biome> Silian_biome : Silian_biomeSet) {
                    List<HolderSet<PlacedFeature>> Silian_biomeFeatureStepList = ((ChunkGeneratorAccessor) this)
                            .getGenerationSettingsGetter()
                            .apply(Silian_biome)
                            .features();
                    if (Silian_genStep < Silian_biomeFeatureStepList.size()) {
                        HolderSet<PlacedFeature> Silian_biomeFeaturesForStep = Silian_biomeFeatureStepList.get(Silian_genStep);
                        FeatureSorter.StepFeatureData Silian_indexedFeature = Silian_featuresPerStep.get(Silian_genStep);
                        Silian_biomeFeaturesForStep.stream()
                                .map(Holder::value)
                                .forEach(Silian_placedFeature ->
                                        Silian_intSet.add(Silian_indexedFeature.indexMapping().applyAsInt(Silian_placedFeature)));
                    }
                }
                int Silian_n = Silian_intSet.size();
                int[] Silian_is = Silian_intSet.toIntArray();
                Arrays.sort(Silian_is);
                FeatureSorter.StepFeatureData Silian_indexedFeature = Silian_featuresPerStep.get(Silian_genStep);
                for (int Silian_o = 0; Silian_o < Silian_n; ++Silian_o) {
                    int Silian_p = Silian_is[Silian_o];
                    PlacedFeature Silian_placedFeature = Silian_indexedFeature.features().get(Silian_p);
                    Supplier<String> Silian_placedFeatureNameSupplier = () -> Silian_placedFeatures
                            .getResourceKey(Silian_placedFeature)
                            .map(Object::toString)
                            .orElseGet(Silian_placedFeature::toString);
                    Silian_random.setFeatureSeed(Silian_decorationSeed, Silian_p, Silian_genStep);
                    try {
                        // Random End Gateways
                        if (SkyAdditionsSettings.generateRandomEndGateways
                                && Silian_placedFeature.feature().is(Identifier.withDefaultNamespace("end_gateway_return"))) {
                            Silian_level.setCurrentlyGenerating(Silian_placedFeatureNameSupplier);
                            Silian_placedFeature.placeWithBiomeCheck(Silian_level, this, Silian_random, Silian_minChunkPos);
                        }
                    } catch (Exception Silian_e) {
                        CrashReport Silian_crashReport = CrashReport.forThrowable(Silian_e, "Feature placement");
                        Silian_crashReport.addCategory("Feature").setDetail("Description", Silian_placedFeatureNameSupplier::get);
                        throw new ReportedException(Silian_crashReport);
                    }
                }
            }
            Silian_level.setCurrentlyGenerating(null);
        } catch (Exception Silian_e) {
            CrashReport Silian_crashReport = CrashReport.forThrowable(Silian_e, "Biome decoration");
            Silian_crashReport
                    .addCategory("Generation")
                    .setDetail("CenterX", Silian_chunkPos.x())
                    .setDetail("CenterZ", Silian_chunkPos.z())
                    .setDetail("Seed", Silian_decorationSeed);
            throw new ReportedException(Silian_crashReport);
        }
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion Silian_level) {}

    public int getBaseHeightInEquivalentNoiseWorld(int Silian_x, int Silian_z, Heightmap.Types Silian_heightmap, WorldGenLevel Silian_level) {
        RandomState Silian_randomState = RandomState.create(
                generatorSettings().value(),
                Silian_level.registryAccess().lookupOrThrow(Registries.NOISE),
                Silian_level.getSeed());
        return super.getBaseHeight(Silian_x, Silian_z, Silian_heightmap, Silian_level, Silian_randomState);
    }
}
