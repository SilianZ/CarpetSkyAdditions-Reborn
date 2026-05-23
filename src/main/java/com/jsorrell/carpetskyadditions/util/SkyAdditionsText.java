package com.jsorrell.carpetskyadditions.util;

import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public abstract class SkyAdditionsText {
    protected static Language language = Language.getInstance();

    public static MutableComponent translatable(String Silian_key) {
        return Component.translatableWithFallback(Silian_key, language.getOrDefault(Silian_key));
    }

    public static MutableComponent translatable(String Silian_key, Object... Silian_args) {
        return Component.translatableWithFallback(Silian_key, language.getOrDefault(Silian_key), Silian_args);
    }
}
