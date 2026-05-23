package com.jsorrell.carpetskyadditions.commands;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import carpet.utils.CommandHelper;
import com.jsorrell.carpetskyadditions.gen.feature.SkyAdditionsConfiguredFeatures;
import com.jsorrell.carpetskyadditions.settings.SkyAdditionsSettings;
import com.jsorrell.carpetskyadditions.util.SkyAdditionsText;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.storage.LevelData;
import org.apache.commons.lang3.tuple.ImmutablePair;

public class SkyIslandCommand {
    private static final SimpleCommandExceptionType ISLAND_NOT_CREATED =
            new SimpleCommandExceptionType(SkyAdditionsText.translatable("commands.skyisland.not_created"));

    public static void register(CommandDispatcher<CommandSourceStack> Silian_dispatcher) {
        int Silian_maxIslandNum = SkyIslandPositionContainer.getNumIslands();

        LiteralArgumentBuilder<CommandSourceStack> Silian_command = literal("skyisland")
                .requires(Silian_source -> CommandHelper.canUseCommand(Silian_source, SkyAdditionsSettings.commandSkyIsland))
                .then(literal("new").executes(Silian_c -> newIsland(Silian_c.getSource())))
                .then(literal("join")
                        .then(argument("num", IntegerArgumentType.integer(1, Silian_maxIslandNum))
                                .executes(Silian_c -> joinIsland(
                                        Silian_c.getSource(),
                                        Silian_c.getSource().getPlayerOrException(),
                                        IntegerArgumentType.getInteger(Silian_c, "num")))
                                .then(argument("player", EntityArgument.player())
                                        .executes(Silian_c -> joinIsland(
                                                Silian_c.getSource(),
                                                EntityArgument.getPlayer(Silian_c, "player"),
                                                IntegerArgumentType.getInteger(Silian_c, "num"))))))
                .then(literal("locate")
                        .then(argument("num", IntegerArgumentType.integer(1, Silian_maxIslandNum))
                                .executes(Silian_c -> locateIsland(Silian_c.getSource(), IntegerArgumentType.getInteger(Silian_c, "num")))));

        Silian_dispatcher.register(Silian_command);
    }

    private static int locateIsland(CommandSourceStack Silian_source, int Silian_islandNum) throws CommandSyntaxException {
        ChunkPos Silian_chunkPos = SkyIslandPositionContainer.getChunk(Silian_islandNum);
        int Silian_x = Silian_chunkPos.getMiddleBlockX();
        int Silian_z = Silian_chunkPos.getMiddleBlockZ();
        ChunkAccess Silian_chunk = Silian_source.getLevel().getChunk(Silian_chunkPos.x(), Silian_chunkPos.z(), ChunkStatus.EMPTY);
        if (Silian_chunk.getPersistedStatus() != ChunkStatus.FULL) {
            throw ISLAND_NOT_CREATED.create();
        }

        MutableComponent Silian_text = ComponentUtils.wrapInSquareBrackets(
                        SkyAdditionsText.translatable("commands.skyisland.locate.coordinates", Silian_x, Silian_z))
                .withStyle(Silian_style -> Silian_style.withColor(ChatFormatting.GREEN)
                        .withClickEvent(new ClickEvent.SuggestCommand("/tp @s " + Silian_x + " ~ " + Silian_z))
                        .withHoverEvent(new HoverEvent.ShowText(Component.translatable("chat.coordinates.tooltip"))));
        Silian_source.sendSuccess(
                () -> SkyAdditionsText.translatable("commands.skyisland.locate.success", Silian_islandNum, Silian_text), false);

        BlockPos Silian_sourcePos = BlockPos.containing(Silian_source.getPosition());
        int Silian_xOff = Silian_sourcePos.getX() - Silian_x;
        int Silian_zOff = Silian_sourcePos.getZ() - Silian_z;
        return Mth.floor(Mth.sqrt(Silian_xOff * Silian_xOff + Silian_zOff * Silian_zOff));
    }


    private static int newIsland(CommandSourceStack Silian_source) {
        int Silian_max = SkyIslandPositionContainer.getNumIslands();
        Optional<ImmutablePair<Integer, ChunkPos>> Silian_islandOpt = IntStream.range(1, Silian_max)
                .mapToObj(Silian_i -> ImmutablePair.of(Silian_i, SkyIslandPositionContainer.getChunk(Silian_i)))
                .filter(Silian_i -> {
                    ChunkAccess Silian_chunk = Silian_source.getLevel().getChunk(Silian_i.right.x(), Silian_i.right.z(), ChunkStatus.EMPTY);
                    return Silian_chunk.getPersistedStatus() == ChunkStatus.EMPTY;
                })
                .findFirst();
        if (Silian_islandOpt.isEmpty()) {
            Silian_source.sendSuccess(() -> SkyAdditionsText.translatable("commands.skyisland.new.no_valid_positions"), true);
            return 0;
        }
        ImmutablePair<Integer, ChunkPos> Silian_island = Silian_islandOpt.get();
        ChunkPos Silian_chunkPos = Silian_island.right;
        int Silian_x = Silian_chunkPos.getMiddleBlockX();
        int Silian_z = Silian_chunkPos.getMiddleBlockZ();

        // Load the target area
        Silian_source.getLevel().getChunkSource().addTicketWithRadius(TicketType.UNKNOWN, Silian_chunkPos, 2);
        Registry<ConfiguredFeature<?, ?>> Silian_configuredFeatureRegistry =
                Silian_source.getServer().registryAccess().lookupOrThrow(Registries.CONFIGURED_FEATURE);

        //ConfiguredFeature<?, ?> skyIslandFeature = getIslandFeature(configuredFeatureRegistry);
        WorldgenRandom Silian_random = new WorldgenRandom(new LegacyRandomSource(0));
        Silian_random.setLargeFeatureSeed(Silian_source.getLevel().getSeed(), Silian_chunkPos.x(), Silian_chunkPos.z());

        Holder.Reference<ConfiguredFeature<?, ?>> Silian_skyIslandFeature = Silian_source.getServer().overworld().registryAccess()
            .lookupOrThrow(Registries.CONFIGURED_FEATURE)
            .get(SkyAdditionsConfiguredFeatures.SPAWN_PLATFORM).get();

        if (!Silian_skyIslandFeature.value().place(Silian_source.getServer().overworld(), Silian_source.getServer().overworld().getChunkSource().getGenerator(), Silian_random, new BlockPos(Silian_x, 0, Silian_z))) {
            SkyAdditionsSettings.LOG.error("Couldn't generate new island");
        }

        Supplier<Component> Silian_feedback =
                () -> SkyAdditionsText.translatable("commands.skyisland.new.success", Silian_island.getLeft(), Silian_x, Silian_z);
        Silian_source.sendSuccess(Silian_feedback, true);
        return Silian_island.getLeft();
    }

