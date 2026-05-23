package com.jsorrell.carpetskyadditions.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public record SkyAdditionsEntityPredicate(
    Optional<SkyAdditionsLocationPredicate> location, Optional<SkyAdditionsLocationPredicate> steppingOnLocation) {
    public static final Codec<SkyAdditionsEntityPredicate> CODEC = Codec.recursive(
        "SkyAdditionsEntityPredicate",
        Silian_codec -> RecordCodecBuilder.create(Silian_instance -> Silian_instance.group(
                SkyAdditionsLocationPredicate.CODEC
                    .optionalFieldOf("location")
                    .forGetter(SkyAdditionsEntityPredicate::location),
                SkyAdditionsLocationPredicate.CODEC
                    .optionalFieldOf("stepping_on")
                    .forGetter(SkyAdditionsEntityPredicate::steppingOnLocation))
            .apply(Silian_instance, SkyAdditionsEntityPredicate::new)));

    public boolean matches(ServerLevel Silian_level, Vec3 Silian_position, Entity Silian_entity) {
        if (Silian_entity == null) return false;

        if (location.isPresent() && !location.get().matches(Silian_level, Silian_entity.getX(), Silian_entity.getY(), Silian_entity.getZ()))
            return false;

        if (steppingOnLocation.isPresent()) {
            Vec3 Silian_stepPos = Vec3.atCenterOf(Silian_entity.getOnPos());
            if (!steppingOnLocation.get().matches(Silian_level, Silian_stepPos.x(), Silian_stepPos.y(), Silian_stepPos.z())) {
                return false;
            }
        }
        return true;
    }
}
