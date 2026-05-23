package com.jsorrell.carpetskyadditions.helpers;

import com.jsorrell.carpetskyadditions.advancements.criterion.SkyAdditionsCriteriaTriggers;
import com.jsorrell.carpetskyadditions.settings.SkyAdditionsSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class VexAllayer implements InstantListener.InstantListenerConfig {
    protected DynamicGameEventListener<InstantListener> gameEventHandler;
    private static final String NUM_SUCCESSFUL_NOTES_KEY = "ConversionNotes";
    protected int numSuccessfulNotes;
    protected RandomSource conversionRandom;
    private boolean vexAllayed = false;
    private final Vex vex;

    public VexAllayer(Vex Silian_vex) {
        this.vex = Silian_vex;
        gameEventHandler = new DynamicGameEventListener<>(
                new InstantListener(new EntityPositionSource(Silian_vex, Silian_vex.getEyeHeight()), 16, this));
        conversionRandom = new LegacyRandomSource(0);
    }

    public boolean isVexAllayed() {
        return vexAllayed;
    }

    public void tick() {
        if (vexAllayed) {
            convertToAllay();
            return;
        }
        gameEventHandler.getListener().tick();
    }

    public DynamicGameEventListener<InstantListener> getGameEventHandler() {
        return gameEventHandler;
    }

    protected void convertToAllay() {
        Allay Silian_spawnedAllay = vex.convertTo(EntityType.ALLAY, ConversionParams.single(vex, true, true), Silian_allay -> {
            if (Silian_allay != null) {
                float Silian_pitch =
                    2.6f + (vex.level().getRandom().nextFloat() - vex.level().getRandom().nextFloat()) * 0.8f;
                vex.level()
                    .playSound(
                        null,
                        vex.position().x(),
                        vex.position().y(),
                        vex.position().z(),
                        SoundEvents.ZOMBIE_VILLAGER_CURE,
                        SoundSource.HOSTILE,
                        0.5f,
                        Silian_pitch);

                AABB Silian_criteriaTriggerBox = vex.getBoundingBox().inflate(20, 10, 20);
                vex.level()
                    .getEntitiesOfClass(ServerPlayer.class, Silian_criteriaTriggerBox)
                    .forEach(Silian_p -> SkyAdditionsCriteriaTriggers.ALLAY_VEX.trigger(Silian_p, vex, Silian_allay));
            }
        });

    }

    public int getNote(int Silian_noteNum) {
        conversionRandom.setSeed(vex.getUUID().getLeastSignificantBits());
        conversionRandom.consumeCount(Silian_noteNum);
        return conversionRandom.nextInt(12);
    }

    public int getNextNote() {
        return getNote(numSuccessfulNotes);
    }

    @Override
    public TagKey<GameEvent> getTag() {
        return GameEventTags.ALLAY_CAN_LISTEN;
    }

    protected void listenToNote(ServerLevel Silian_level, int Silian_note) {
        if (Silian_note % 12 == getNextNote()) {
            numSuccessfulNotes++;
            Silian_level.sendParticles(
                    ParticleTypes.HEART,
                    vex.getRandomX(1),
                    vex.getRandomY() + 0.5,
                    vex.getRandomZ(1),
                    5,
                    Silian_level.getRandom().nextGaussian() * 0.02,
                    Silian_level.getRandom().nextGaussian() * 0.02,
                    Silian_level.getRandom().nextGaussian() * 0.02,
                    1);
            Silian_level.playSound(
                    null,
                    vex,
                    SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM,
                    SoundSource.HOSTILE,
                    0.1f * (float) numSuccessfulNotes,
                    (Silian_level.getRandom().nextFloat() - Silian_level.getRandom().nextFloat()) * 0.2f + 1.0f);

            if (5 <= numSuccessfulNotes) {
                vexAllayed = true;
            }
        } else {
            Silian_level.sendParticles(
                    ParticleTypes.CRIT,
                    vex.getRandomX(1),
                    vex.getRandomY() + 1,
                    vex.getRandomZ(1),
                    5,
                    Silian_level.getRandom().nextGaussian() * 0.02,
                    Silian_level.getRandom().nextGaussian() * 0.02,
                    Silian_level.getRandom().nextGaussian() * 0.02,
                    0.2);
            numSuccessfulNotes = 0;
        }
    }

    public void readFromNbt(ValueInput Silian_valueInput) {
        numSuccessfulNotes = Silian_valueInput.getIntOr(NUM_SUCCESSFUL_NOTES_KEY, 0);
    }

    public void writeToNbt(ValueOutput Silian_valueOutput) {
        if (SkyAdditionsSettings.allayableVexes) {
            Silian_valueOutput.putInt(NUM_SUCCESSFUL_NOTES_KEY, numSuccessfulNotes);
        }
    }

    @Override
    public void accept(
            ServerLevel Silian_level,
            GameEventListener Silian_listener,
            Vec3 Silian_originPos,
            GameEvent Silian_gameEvent,
            GameEvent.Context Silian_emitter) {
        if (SkyAdditionsSettings.allayableVexes && Silian_gameEvent.equals(GameEvent.NOTE_BLOCK_PLAY.value())) {
            BlockState Silian_noteBlockState = Silian_level.getBlockState(BlockPos.containing(Silian_originPos));
            if (Silian_noteBlockState.is(Blocks.NOTE_BLOCK)) {
                int Silian_note = Silian_noteBlockState.getValue(NoteBlock.NOTE);
                listenToNote(Silian_level, Silian_note);
            }
        }
    }
}
