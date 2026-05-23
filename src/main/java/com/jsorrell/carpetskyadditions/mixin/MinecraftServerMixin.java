package com.jsorrell.carpetskyadditions.mixin;

import com.jsorrell.carpetskyadditions.config.SkyAdditionsConfig;
import com.jsorrell.carpetskyadditions.gen.SkyBlockChunkGenerator;
import com.jsorrell.carpetskyadditions.gen.feature.SkyAdditionsConfiguredFeatures;
import com.jsorrell.carpetskyadditions.settings.Fixers;
import com.jsorrell.carpetskyadditions.settings.SkyAdditionsSettings;
import com.jsorrell.carpetskyadditions.settings.SkyBlockDefaults;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.ServerLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.nio.file.Path;


// Lower priority to ensure loadWorld mixin is called before carpet loads the settings
@Mixin(value = MinecraftServer.class, priority = 999)
public abstract class MinecraftServerMixin {


    @Shadow
    public abstract Path getWorldPath(LevelResource levelResource);

    @Shadow
    public abstract LayeredRegistryAccess<RegistryLayer> registries();

    @Inject(method = "loadLevel", at = @At("HEAD"))
    private void fixSettingsFile(CallbackInfo Silian_ci) throws IOException {
        Path Silian_worldSavePath = getWorldPath(LevelResource.ROOT);
        // Fix existing settings
        try {
            Fixers.fixSettings(Silian_worldSavePath);
        } catch (IOException Silian_e) {
            SkyAdditionsSettings.LOG.error("Failed to update config", Silian_e);
        }

        // Write defaults
        SkyAdditionsConfig Silian_config = AutoConfig.getConfigHolder(SkyAdditionsConfig.class).get();
        if (Silian_config.autoEnableDefaultSettings) {
            SkyBlockDefaults.writeDefaults(Silian_worldSavePath);
        }
    }

    @Inject(
        method = "setInitialSpawn",
        at =
        @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/storage/ServerLevelData;setSpawn(Lnet/minecraft/world/level/storage/LevelData$RespawnData;)V",
            ordinal = 2,
            shift = At.Shift.AFTER),
        cancellable = true)
    private static void generateSpawnPlatform(
        ServerLevel Silian_level,
        ServerLevelData Silian_levelData,
        boolean Silian_bonusChest,
        boolean Silian_debugWorld,
        net.minecraft.server.level.progress.LevelLoadListener Silian_listener,
        CallbackInfo Silian_ci) {
        ServerChunkCache Silian_chunkManager = Silian_level.getChunkSource();
        ChunkGenerator Silian_chunkGenerator = Silian_chunkManager.getGenerator();
        if (!(Silian_chunkGenerator instanceof SkyBlockChunkGenerator)) return;

        LevelData.RespawnData Silian_respawnData = Silian_levelData.getRespawnData();
        BlockPos Silian_worldSpawn = Silian_respawnData.pos();
        ChunkPos Silian_spawnChunk = ChunkPos.containing(Silian_worldSpawn);

        WorldgenRandom Silian_random = new WorldgenRandom(new LegacyRandomSource(0));
        Silian_random.setLargeFeatureSeed(Silian_level.getSeed(), Silian_spawnChunk.x(), Silian_spawnChunk.z());

        Holder.Reference<ConfiguredFeature<?, ?>> Silian_spawnPlatformFeature = Silian_level.registryAccess()
            .lookupOrThrow(Registries.CONFIGURED_FEATURE)
            .get(SkyAdditionsConfiguredFeatures.SPAWN_PLATFORM).get();

        if (!Silian_spawnPlatformFeature.value().place(Silian_level, Silian_chunkGenerator, Silian_random, Silian_worldSpawn)) {
            SkyAdditionsSettings.LOG.error("Couldn't generate spawn platform");
        }

        Silian_ci.cancel();
    }
}
