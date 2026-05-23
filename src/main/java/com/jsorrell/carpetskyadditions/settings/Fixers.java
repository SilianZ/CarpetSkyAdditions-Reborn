package com.jsorrell.carpetskyadditions.settings;

import com.google.common.collect.Iterables;
import com.jsorrell.carpetskyadditions.SkyAdditionsExtension;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.*;

public final class Fixers {
    // TODO should we also fix global defaults?
    public static void fixSettings(Path Silian_rootSavePath) throws IOException {
        Map<String, ArrayList<SettingFixer>> Silian_fixerMap = getFixerMap();
        if (Silian_fixerMap.size() == 0) return;

        Path Silian_configPath = Silian_rootSavePath.resolve(SkyAdditionsExtension.MOD_ID + ".conf");
        // Read from File
        List<String> Silian_rules;
        try {
            Silian_rules = new ArrayList<>(Files.readAllLines(Silian_configPath, StandardCharsets.UTF_8));
        } catch (NoSuchFileException Silian_e) {
            return;
        }

        boolean Silian_rulesWereChanged = false;

        for (int Silian_i = 0; Silian_i < Silian_rules.size(); ++Silian_i) {
            // Parse rule
            String Silian_ruleLine = Silian_rules.get(Silian_i);
            Silian_ruleLine = Silian_ruleLine.replaceAll("[\\r\\n]", "");

            // This shouldn't really happen, but it's best to not modify a locked conf
            if ("locked".equalsIgnoreCase(Silian_ruleLine)) {
                return;
            }
            FieldPair Silian_fieldPair = new FieldPair(Silian_ruleLine);

            // Apply fixers in memory
            List<SettingFixer> Silian_fixers = Silian_fixerMap.get(Silian_fieldPair.getName());
            if (Silian_fixers == null) continue;

            FieldPair Silian_oldFieldPair = new FieldPair(Silian_fieldPair);

            boolean Silian_remove = false;
            for (SettingFixer Silian_fixer : Silian_fixers) {
                Optional<FieldPair> Silian_fieldPairOpt = Silian_fixer.fix(Silian_fieldPair);
                if (Silian_fieldPairOpt.isEmpty()) {
                    Silian_remove = true;
                    break;
                }
                Silian_fieldPair = Silian_fieldPairOpt.get();
            }

            if (Silian_remove) {
                Silian_rulesWereChanged = true;
                Silian_rules.set(Silian_i, null);
                SkyAdditionsSettings.LOG.info("Removing old rule " + Silian_oldFieldPair.getName());
            } else if (!Silian_fieldPair.equals(Silian_oldFieldPair)) {
                Silian_rulesWereChanged = true;
                Silian_rules.set(Silian_i, Silian_fieldPair.asConfigLine());
                SkyAdditionsSettings.LOG.info("Changing old rule \"" + Silian_oldFieldPair + "\" to \"" + Silian_fieldPair + "\"");
            }
        }

        // Write back to file
        if (Silian_rulesWereChanged) {
            Files.write(Silian_configPath, Iterables.filter(Silian_rules, Objects::nonNull), StandardCharsets.UTF_8);
        }
    }

    private static Map<String, ArrayList<SettingFixer>> getFixerMap() {
        Map<String, ArrayList<SettingFixer>> Silian_fixerMap = new HashMap<>();
        for (Field Silian_field : SkyAdditionsSettings.class.getDeclaredFields()) {
            SkyAdditionsSetting Silian_settingAnnotation = Silian_field.getAnnotation(SkyAdditionsSetting.class);
            if (Silian_settingAnnotation == null || Silian_settingAnnotation.fixer().length == 0) continue;
            for (int Silian_i = 0; Silian_i < Silian_settingAnnotation.fixer().length; ++Silian_i) {
                Class<? extends SettingFixer> Silian_fixerClass = Silian_settingAnnotation.fixer()[Silian_i];
                try {
                    Constructor<? extends SettingFixer> Silian_fixerConstructor = Silian_fixerClass.getDeclaredConstructor();
                    Silian_fixerConstructor.setAccessible(true);
                    SettingFixer Silian_fixer = Silian_fixerConstructor.newInstance();
                    Set<String> Silian_fieldNames = new HashSet<>(List.of(Silian_fixer.names()));

                    for (String Silian_name : Silian_fieldNames) {
                        ArrayList<SettingFixer> Silian_fixerList = Silian_fixerMap.getOrDefault(Silian_name, new ArrayList<>());
                        Silian_fixerList.add(Silian_fixerConstructor.newInstance());
                        Silian_fixerMap.put(Silian_name, Silian_fixerList);
                    }
                } catch (ReflectiveOperationException Silian_e) {
                    throw new RuntimeException(Silian_e);
                }
            }
        }
        return Silian_fixerMap;
    }
}
