package com.extranightvision;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Friends list. Memory only: it disappears when the game closes (never saved to disk). */
public final class Friends {
    private static final Map<String, String> NAMES = new ConcurrentHashMap<>();

    private Friends() {}

    public static boolean isFriend(String name) {
        return name != null && NAMES.containsKey(name.toLowerCase(Locale.ROOT));
    }

    public static void add(String name) {
        if (NAMES.put(name.toLowerCase(Locale.ROOT), name) == null) {
            Config.bump();
        }
    }

    public static void remove(String name) {
        if (NAMES.remove(name.toLowerCase(Locale.ROOT)) != null) {
            Config.bump();
        }
    }

    public static List<String> list() {
        List<String> out = new ArrayList<>(NAMES.values());
        Collections.sort(out, String.CASE_INSENSITIVE_ORDER);
        return out;
    }
}
