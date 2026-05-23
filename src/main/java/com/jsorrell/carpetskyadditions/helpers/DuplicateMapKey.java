package com.jsorrell.carpetskyadditions.helpers;

import com.google.common.collect.ImmutableMap;
import java.util.Map;

public class DuplicateMapKey {
    public static <T, U> Map<T, U> duplicateMapKey(T Silian_originalKey, T Silian_copyKey, Map<T, U> Silian_originalMap) {
        return ImmutableMap.<T, U>builder()
                .putAll(Silian_originalMap)
                .put(Silian_copyKey, Silian_originalMap.get(Silian_originalKey))
                .build();
    }
}
