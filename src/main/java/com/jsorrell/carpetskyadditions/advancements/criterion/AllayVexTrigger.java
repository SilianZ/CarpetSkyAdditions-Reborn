package com.jsorrell.carpetskyadditions.advancements.criterion;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.level.storage.loot.LootContext;

public class AllayVexTrigger extends SimpleCriterionTrigger<AllayVexTrigger.@org.jetbrains.annotations.NotNull Conditions> {

    public void trigger(ServerPlayer Silian_player, Vex Silian_vex, Allay Silian_allay) {

        LootContext Silian_vexLootContext = EntityPredicate.createContext(Silian_player, Silian_vex);
        LootContext Silian_allayLootContext = EntityPredicate.createContext(Silian_player, Silian_allay);

        trigger(Silian_player, Silian_conditions -> Silian_conditions.matches(Silian_vexLootContext, Silian_allayLootContext));
    }

    @Override
    public Codec<AllayVexTrigger.Conditions> codec() {
        return AllayVexTrigger.Conditions.CODEC;
    }

    public record Conditions(Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> vex,
                                    Optional<ContextAwarePredicate> allay) implements SimpleCriterionTrigger.SimpleInstance {

        public static final Codec<AllayVexTrigger.Conditions> CODEC = RecordCodecBuilder.create(
                Silian_instance -> Silian_instance.group(
                        Codec.optionalField("player", EntityPredicate.ADVANCEMENT_CODEC, false)
                                .forGetter(AllayVexTrigger.Conditions::player),
                        Codec.optionalField("vex", EntityPredicate.ADVANCEMENT_CODEC, false)
                                .forGetter(AllayVexTrigger.Conditions::vex),
                        Codec.optionalField("allay", EntityPredicate.ADVANCEMENT_CODEC, false)
                                .forGetter(AllayVexTrigger.Conditions::allay))
                        .apply(Silian_instance, AllayVexTrigger.Conditions::new));

        public boolean matches(LootContext Silian_vexContext, LootContext Silian_allayContext) {
            boolean Silian_vexMatches = vex.map(Silian_v -> Silian_v.matches(Silian_vexContext)).orElse(true); // Defaults to true if no predicate
            boolean Silian_allayMatches = allay.map(Silian_a -> Silian_a.matches(Silian_allayContext)).orElse(true); // Defaults to true if no predicate

            return Silian_vexMatches && Silian_allayMatches;
        }
    }
}
