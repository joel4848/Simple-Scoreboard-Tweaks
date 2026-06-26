package com.joel4848.simplescoreboardtweaks;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleScoreboardTweaks implements ClientModInitializer {
    public static final String MOD_ID = "simple-scoreboard-tweaks";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        ModConfig.load();
        LOGGER.info("Simple Scoreboard Tweaks initialized.");
    }
}
