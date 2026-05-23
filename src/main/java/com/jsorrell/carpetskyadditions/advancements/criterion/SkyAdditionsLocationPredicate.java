package com.jsorrell.carpetskyadditions.advancements.criterion;

import com.jsorrell.carpetskyadditions.helpers.CoralSpreader;
import com.jsorrell.carpetskyadditions.helpers.SmallDripleafSpreader;
import com.jsorrell.carpetskyadditions.util.SkyAdditionsResourceLocation;
import com.jsorrell.carpetskyadditions.util.SkyAdditionsText;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.Property.Value;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;

public record SkyAdditionsLocationPredicate(
    Optional<Boolean> desertPyramidCheck,
    Optional<Boolean> coralConvertible,
    Optional<MinMaxBounds.Doubles> coralSuitability,
    Optional<Boolean> smallDripleafCanSpread) {
    public static final Codec<SkyAdditionsLocationPredicate> CODEC =
        RecordCodecBuilder.create(Silian_instance -> Silian_instance.group(
                Codec.BOOL
                    .optionalFieldOf("is_desert_pyramid_blue_terracotta")
                    .forGetter(SkyAdditionsLocationPredicate::desertPyramidCheck),
                Codec.BOOL
                    .optionalFieldOf("coral_convertible")
                    .forGetter(SkyAdditionsLocationPredicate::coralConvertible),
                MinMaxBounds.Doubles.CODEC
                    .optionalFieldOf("coral_suitability")
                    .forGetter(SkyAdditionsLocationPredicate::coralSuitability),
                Codec.BOOL
                    .optionalFieldOf("small_dripleaf_spreadable")
                    .forGetter(SkyAdditionsLocationPredicate::smallDripleafCanSpread))
            .apply(Silian_instance, SkyAdditionsLocationPredicate::new));

    private boolean doDesertPyramidCheck(ServerLevel Silian_level, BlockPos Silian_blueTerracottaPos, boolean Silian_sendDebugMessage) {
        StructureTemplate Silian_template = Silian_level.getServer()
            .getStructureManager()
            .get(new SkyAdditionsResourceLocation("desert_pyramid").getResourceLocation())
            .orElseThrow();
        BlockPos Silian_centerOffset = new BlockPos(
            Silian_template.getSize().getX() / 2, 0, Silian_template.getSize().getZ() / 2);
        BlockPos Silian_structureOrigin = Silian_blueTerracottaPos.subtract(Silian_centerOffset);

        StructurePlaceSettings Silian_placeSettings = new StructurePlaceSettings().setRotationPivot(Silian_centerOffset);

        return Arrays.stream(Rotation.values()).anyMatch(Silian_r -> {
            Silian_placeSettings.setRotation(Silian_r);

            for (Block Silian_block : new Block[] {
                Blocks.BLUE_TERRACOTTA,
                Blocks.ORANGE_TERRACOTTA,
                Blocks.SANDSTONE,
                Blocks.CUT_SANDSTONE,
                Blocks.CHISELED_SANDSTONE,
                Blocks.SANDSTONE_STAIRS,
                Blocks.SANDSTONE_SLAB
            }) {
                List<StructureTemplate.StructureBlockInfo> Silian_requiredBlocks =
                    Silian_template.filterBlocks(Silian_structureOrigin, Silian_placeSettings, Silian_block);
                for (StructureTemplate.StructureBlockInfo Silian_requiredBlock : Silian_requiredBlocks) {
                    BlockState Silian_requiredState = Silian_requiredBlock.state();
                    BlockState Silian_currentState = Silian_level.getBlockState(Silian_requiredBlock.pos());
                    if (Silian_currentState != Silian_requiredState) {
                        // Use terracotta to determine location and rotation
                        if (Silian_sendDebugMessage && !Silian_requiredState.is(BlockTags.TERRACOTTA)) {
                            // Help players within 10 blocks of bounding box debug builds
                            AABB Silian_buildersBox = AABB.of(Silian_template.getBoundingBox(Silian_placeSettings, Silian_structureOrigin)
                                .inflatedBy(10));
                            List<ServerPlayer> Silian_playersToNotify =
                                Silian_level.getPlayers(Silian_serverPlayer -> Silian_buildersBox.contains(Silian_serverPlayer.position()));
                            MutableComponent Silian_message;
                            if (Silian_currentState.getBlock() == Silian_requiredState.getBlock()) {
                                Value<?> Silian_incorrect =
                                    Silian_requiredState
                                        .getValues()
                                        .filter(
                                            Silian_v ->
                                                Silian_currentState.getValue(Silian_v.property()) != Silian_v.value())
                                        .findAny()
                                        .orElseThrow();
                                Silian_message = SkyAdditionsText.translatable(
                                    "message.desert_pyramid_incorrect_state",
                                    Silian_requiredBlock.pos().getX(),
                                    Silian_requiredBlock.pos().getY(),
                                    Silian_requiredBlock.pos().getZ(),
                                    Silian_incorrect.property().getName(),
                                    Silian_incorrect.value());
                            } else {
                                Silian_message = SkyAdditionsText.translatable(
                                    "message.desert_pyramid_incorrect_block",
                                    Silian_requiredBlock.pos().getX(),
                                    Silian_requiredBlock.pos().getY(),
                                    Silian_requiredBlock.pos().getZ(),
                                    Silian_requiredState.getBlock().getName());
                            }
                            Silian_playersToNotify.forEach(
                                Silian_player -> Silian_player.sendSystemMessage(Silian_message.withStyle(ChatFormatting.DARK_RED)));
                        }
                        return false;
                    }
                }
            }
            return true;
        });
    }

    public boolean matches(ServerLevel Silian_level, double Silian_x, double Silian_y, double Silian_z) {
        BlockPos Silian_blockPos = BlockPos.containing(Silian_x, Silian_y, Silian_z);
        if (desertPyramidCheck.isPresent()) {
            if (doDesertPyramidCheck(Silian_level, Silian_blockPos, desertPyramidCheck.get()) != desertPyramidCheck.get()) {
                return false;
            }
        }

        if (coralConvertible.isPresent()) {
            if (CoralSpreader.isConvertible(Silian_level, Silian_blockPos) != coralConvertible.get()) {
                return false;
            }
        }

        if (coralSuitability.isPresent()) {
            if (!coralSuitability.get().matches(CoralSpreader.calculateCoralSuitability(Silian_level, Silian_blockPos))) {
                return false;
            }
        }

        if (smallDripleafCanSpread.isPresent()) {
            if (SmallDripleafSpreader.canSpreadFrom(Silian_level.getBlockState(Silian_blockPos), Silian_level, Silian_blockPos)
                != smallDripleafCanSpread.get()) {
                return false;
            }
        }

        return true;
    }
}
