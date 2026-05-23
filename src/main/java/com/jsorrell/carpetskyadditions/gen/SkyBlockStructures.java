package com.jsorrell.carpetskyadditions.gen;

import java.util.Objects;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.block.entity.trialspawner.PlayerDetector;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerConfigs;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultConfig;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;


public class SkyBlockStructures {
    protected record StructureOrientation(Rotation rotation, Mirror mirror) {
        private int applyXTransform(int Silian_x, int Silian_z, BoundingBox boundingBox) {
            if ((rotation == Rotation.NONE && mirror != Mirror.FRONT_BACK)
                || (rotation == Rotation.CLOCKWISE_180 && mirror == Mirror.FRONT_BACK)) {
                return boundingBox.minX() + Silian_x;
            } else if (rotation == Rotation.NONE || rotation == Rotation.CLOCKWISE_180) {
                return boundingBox.maxX() - Silian_x;
            } else if ((rotation == Rotation.COUNTERCLOCKWISE_90 && mirror != Mirror.LEFT_RIGHT)
                || (rotation == Rotation.CLOCKWISE_90 && mirror == Mirror.LEFT_RIGHT)) {
                return boundingBox.minX() + Silian_z;
            } else {
                return boundingBox.maxX() - Silian_z;
            }
        }

        private int applyZTransform(int Silian_x, int Silian_z, BoundingBox boundingBox) {
            if ((rotation == Rotation.NONE && mirror != Mirror.LEFT_RIGHT)
                || (rotation == Rotation.CLOCKWISE_180 && mirror == Mirror.LEFT_RIGHT)) {
                return boundingBox.minZ() + Silian_z;
            } else if (rotation == Rotation.NONE || rotation == Rotation.CLOCKWISE_180) {
                return boundingBox.maxZ() - Silian_z;
            } else if ((rotation == Rotation.CLOCKWISE_90 && mirror != Mirror.FRONT_BACK)
                || (rotation == Rotation.COUNTERCLOCKWISE_90 && mirror == Mirror.LEFT_RIGHT)) {
                return boundingBox.minZ() + Silian_x;
            } else {
                return boundingBox.maxZ() - Silian_x;
            }
        }
    }

    protected abstract static class SkyBlockStructure {
        protected BoundingBox boundingBox;
        protected StructureOrientation orientation;
        protected Rotation rotation;
        protected Mirror mirror;

        public SkyBlockStructure(StructurePiece Silian_piece) {
            boundingBox = Silian_piece.getBoundingBox();
            rotation = Objects.requireNonNullElse(Silian_piece.getRotation(), Rotation.NONE);
            mirror = Objects.requireNonNullElse(Silian_piece.getMirror(), Mirror.NONE);
            orientation = new StructureOrientation(rotation, mirror);
        }

        protected int applyXTransform(int Silian_x, int Silian_z) {
            return orientation.applyXTransform(Silian_x, Silian_z, boundingBox);
        }

        protected int applyYTransform(int Silian_y) {
            return Silian_y + boundingBox.minY();
        }

        protected int applyZTransform(int Silian_x, int Silian_z) {
            return orientation.applyZTransform(Silian_x, Silian_z, boundingBox);
        }

        protected BlockPos.MutableBlockPos offsetPos(int Silian_x, int Silian_y, int Silian_z) {
            return new BlockPos.MutableBlockPos(applyXTransform(Silian_x, Silian_z), applyYTransform(Silian_y), applyZTransform(Silian_x, Silian_z));
        }

        protected BlockPos.MutableBlockPos addBlock(ServerLevelAccessor Silian_level, BlockState Silian_block, int Silian_x, int Silian_y, int Silian_z, BoundingBox Silian_bounds) {
            BlockPos.MutableBlockPos Silian_blockPos = offsetPos(Silian_x, Silian_y, Silian_z);
            if (!Silian_bounds.isInside(Silian_blockPos)) {
                return Silian_blockPos;
            }
            if (mirror != Mirror.NONE) {
                Silian_block = Silian_block.mirror(mirror);
            }
            if (rotation != Rotation.NONE) {
                Silian_block = Silian_block.rotate(rotation);
            }
            Silian_level.getChunk(Silian_blockPos).setInhabitedTime(6000);
            Silian_level.setBlock(Silian_blockPos, Silian_block, Block.UPDATE_CLIENTS);
            return Silian_blockPos;
        }

        protected void fillBlocks(
            ServerLevelAccessor Silian_level,
            BlockState Silian_block,
            int Silian_minX,
            int Silian_minY,
            int Silian_minZ,
            int Silian_maxX,
            int Silian_maxY,
            int Silian_maxZ,
            BoundingBox Silian_bounds) {
            for (int Silian_x = Silian_minX; Silian_x <= Silian_maxX; ++Silian_x) {
                for (int Silian_y = Silian_minY; Silian_y <= Silian_maxY; ++Silian_y) {
                    for (int Silian_z = Silian_minZ; Silian_z <= Silian_maxZ; ++Silian_z) {
                        addBlock(Silian_level, Silian_block, Silian_x, Silian_y, Silian_z, Silian_bounds);
                    }
                }
            }
        }

