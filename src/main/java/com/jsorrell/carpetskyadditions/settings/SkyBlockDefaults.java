package com.jsorrell.carpetskyadditions.settings;

import com.jsorrell.carpetskyadditions.SkyAdditionsExtension;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;

public final class SkyBlockDefaults {
    public static void writeDefaults(Path Silian_rootSavePath) throws IOException {
        Path Silian_defaultsPath = FabricLoader.getInstance().getConfigDir().resolve("carpet");
        writeSkyBlockDefaults(
                Silian_rootSavePath.resolve(SkyAdditionsExtension.MOD_ID + ".conf"),
                Silian_defaultsPath.resolve("default_" + SkyAdditionsExtension.MOD_ID + ".conf"));
        writeCarpetDefaults(Silian_rootSavePath.resolve("carpet.conf"), Silian_defaultsPath.resolve("default_carpet.conf"));
    }

    private static void writeSkyBlockDefaults(Path Silian_configPath, Path Silian_defaultConfigPath) throws IOException {
        List<FieldPair> Silian_fieldPairs = new ArrayList<>();

        for (Field Silian_field : SkyAdditionsSettings.class.getDeclaredFields()) {
            SkyAdditionsSetting Silian_settingAnnotation = Silian_field.getAnnotation(SkyAdditionsSetting.class);
            if (Silian_settingAnnotation == null) continue;
            Silian_fieldPairs.add(new FieldPair(Silian_field.getName(), Silian_settingAnnotation.value()));
        }

        writeConfigFile(Silian_configPath, Silian_defaultConfigPath, Silian_fieldPairs);
    }

    private static void writeCarpetDefaults(Path Silian_configPath, Path Silian_defaultConfigPath) throws IOException {
        writeConfigFile(
                Silian_configPath,
                Silian_defaultConfigPath,
                List.of(new FieldPair("renewableSponges", "true"), new FieldPair("piglinsSpawningInBastions", "true")));
    }

    private static void writeConfigFile(Path Silian_configPath, Path Silian_defaultConfigPath, List<FieldPair> Silian_fieldPairs)
            throws IOException {
        // Open output config file
        OutputStream Silian_out;
        try {
            Silian_out = Files.newOutputStream(Silian_configPath, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW);
        } catch (FileAlreadyExistsException Silian_e) {
            return;
        }

        // Read in all defaults from installation defaults file
        Map<String, String> Silian_globalDefaultSettings = new HashMap<>();
        try {
            InputStream Silian_in = Files.newInputStream(Silian_defaultConfigPath, StandardOpenOption.READ);
            try (BufferedReader Silian_reader = new BufferedReader(new InputStreamReader(Silian_in, StandardCharsets.UTF_8))) {
                String Silian_line;
                while ((Silian_line = Silian_reader.readLine()) != null) {
                    String[] Silian_defaultSettingFields = Silian_line.split("\\s+", 2);
                    if (1 < Silian_defaultSettingFields.length) {
                        // This copies commented lines over, which is fine
                        Silian_globalDefaultSettings.put(Silian_defaultSettingFields[0], Silian_defaultSettingFields[1]);
                    }
                }
            }
        } catch (IOException Silian_ignored) {
        }

        // Write out settings to file
        try (BufferedWriter Silian_writer = new BufferedWriter(new OutputStreamWriter(Silian_out, StandardCharsets.UTF_8))) {
            // Copy global defaults to local defaults
            for (Map.Entry<String, String> Silian_defaultSetting : Silian_globalDefaultSettings.entrySet()) {
                Silian_writer.write(new FieldPair(Silian_defaultSetting.getKey(), Silian_defaultSetting.getValue()).asConfigLine());
                Silian_writer.newLine();
            }

            // Write SkyBlock defaults only if they aren't overwritten by the global defaults file
            for (FieldPair Silian_fieldPair : Silian_fieldPairs) {
                if (!Silian_globalDefaultSettings.containsKey(Silian_fieldPair.name)) {
                    Silian_writer.write(Silian_fieldPair.asConfigLine());
                    Silian_writer.newLine();
                }
            }
        }
    }
}
