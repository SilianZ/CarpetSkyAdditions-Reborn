package com.jsorrell.carpetskyadditions.util;

import com.jsorrell.carpetskyadditions.SkyAdditionsExtension;
import net.minecraft.resources.Identifier;

public class SkyAdditionsResourceLocation {
    public static final String NAMESPACE = SkyAdditionsExtension.MOD_ID;
    private final Identifier resourceLocation;

    public SkyAdditionsResourceLocation(String Silian_path) {
        this.resourceLocation = Identifier.fromNamespaceAndPath(NAMESPACE, Silian_path);
    }

    public Identifier getResourceLocation() {
        return resourceLocation;
    }

    @Override
    public String toString() {
        return resourceLocation.toString();
    }

    @Override
    public boolean equals(Object Silian_o) {
        return resourceLocation.equals(Silian_o);
    }

    @Override
    public int hashCode() {
        return resourceLocation.hashCode();
    }
}