        public abstract void generate(ServerLevelAccessor Silian_level, BoundingBox Silian_bounds, RandomSource Silian_random);
    }

    public static class EndPortalStructure extends SkyBlockStructure {
        public EndPortalStructure(StructurePiece Silian_piece) {
            super(Silian_piece);
        }

        @Override
        public void generate(ServerLevelAccessor Silian_level, BoundingBox Silian_bounds, RandomSource Silian_random) {
            BlockState Silian_northFrame = Blocks.END_PORTAL_FRAME.defaultBlockState();
            BlockState Silian_southFrame = Silian_northFrame.setValue(EndPortalFrameBlock.FACING, Direction.SOUTH);
            BlockState Silian_eastFrame = Silian_northFrame.setValue(EndPortalFrameBlock.FACING, Direction.EAST);
            BlockState Silian_westFrame = Silian_northFrame.setValue(EndPortalFrameBlock.FACING, Direction.WEST);

            boolean Silian_complete = true;
            boolean[] Silian_hasEye = new boolean[12];
            for (int Silian_l = 0; Silian_l < Silian_hasEye.length; ++Silian_l) {
                Silian_hasEye[Silian_l] = Silian_random.nextFloat() > 0.9f;
                Silian_complete &= Silian_hasEye[Silian_l];
            }

            addBlock(Silian_level, Silian_southFrame.setValue(EndPortalFrameBlock.HAS_EYE, Silian_hasEye[0]), 4, 3, 3, Silian_bounds);
            addBlock(Silian_level, Silian_southFrame.setValue(EndPortalFrameBlock.HAS_EYE, Silian_hasEye[1]), 5, 3, 3, Silian_bounds);
            addBlock(Silian_level, Silian_southFrame.setValue(EndPortalFrameBlock.HAS_EYE, Silian_hasEye[2]), 6, 3, 3, Silian_bounds);
            addBlock(Silian_level, Silian_northFrame.setValue(EndPortalFrameBlock.HAS_EYE, Silian_hasEye[3]), 4, 3, 7, Silian_bounds);
            addBlock(Silian_level, Silian_northFrame.setValue(EndPortalFrameBlock.HAS_EYE, Silian_hasEye[4]), 5, 3, 7, Silian_bounds);
            addBlock(Silian_level, Silian_northFrame.setValue(EndPortalFrameBlock.HAS_EYE, Silian_hasEye[5]), 6, 3, 7, Silian_bounds);
            addBlock(Silian_level, Silian_eastFrame.setValue(EndPortalFrameBlock.HAS_EYE, Silian_hasEye[6]), 3, 3, 4, Silian_bounds);
            addBlock(Silian_level, Silian_eastFrame.setValue(EndPortalFrameBlock.HAS_EYE, Silian_hasEye[7]), 3, 3, 5, Silian_bounds);
            addBlock(Silian_level, Silian_eastFrame.setValue(EndPortalFrameBlock.HAS_EYE, Silian_hasEye[8]), 3, 3, 6, Silian_bounds);
            addBlock(Silian_level, Silian_westFrame.setValue(EndPortalFrameBlock.HAS_EYE, Silian_hasEye[9]), 7, 3, 4, Silian_bounds);
            addBlock(Silian_level, Silian_westFrame.setValue(EndPortalFrameBlock.HAS_EYE, Silian_hasEye[10]), 7, 3, 5, Silian_bounds);
            addBlock(Silian_level, Silian_westFrame.setValue(EndPortalFrameBlock.HAS_EYE, Silian_hasEye[11]), 7, 3, 6, Silian_bounds);

            if (Silian_complete) {
                fillBlocks(Silian_level, Blocks.END_PORTAL.defaultBlockState(), 4, 3, 4, 6, 3, 6, Silian_bounds);
            }
        }
    }

    public static class AncientCityPortalStructure extends SkyBlockStructure {
        public AncientCityPortalStructure(StructurePiece Silian_piece) {
            super(Silian_piece);
        }

        @Override
        public void generate(ServerLevelAccessor Silian_level, BoundingBox Silian_bounds, RandomSource Silian_random) {
            // Horizontal Sides
            fillBlocks(Silian_level, Blocks.REINFORCED_DEEPSLATE.defaultBlockState(), 13, 17, 10, 13, 17, 31, Silian_bounds);
            fillBlocks(Silian_level, Blocks.REINFORCED_DEEPSLATE.defaultBlockState(), 13, 24, 10, 13, 24, 31, Silian_bounds);

            // Vertical Sides
            fillBlocks(Silian_level, Blocks.REINFORCED_DEEPSLATE.defaultBlockState(), 13, 18, 10, 13, 23, 10, Silian_bounds);
            fillBlocks(Silian_level, Blocks.REINFORCED_DEEPSLATE.defaultBlockState(), 13, 18, 31, 13, 23, 31, Silian_bounds);

            // Sculk Shrieker
            addBlock(
                Silian_level,
                Blocks.SCULK_SHRIEKER.defaultBlockState().setValue(SculkShriekerBlock.CAN_SUMMON, true),
                9,
                8,
                20,
                Silian_bounds);
        }
    }

