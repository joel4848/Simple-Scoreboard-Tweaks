package com.joel4848.simplescoreboardtweaks;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ModConfig cfg = ModConfig.get();

            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Text.translatable("config.simple-scoreboard-tweaks.title"))
                    .setSavingRunnable(ModConfig::save)
                    .transparentBackground();

            ConfigEntryBuilder eb = builder.entryBuilder();

            ConfigCategory mainPage = builder.getOrCreateCategory(Text.of(""));

            var visibilitySub = eb.startSubCategory(Text.translatable("config.simple-scoreboard-tweaks.category.visibility"));
            visibilitySub.add(eb.startBooleanToggle(
                            Text.translatable("config.simple-scoreboard-tweaks.showScoreboard"),
                            cfg.showScoreboard)
                    .setDefaultValue(true)
                    .setTooltip(Text.translatable("config.simple-scoreboard-tweaks.showScoreboard.tooltip"))
                    .setSaveConsumer(v -> cfg.showScoreboard = v)
                    .build());

            visibilitySub.add(eb.startBooleanToggle(
                            Text.translatable("config.simple-scoreboard-tweaks.showScores"),
                            cfg.showScores)
                    .setDefaultValue(true)
                    .setTooltip(Text.translatable("config.simple-scoreboard-tweaks.showScores.tooltip"))
                    .setSaveConsumer(v -> cfg.showScores = v)
                    .build());
            mainPage.addEntry(visibilitySub.build());

            var sortingSub = eb.startSubCategory(Text.translatable("config.simple-scoreboard-tweaks.category.sorting"));
            sortingSub.add(eb.startEnumSelector(
                            Text.translatable("config.simple-scoreboard-tweaks.sortMode"),
                            ModConfig.SortMode.class,
                            cfg.sortMode)
                    .setDefaultValue(ModConfig.SortMode.SCORE_DESC)
                    .setTooltip(Text.translatable("config.simple-scoreboard-tweaks.sortMode.tooltip"))
                    .setEnumNameProvider(e -> Text.translatable(
                            "config.simple-scoreboard-tweaks.sortMode." + e.name().toLowerCase()))
                    .setSaveConsumer(v -> cfg.sortMode = v)
                    .build());

            sortingSub.add(eb.startIntSlider(
                            Text.translatable("config.simple-scoreboard-tweaks.maxEntries"),
                            cfg.maxEntries, 0, 100)
                    .setDefaultValue(15)
                    .setTooltip(Text.translatable("config.simple-scoreboard-tweaks.maxEntries.tooltip"))
                    .setSaveConsumer(v -> cfg.maxEntries = v)
                    .build());
            mainPage.addEntry(sortingSub.build());

            var positionSub = eb.startSubCategory(Text.translatable("config.simple-scoreboard-tweaks.category.position"));
            positionSub.add(eb.startIntField(
                            Text.translatable("config.simple-scoreboard-tweaks.offsetX"),
                            cfg.offsetX)
                    .setDefaultValue(0)
                    .setTooltip(Text.translatable("config.simple-scoreboard-tweaks.offsetX.tooltip"))
                    .setSaveConsumer(v -> cfg.offsetX = v)
                    .build());

            positionSub.add(eb.startIntField(
                            Text.translatable("config.simple-scoreboard-tweaks.offsetY"),
                            cfg.offsetY)
                    .setDefaultValue(0)
                    .setTooltip(Text.translatable("config.simple-scoreboard-tweaks.offsetY.tooltip"))
                    .setSaveConsumer(v -> cfg.offsetY = v)
                    .build());

            positionSub.add(eb.startIntSlider(
                            Text.translatable("config.simple-scoreboard-tweaks.scale"),
                            cfg.scale, 10, 300)
                    .setDefaultValue(100)
                    .setTooltip(Text.translatable("config.simple-scoreboard-tweaks.scale.tooltip"))
                    .setSaveConsumer(v -> cfg.scale = v)
                    .build());
            mainPage.addEntry(positionSub.build());

            var opacitySub = eb.startSubCategory(Text.translatable("config.simple-scoreboard-tweaks.category.opacity"));
            opacitySub.add(eb.startIntSlider(
                            Text.translatable("config.simple-scoreboard-tweaks.backgroundOpacity"),
                            cfg.backgroundOpacity, 0, 100)
                    .setDefaultValue(50)
                    .setTooltip(Text.translatable("config.simple-scoreboard-tweaks.backgroundOpacity.tooltip"))
                    .setSaveConsumer(v -> cfg.backgroundOpacity = v)
                    .build());

            opacitySub.add(eb.startIntSlider(
                            Text.translatable("config.simple-scoreboard-tweaks.titleBackgroundOpacity"),
                            cfg.titleBackgroundOpacity, 0, 100)
                    .setDefaultValue(60)
                    .setTooltip(Text.translatable("config.simple-scoreboard-tweaks.titleBackgroundOpacity.tooltip"))
                    .setSaveConsumer(v -> cfg.titleBackgroundOpacity = v)
                    .build());

            opacitySub.add(eb.startIntSlider(
                            Text.translatable("config.simple-scoreboard-tweaks.textOpacity"),
                            cfg.textOpacity, 0, 100)
                    .setDefaultValue(100)
                    .setTooltip(Text.translatable("config.simple-scoreboard-tweaks.textOpacity.tooltip"))
                    .setSaveConsumer(v -> cfg.textOpacity = v)
                    .build());

            opacitySub.add(eb.startIntSlider(
                            Text.translatable("config.simple-scoreboard-tweaks.titleTextOpacity"),
                            cfg.titleTextOpacity, 0, 100)
                    .setDefaultValue(100)
                    .setTooltip(Text.translatable("config.simple-scoreboard-tweaks.titleTextOpacity.tooltip"))
                    .setSaveConsumer(v -> cfg.titleTextOpacity = v)
                    .build());
            mainPage.addEntry(opacitySub.build());

            return builder.build();
        };
    }
}