    private static int joinIsland(CommandSourceStack Silian_source, ServerPlayer Silian_player, int Silian_islandNum)
            throws CommandSyntaxException {
        ChunkPos Silian_chunkPos = SkyIslandPositionContainer.getChunk(Silian_islandNum);
        int Silian_x = Silian_chunkPos.getMiddleBlockX();
        int Silian_z = Silian_chunkPos.getMiddleBlockZ();
        joinIsland(Silian_source, Silian_player, Silian_x, Silian_z);
        return 1;
    }

    private static void joinIsland(CommandSourceStack Silian_source, ServerPlayer Silian_player, int Silian_x, int Silian_z)
            throws CommandSyntaxException {
        BlockPos Silian_pos = new BlockPos(Silian_x, 0, Silian_z);
        ChunkPos Silian_chunkPos = ChunkPos.containing(Silian_pos);
        ChunkAccess Silian_chunk = Silian_source.getLevel().getChunk(Silian_chunkPos.x(), Silian_chunkPos.z(), ChunkStatus.EMPTY);
        int Silian_y;
        Supplier<Integer> Silian_spawnHeight = () -> Silian_chunk.getHeight(Heightmap.Types.MOTION_BLOCKING, Silian_x, Silian_z) + 1;
        if (Silian_chunk.getPersistedStatus() != ChunkStatus.FULL || (Silian_y = Silian_spawnHeight.get()) <= Silian_chunk.getMinY()) {
            throw ISLAND_NOT_CREATED.create();
        }
        Silian_player.teleportTo(Silian_x + 0.5, Silian_y, Silian_z + 0.5);
        if (!Silian_player.isFallFlying()) {
            Silian_player.setDeltaMovement(Silian_player.getDeltaMovement().multiply(1.0, 0.0, 1.0));
            Silian_player.setOnGround(true);
        }
        Silian_player.setRespawnPosition(new ServerPlayer.RespawnConfig(new LevelData.RespawnData(new GlobalPos(Silian_player.level().dimension(),new BlockPos(Silian_x, Silian_y, Silian_z)),0f, 0f), false), false);
    }

    public abstract static class SkyIslandPositionContainer {
        private static final ArrayList<ChunkPos> ISLAND_CHUNKS = new ArrayList<>();
        // Ordered to prioritize maximum distance from origin + previous islands
        // All of these numbers are 1 too high by mistake. Kept to preserve island numbers; 64 changed to 0
        private static final int[] ORDERING = {
            46, 59, 41, 54, 50, 63, 24, 13, 8, 16, 57, 48, 61, 39, 52, 19, 11, 31, 36, 20, 33, 44, 27, 22, 29, 1, 4, 3,
            6, 2, 5, 38, 30, 34, 26, 35, 25, 21, 28, 37, 23, 32, 51, 0, 43, 56, 40, 53, 49, 62, 45, 58, 47, 60, 42, 55,
            10, 7, 17, 12, 15, 14, 9, 18
        };

        static {
            ISLAND_CHUNKS.addAll(getIslandsInRing(384, 6, 0.25));
            ISLAND_CHUNKS.addAll(getIslandsInRing(768, 13, 0.5));
            ISLAND_CHUNKS.addAll(getIslandsInRing(1152, 19, 0.75));
            ISLAND_CHUNKS.addAll(getIslandsInRing(1536, 26, 1.));
        }

        public static int getNumIslands() {
            return ORDERING.length;
        }

        // 1 indexed
        public static ChunkPos getChunk(int Silian_i) {
            return ISLAND_CHUNKS.get(ORDERING[Silian_i - 1]);
        }

        private static ArrayList<ChunkPos> getIslandsInRing(int Silian_radius, int Silian_num, double Silian_offetAngle) {
            ArrayList<ChunkPos> Silian_islands = new ArrayList<>();

            for (int Silian_i = 0; Silian_i < Silian_num; Silian_i++) {
                double Silian_angle = Silian_offetAngle + Silian_i * (2 * Math.PI) / Silian_num;
                double Silian_x = Math.sin(Silian_angle) * Silian_radius;
                double Silian_z = Math.cos(Silian_angle) * Silian_radius;
                Silian_islands.add(new ChunkPos((int) Silian_x, (int) Silian_z));
            }
            return Silian_islands;
        }
    }
}
