package com.jsorrell.carpetskyadditions.advancements.criterion;

import com.jsorrell.carpetskyadditions.util.SkyAdditionsResourceLocation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.spider.CaveSpider;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.level.storage.loot.LootContext;

import java.util.Optional;

public class ConvertSpiderTrigger extends SimpleCriterionTrigger<ConvertSpiderTrigger.Conditions> {


    public void trigger(ServerPlayer Silian_player, Spider Silian_spider, CaveSpider Silian_caveSpider) {
        LootContext Silian_spiderLootContext = EntityPredicate.createContext(Silian_player, Silian_spider);
        LootContext Silian_caveSpiderLootContext = EntityPredicate.createContext(Silian_player, Silian_caveSpider);
        trigger(Silian_player, Silian_conditions -> Silian_conditions.matches(Silian_spiderLootContext, Silian_caveSpiderLootContext));
    }

    @Override
    public Codec<ConvertSpiderTrigger.Conditions> codec() {
        return ConvertSpiderTrigger.Conditions.CODEC;
    }
    public static record Conditions(Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> spider,
                                    Optional<ContextAwarePredicate> caveSpider) implements SimpleCriterionTrigger.SimpleInstance {

        public static final Codec<ConvertSpiderTrigger.Conditions> CODEC = RecordCodecBuilder.create(
                Silian_instance -> Silian_instance.group(
                        Codec.optionalField("player",EntityPredicate.ADVANCEMENT_CODEC, false)
                                .forGetter(ConvertSpiderTrigger.Conditions::player),
                        Codec.optionalField("spider",EntityPredicate.ADVANCEMENT_CODEC, false)
                                .forGetter(ConvertSpiderTrigger.Conditions::spider),
                        Codec.optionalField("caveSpider",EntityPredicate.ADVANCEMENT_CODEC, false)
                                .forGetter(ConvertSpiderTrigger.Conditions::caveSpider))
                        .apply(Silian_instance, ConvertSpiderTrigger.Conditions::new));

        public boolean matches(LootContext Silian_spiderContext, LootContext Silian_caveSpiderContext) {
                // Check if spider and caveSpider predicates are present and match their contexts
                boolean Silian_spiderMatches = spider.map(Silian_predicate -> Silian_predicate.matches(Silian_spiderContext)).orElse(true);
                boolean Silian_caveSpiderMatches = caveSpider.map(Silian_predicate -> Silian_predicate.matches(Silian_caveSpiderContext)).orElse(true);

                return Silian_spiderMatches && Silian_caveSpiderMatches;
        }
    }
}