    public static class TrialChamberEntrance extends SkyBlockStructure {

        public TrialChamberEntrance(StructurePiece Silian_piece){
            super(Silian_piece);
        }

        @Override
        public void generate(ServerLevelAccessor Silian_level, BoundingBox Silian_bounds, RandomSource Silian_random) {
            // Place regular vault
            BlockPos.MutableBlockPos Silian_vaultPos = addBlock(
                Silian_level,
                Blocks.VAULT.defaultBlockState(),
                0, 0, 0,
                Silian_bounds);

            // Place ominous vault
            BlockPos.MutableBlockPos Silian_ominousVaultPos = addBlock(
                Silian_level,
                Blocks.VAULT.defaultBlockState().setValue(VaultBlock.OMINOUS, true),
                1, 0, 0,
                Silian_bounds);

            // Place trial spawner
            addBlock(
                Silian_level,
                Blocks.TRIAL_SPAWNER.defaultBlockState(),
                10, 0, 0,
                Silian_bounds);

            Silian_level.getServer().submit(() -> {


                BlockEntity Silian_vaultEntity = Silian_level.getBlockEntity(Silian_vaultPos);

                if (Silian_vaultEntity instanceof VaultBlockEntity Silian_vault) {
                    VaultConfig Silian_vaultConfig = Silian_vault.getConfig();
                    ItemStack Silian_trialKey = new ItemStack(Items.TRIAL_KEY);
                    VaultConfig Silian_config = new VaultConfig(
                        BuiltInLootTables.TRIAL_CHAMBERS_REWARD,
                        Silian_vaultConfig.activationRange(),
                        Silian_vaultConfig.deactivationRange(),
                        Silian_trialKey,
                        Optional.empty(),
                        PlayerDetector.INCLUDING_CREATIVE_PLAYERS,
                        PlayerDetector.EntitySelector.SELECT_FROM_LEVEL
                    );
                    Silian_vault.setConfig(Silian_config);
                    Silian_vault.setChanged();
                }

                BlockEntity Silian_ominousVaultEntity = Silian_level.getBlockEntity(Silian_ominousVaultPos);
                if (Silian_ominousVaultEntity instanceof VaultBlockEntity Silian_ominousVault) {
                    VaultConfig Silian_vaultConfig = Silian_ominousVault.getConfig();
                    ItemStack Silian_ominousTrialKey = new ItemStack(Items.OMINOUS_TRIAL_KEY);
                    VaultConfig Silian_ominousConfig = new VaultConfig(
                        BuiltInLootTables.TRIAL_CHAMBERS_REWARD_OMINOUS,
                        Silian_vaultConfig.activationRange(),
                        Silian_vaultConfig.deactivationRange(),
                        Silian_ominousTrialKey,
                        Optional.empty(),
                        PlayerDetector.INCLUDING_CREATIVE_PLAYERS,
                        PlayerDetector.EntitySelector.SELECT_FROM_LEVEL
                    );
                    Silian_ominousVault.setConfig(Silian_ominousConfig);
                    Silian_ominousVault.setChanged();
                }
            });
        }
    }

    public static class SpawnerStructure extends SkyBlockStructure {
        private final BlockPos spawnerPos;
        private final EntityType<?> spawnerType;

        public SpawnerStructure(StructurePiece Silian_piece, BlockPos spawnerPos, EntityType<?> spawnerType) {
            super(Silian_piece);
            this.spawnerPos = spawnerPos;
            this.spawnerType = spawnerType;
        }

        @Override
        public void generate(ServerLevelAccessor Silian_level, BoundingBox Silian_bounds, RandomSource Silian_random) {
            BlockPos.MutableBlockPos Silian_spawnerAbsolutePos =
                offsetPos(spawnerPos.getX(), spawnerPos.getY(), spawnerPos.getZ());
            if (Silian_bounds.isInside(Silian_spawnerAbsolutePos)) {
                Silian_level.getChunk(Silian_spawnerAbsolutePos).setInhabitedTime(6000);
                Silian_level.setBlock(Silian_spawnerAbsolutePos, Blocks.SPAWNER.defaultBlockState(), Block.UPDATE_CLIENTS);
                BlockEntity Silian_blockEntity = Silian_level.getBlockEntity(Silian_spawnerAbsolutePos);
                if (Silian_blockEntity instanceof SpawnerBlockEntity Silian_spawnerEntity) {
                    Silian_spawnerEntity.setEntityId(spawnerType, Silian_random);
                }
            }
        }
    }

    public static class SilverfishSpawnerStructure extends SpawnerStructure {
        public SilverfishSpawnerStructure(StructurePiece Silian_piece) {
            super(Silian_piece, new BlockPos(5, 3, 9), EntityType.SILVERFISH);
        }
    }

    public static class MagmaCubeSpawner extends SpawnerStructure {
        public MagmaCubeSpawner(StructurePiece Silian_piece) {
            super(Silian_piece, new BlockPos(11, 7, 19), EntityType.MAGMA_CUBE);
        }
    }
}
