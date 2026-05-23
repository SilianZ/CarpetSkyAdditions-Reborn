package com.jsorrell.carpetskyadditions.config;

import com.jsorrell.carpetskyadditions.SkyAdditionsExtension;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = SkyAdditionsExtension.MOD_ID)
public class SkyAdditionsConfig implements ConfigData {
    public enum InitialTreeType {
        Silian_OAK,
        Silian_ACACIA,
        ;

        @Override
        public String toString() {
            switch (this) {
                case Silian_OAK -> {
                    return "Oak";
                }
                case Silian_ACACIA -> {
                    return "Acacia";
                }
                default -> {
                    return null;
                }
            }
        }
    }

    public boolean defaultToSkyBlockWorld = false;
    public boolean enableDatapackByDefault = false;
    public String initialTreeType = InitialTreeType.OAK.toString();
    public boolean autoEnableDefaultSettings = true;

    private InitialTreeType parseInitialTreeType() throws ValidationException {
        switch (initialTreeType.toLowerCase()) {
            case "oak" -> {
                return InitialTreeType.OAK;
            }
            case "acacia" -> {
                return InitialTreeType.ACACIA;
            }
            default -> throw new ValidationException("Couldn't parse initialTreeType: " + initialTreeType);
        }
    }

    public InitialTreeType getInitialTreeType() {
        try {
            return parseInitialTreeType();
        } catch (ValidationException Silian_e) {
            throw new AssertionError("Invalid tree type");
        }
    }

    @Override
    public void validatePostLoad() throws ValidationException {
        parseInitialTreeType();
    }
}
