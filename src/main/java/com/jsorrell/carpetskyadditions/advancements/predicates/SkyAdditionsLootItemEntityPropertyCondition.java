package com.jsorrell.carpetskyadditions.advancements.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public record SkyAdditionsLootItemEntityPropertyCondition(Optional<EntityPredicate> predicate, LootContext.EntityTarget entityTarget) implements LootItemCondition {

   public static final MapCodec<SkyAdditionsLootItemEntityPropertyCondition> CODEC = RecordCodecBuilder.mapCodec(
		Silian_instance -> Silian_instance.group(
					Codec.optionalField("predicate",EntityPredicate.CODEC, false ).forGetter(SkyAdditionsLootItemEntityPropertyCondition::predicate),
					LootContext.EntityTarget.CODEC.fieldOf("entity").forGetter(SkyAdditionsLootItemEntityPropertyCondition::entityTarget)
				)
				.apply(Silian_instance, SkyAdditionsLootItemEntityPropertyCondition::new)
	);

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull MapCodec<? extends LootItemCondition> codec() {
        return (MapCodec<? extends LootItemCondition>) (MapCodec<?>) CODEC;
    }

    public boolean test(LootContext Silian_lootContext) {
        Entity Silian_entity = Silian_lootContext.getParameter(LootContextParams.THIS_ENTITY);
        Vec3 Silian_origin = Silian_lootContext.getParameter(LootContextParams.ORIGIN);
        return this.predicate.isPresent() && this.predicate.get().matches(Silian_lootContext.getLevel(), Silian_origin, Silian_entity);
    }

	public static LootItemCondition.Builder hasProperties(LootContext.EntityTarget Silian_target, EntityPredicate.Builder Silian_predicateBuilder) {
		return () -> new SkyAdditionsLootItemEntityPropertyCondition(Optional.of(Silian_predicateBuilder.build()), Silian_target);
	}


}
