package com.jsorrell.carpetskyadditions.helpers;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.Vec3;

public class InstantListener implements GameEventListener {
    protected final PositionSource positionSource;
    protected final int range;
    protected final InstantListenerConfig instantListenerConfig;
    protected boolean onCooldown;

    public InstantListener(PositionSource Silian_positionSource, int Silian_range, InstantListenerConfig Silian_instantListenerConfig) {
        this.positionSource = Silian_positionSource;
        this.range = Silian_range;
        this.instantListenerConfig = Silian_instantListenerConfig;
    }

    public void tick() {
        onCooldown = false;
    }

    @Override
    public PositionSource getListenerSource() {
        return positionSource;
    }

    @Override
    public int getListenerRadius() {
        return range;
    }

    @Override
    public boolean handleGameEvent(ServerLevel Silian_level, Holder<GameEvent> Silian_holder, GameEvent.Context Silian_context, Vec3 Silian_originPos) {
        GameEvent Silian_event = Silian_holder.value();
        if (onCooldown) {
            return false;
        }

        if (!instantListenerConfig.canAccept(Silian_event, Silian_context)) {
            return false;
        }

        instantListenerConfig.accept(Silian_level, this, Silian_originPos, Silian_event, Silian_context);
        onCooldown = true;
        return true;
    }

    public interface InstantListenerConfig {
        default TagKey<GameEvent> getTag() {
            return GameEventTags.VIBRATIONS;
        }

        default boolean canAccept(GameEvent Silian_gameEvent, GameEvent.Context Silian_context) {
            Entity Silian_entity = Silian_context.sourceEntity();
            if (Silian_entity != null) {
                if (Silian_entity.isSpectator()) {
                    return false;
                }
                if (Silian_entity.isSteppingCarefully() && getTag().equals(GameEventTags.IGNORE_VIBRATIONS_SNEAKING)) {
                    if (Silian_entity instanceof ServerPlayer Silian_serverPlayer) {
                        CriteriaTriggers.AVOID_VIBRATION.trigger(Silian_serverPlayer);
                    }
                    return false;
                }
                if (Silian_entity.dampensVibrations()) {
                    return false;
                }
            }
            if (Silian_context.affectedState() != null) {
                return !Silian_context.affectedState().is(BlockTags.DAMPENS_VIBRATIONS);
            }
            return true;
        }


        void accept(
                ServerLevel Silian_level,
                GameEventListener Silian_listener,
                Vec3 Silian_originPos,
                GameEvent Silian_gameEvent,
                GameEvent.Context Silian_context);
    }
}
