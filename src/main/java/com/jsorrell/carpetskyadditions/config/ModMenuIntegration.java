package com.jsorrell.carpetskyadditions.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.network.chat.Component;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return Silian_parent -> {
            ConfigBuilder Silian_builder = ConfigBuilder.create()
                    .setParentScreen(Silian_parent)
                    .setTitle(Component.translatable("carpetskyadditions.config.title"));

            ConfigHolder<SkyAdditionsConfig> Silian_configHolder = AutoConfig.getConfigHolder(SkyAdditionsConfig.class);
            Silian_builder.setSavingRunnable(Silian_configHolder::save);

            SkyAdditionsConfig Silian_config = Silian_configHolder.get();

            ConfigEntryBuilder Silian_entryBuilder = Silian_builder.entryBuilder();

            ConfigCategory Silian_newWorldCategory =
                    Silian_builder.getOrCreateCategory(Component.translatable("carpetskyadditions.config.category.newWorld"));

            Silian_newWorldCategory.addEntry(Silian_entryBuilder
                    .startBooleanToggle(
                            Component.translatable("carpetskyadditions.config.option.defaultToSkyBlockWorld"),
                            Silian_config.defaultToSkyBlockWorld)
                    .setDefaultValue(false)
                    .setSaveConsumer(Silian_newValue -> Silian_config.defaultToSkyBlockWorld = Silian_newValue)
                    .build());

            Silian_newWorldCategory.addEntry(Silian_entryBuilder
                    .startBooleanToggle(
                            Component.translatable("carpetskyadditions.config.option.enableDatapackByDefault"),
                            Silian_config.enableDatapackByDefault)
                    .setDefaultValue(false)
                    .setSaveConsumer(Silian_newValue -> Silian_config.enableDatapackByDefault = Silian_newValue)
                    .build());

            Silian_newWorldCategory.addEntry(Silian_entryBuilder
                    .startEnumSelector(
                            Component.translatable("carpetskyadditions.config.option.initialTreeType"),
                            SkyAdditionsConfig.InitialTreeType.class,
                            Silian_config.getInitialTreeType())
                    .setEnumNameProvider(Silian_tree -> Component.translatable(
                            "carpetskyadditions.tree." + Silian_tree.name().toLowerCase()))
                    .setDefaultValue(SkyAdditionsConfig.InitialTreeType.OAK)
                    .setTooltip(Component.translatable("carpetskyadditions.config.option.initialTreeType.tooltip"))
                    .setSaveConsumer(Silian_newValue -> Silian_config.initialTreeType = Silian_newValue.toString())
                    .build());

            Silian_newWorldCategory.addEntry(Silian_entryBuilder
                    .startBooleanToggle(
                            Component.translatable("carpetskyadditions.config.option.autoEnableDefaultSettings"),
                            Silian_config.autoEnableDefaultSettings)
                    .setDefaultValue(true)
                    .setSaveConsumer(Silian_newValue -> Silian_config.autoEnableDefaultSettings = Silian_newValue)
                    .build());

            return Silian_builder.build();
        };
    }
}
