package com.joel4848.simplescoreboardtweaks.mixin;

import com.joel4848.simplescoreboardtweaks.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.number.NumberFormat;
import net.minecraft.scoreboard.number.StyledNumberFormat;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Shadow @Final private MinecraftClient client;

    // Cancel vanilla scoreboard rendering when player has made a change
    @Inject(
            method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onRenderScoreboardSidebar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        ModConfig cfg = ModConfig.get();

        // Just cancel vanilla rendering if scoreboard is disabled
        if (!cfg.showScoreboard) {
            ci.cancel();
            return;
        }

        // If all settings are default, let the vanilla scoreboard rendering do its work
        if (sst_isAllDefault(cfg)) {
            return;
        }

        ci.cancel();

        if (client.world == null) return;
        Scoreboard scoreboard = client.world.getScoreboard();
        ScoreboardObjective objective = scoreboard.getObjectiveForSlot(
                net.minecraft.scoreboard.ScoreboardDisplaySlot.SIDEBAR);
        if (objective == null) return;

        List<ScoreboardEntry> entries = new ArrayList<>(
                scoreboard.getScoreboardEntries(objective)
                        .stream()
                        .filter(e -> !e.hidden())
                        .toList());

        Comparator<ScoreboardEntry> comparator = switch (cfg.sortMode) {
            case SCORE_DESC -> Comparator.comparingInt(ScoreboardEntry::value).reversed();
            case SCORE_ASC  -> Comparator.comparingInt(ScoreboardEntry::value);

            case NAME_ASC   -> (e1, e2) -> ScoreboardUtils.compareNatural(e1.owner(), e2.owner());

            case NAME_DESC  -> (e1, e2) -> ScoreboardUtils.compareNatural(e2.owner(), e1.owner());
        };

        entries.sort(comparator);

        int maxEntries = cfg.maxEntries;
        if (maxEntries == 0) {
            entries = List.of();
        } else if (entries.size() > maxEntries) {
            entries = entries.subList(0, maxEntries);
        }

        TextRenderer textRenderer = ((InGameHud)(Object)this).getTextRenderer();
        Text titleText = objective.getDisplayName();
        int titleWidth = textRenderer.getWidth(titleText);

        NumberFormat fallbackFormat = objective.getNumberFormatOr(StyledNumberFormat.RED);

        int maxEntryWidth = 0;
        record RenderedEntry(Text nameText, Text scoreText) {}
        List<RenderedEntry> rendered = new ArrayList<>();

        for (ScoreboardEntry entry : entries) {
            Text nameText  = entry.name();
            Text scoreText = null;
            if (cfg.showScores) {
                NumberFormat fmt = entry.numberFormatOverride() != null
                        ? entry.numberFormatOverride()
                        : fallbackFormat;
                scoreText = fmt.format(entry.value());
            }
            int w = textRenderer.getWidth(nameText);
            if (cfg.showScores && scoreText != null) {
                w += 1 + textRenderer.getWidth(scoreText);
            }
            if (w > maxEntryWidth) maxEntryWidth = w;
            rendered.add(new RenderedEntry(nameText, scoreText));
        }

        int contentWidth = Math.max(titleWidth, maxEntryWidth);
        int boardWidth   = contentWidth + 8; // 4 px padding each side
        int lineHeight   = 10;
        int boardHeight  = (rendered.size() + 1) * lineHeight + 2;

        int scaledWidth  = context.getScaledWindowWidth();
        int scaledHeight = context.getScaledWindowHeight();

        float scaleFactor = cfg.scale / 100.0f;

        int baseX = (int) (scaledWidth - (boardWidth * scaleFactor) - 1 + cfg.offsetX);
        int baseY = (int) ((scaledHeight - (boardHeight * scaleFactor)) / 2 + cfg.offsetY);

        context.getMatrices().push();

        context.getMatrices().translate(baseX, baseY, 0);
        context.getMatrices().scale(scaleFactor, scaleFactor, 1.0f);

        int localX = 0;
        int localY = 0;

        int titleBgAlpha  = sst_alphaFromPercent(cfg.titleBackgroundOpacity);
        int entryBgAlpha  = sst_alphaFromPercent(cfg.backgroundOpacity);
        int textAlpha     = sst_alphaFromPercent(cfg.textOpacity);
        int titleTxtAlpha = sst_alphaFromPercent(cfg.titleTextOpacity);

        int titleBgColor = (titleBgAlpha << 24) | 0x212121;
        context.fill(localX, localY, localX + boardWidth, localY + lineHeight - 1, titleBgColor);

        int titleColor = sst_colorWithAlpha(0xFFFFFF, titleTxtAlpha);
        context.drawText(textRenderer, titleText,
                localX + (boardWidth - titleWidth) / 2,
                localY + 1,
                titleColor, false);

        int entryBgColor = (entryBgAlpha << 24) | 0x131313;
        for (int i = 0; i < rendered.size(); i++) {
            int rowY = localY + lineHeight - 1 + i * lineHeight;

            context.fill(localX, rowY, localX + boardWidth, rowY + lineHeight, entryBgColor);

            RenderedEntry re = rendered.get(i);
            context.drawText(textRenderer, re.nameText(), localX + 4, rowY + 1,
                    sst_colorWithAlpha(0xFFFFFF, textAlpha), false);

            if (cfg.showScores && re.scoreText() != null) {
                int scoreWidth = textRenderer.getWidth(re.scoreText());
                context.drawText(textRenderer, re.scoreText(),
                        localX + boardWidth - 4 - scoreWidth,
                        rowY + 1,
                        sst_colorWithAlpha(0xFF5555, textAlpha), false);
            }
        }

        context.getMatrices().pop();
    }

    public class ScoreboardUtils {
        private static final Pattern ALPHA_NUM_PATTERN = Pattern.compile("(\\d+)|(\\D+)");

        public static int compareNatural(String s1, String s2) {
            Matcher m1 = ALPHA_NUM_PATTERN.matcher(s1.toLowerCase());
            Matcher m2 = ALPHA_NUM_PATTERN.matcher(s2.toLowerCase());

            while (m1.find() && m2.find()) {
                String token1 = m1.group();
                String token2 = m2.group();

                if (Character.isDigit(token1.charAt(0)) && Character.isDigit(token2.charAt(0))) {
                    int num1 = Integer.parseInt(token1);
                    int num2 = Integer.parseInt(token2);
                    if (num1 != num2) {
                        return Integer.compare(num1, num2);
                    }
                } else {
                    int rel = token1.compareTo(token2);
                    if (rel != 0) {
                        return rel;
                    }
                }
            }
            return Integer.compare(s1.length(), s2.length());
        }
    }

    @Unique
    private static boolean sst_isAllDefault(ModConfig cfg) {
        return cfg.showScoreboard
                && cfg.showScores
                && cfg.sortMode == ModConfig.SortMode.SCORE_DESC
                && cfg.maxEntries == 15
                && cfg.offsetX == 0
                && cfg.offsetY == 0
                && cfg.backgroundOpacity == 50
                && cfg.titleBackgroundOpacity == 60
                && cfg.textOpacity == 100
                && cfg.titleTextOpacity == 100
                && cfg.scale == 100;
    }

    @Unique
    private static int sst_alphaFromPercent(int percent) {
        return Math.round(percent / 100.0f * 255);
    }

    @Unique
    private static int sst_colorWithAlpha(int rgb, int alpha) {
        return (alpha << 24) | (rgb & 0x00FFFFFF);
    }
}