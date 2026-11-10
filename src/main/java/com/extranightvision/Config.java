package com.extranightvision;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Settings, saved to config/extranightvision.json */
public final class Config {
    public static final double MIN_SIZE = 0.1;
    public static final double MAX_SIZE = 10.0;

    public static volatile boolean enabled = false;
    public static volatile double width = 1.5;
    public static volatile double height = 1.5;

    /** If empty: every living entity is enlarged. If not empty: only these entity ids. */
    public static final Set<String> include = ConcurrentHashMap.newKeySet();
    /** Always normal size. */
    public static final Set<String> blacklist = ConcurrentHashMap.newKeySet();

    /** Bumped on every change so cached hitboxes get recalculated. */
    public static volatile int version = 0;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final class Data {
        boolean enabled = false;
        double width = 1.5;
        double height = 1.5;
        List<String> include = new ArrayList<>();
        List<String> blacklist = new ArrayList<>();
    }

    private Config() {}

    public static void bump() {
        version++;
    }

    public static double clamp(double v) {
        v = Math.round(v * 100.0) / 100.0;
        return Math.max(MIN_SIZE, Math.min(MAX_SIZE, v));
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("extranightvision.json");
    }

    public static void load() {
        Path p = file();
        if (!Files.exists(p)) {
            return;
        }
        try {
            String json = Files.readString(p);
            Data d = GSON.fromJson(json, Data.class);
            if (d == null) {
                return;
            }
            enabled = d.enabled;
            width = clamp(d.width);
            height = clamp(d.height);
            include.clear();
            blacklist.clear();
            if (d.include != null) {
                include.addAll(d.include);
            }
            if (d.blacklist != null) {
                blacklist.addAll(d.blacklist);
            }
            bump();
        } catch (Exception e) {
            System.err.println("[ExtraNightVision] Could not read config, using defaults: " + e);
        }
    }

    public static void save() {
        Data d = new Data();
        d.enabled = enabled;
        d.width = width;
        d.height = height;
        d.include = new ArrayList<>(include);
        d.blacklist = new ArrayList<>(blacklist);
        Collections.sort(d.include);
        Collections.sort(d.blacklist);
        try {
            Path p = file();
            Files.createDirectories(p.getParent());
            Files.writeString(p, GSON.toJson(d));
        } catch (IOException e) {
            System.err.println("[ExtraNightVision] Could not save config: " + e);
        }
    }
}
