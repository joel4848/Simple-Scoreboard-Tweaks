package com.joel4848.simplescoreboardtweaks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("simple-scoreboard-tweaks.json");

    private static ModConfig INSTANCE = new ModConfig();

    public boolean showScoreboard = true;

    public boolean showScores = true;

    // SCORE_DESC (default), SCORE_ASC, NAME_ASC, NAME_DESC
    public SortMode sortMode = SortMode.SCORE_DESC;

    public int maxEntries = 15;

    public int offsetX = 0;

    public int offsetY = 0;

    public int backgroundOpacity = 50;

    public int titleBackgroundOpacity = 60;

    public int textOpacity = 100;

    public int titleTextOpacity = 100;

    public int scale = 100;

    public enum SortMode {
        SCORE_DESC,
        SCORE_ASC,
        NAME_ASC,
        NAME_DESC
    }

    public static ModConfig get() {
        return INSTANCE;
    }

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                INSTANCE = GSON.fromJson(json, ModConfig.class);
                // Clamp values to safe ranges after loading
                INSTANCE.clamp();
            } catch (IOException e) {
                SimpleScoreboardTweaks.LOGGER.error("Failed to load config, using defaults.", e);
                INSTANCE = new ModConfig();
            }
        } else {
            INSTANCE = new ModConfig();
            save();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(INSTANCE));
        } catch (IOException e) {
            SimpleScoreboardTweaks.LOGGER.error("Failed to save config.", e);
        }
    }

    private void clamp() {
        maxEntries = Math.max(0, Math.min(100, maxEntries));
        backgroundOpacity = Math.max(0, Math.min(100, backgroundOpacity));
        titleBackgroundOpacity = Math.max(0, Math.min(100, titleBackgroundOpacity));
        textOpacity = Math.max(0, Math.min(100, textOpacity));
        titleTextOpacity = Math.max(0, Math.min(100, titleTextOpacity));
        scale = Math.max(10, Math.min(300, scale));
        if (sortMode == null) sortMode = SortMode.SCORE_DESC;
    }
}
