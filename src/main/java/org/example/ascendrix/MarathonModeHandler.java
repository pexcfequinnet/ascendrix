package org.example.ascendrix;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

public class MarathonModeHandler implements GameModeHandler {
    private final MarathonRuleset ruleset;
    private final PieceSpinHandler spinHandler = new SRSSpinDetector();
    private final HUDHandler hud = new HUDHandler();
    private final int targetLines;
    private int linesCleared = 0;
    private long score = 0;  // was int — switch to long
    private int combo = 0;
    private boolean b2bActive = false;
    private int level = 1;
    private static final double LOG_COMBO_CAP = Math.log(1000);
    private static final double LOG_40        = Math.log(40);
    private static final int[]    B2B_THRESHOLDS = { 7, 14, 22, 36, 54 };
    private static final double[] B2B_TIERS = {1.2, 1.4, 1.6, 1.8, 2.0};
    private static final double[] MINI_B2B_TIERS = {1.15, 1.2, 1.4, 1.6, 1.8};
    private int b2bStreak = 0;  // counts consecutive B2B actions
    private boolean perfectClear = false;


    @Override
    public void setPerfectClearFlag(boolean flag) {
        this.perfectClear = flag;
    }
    public MarathonModeHandler(int targetLines) {
        this.ruleset = MarathonRuleset.create();
        this.targetLines = targetLines;
    }

    @Override
    public boolean supportsPerfectClear() { return true; }

    @Override
    public MarathonRuleset getRuleset() {
        return ruleset;
    }
    private int getTier(int streak) {
        int tier = 0;
        for (int threshold : B2B_THRESHOLDS) {
            if (streak >= threshold) tier++;
            else break;
        }
        return tier;
    }


    @Override
    // Scoring system
    public void onLinesCleared(int lines, SpinType spin, int dropRows, DropType dropType, GameEngine game) {
        // drop bonus always applies regardless of line clear
        int pc_bonus = 0;
        if(perfectClear)
            pc_bonus = 450;

        score += dropBonus(dropRows, dropType);

        if (lines == 0) {
            hud.showClear(spin, lines, System.nanoTime());
            combo = 0;
            return;
        }

        hud.showClear(spin, lines, System.nanoTime());
        linesCleared += lines;

        level = linesCleared / 10 + 1;

        ruleset.onLevelChanged(level);

        boolean isB2B = (lines == 4 || spin != SpinType.NONE);

        if (isB2B && b2bActive) {
            b2bStreak++;
        } else {
            b2bStreak = 0;
        }
        b2bActive = isB2B;

        // Spin bonus math
        int tier = getTier(b2bStreak);
        double b2bMultiplier = isB2B ? B2B_TIERS[tier] : 1.0;
        double spinMultiplier = switch (spin) {
            case T_SPIN -> B2B_TIERS[tier];
            case NONE -> 1.0;
            default -> MINI_B2B_TIERS[tier];
        };
        double lineMultiplier = Math.pow((lines + pc_bonus) / 2.0, 1.25);
        double comboMultiplier = Math.log(combo + 1) / Math.log(10000) + 1.0;
        double levelMultiplier = Math.log(level) / LOG_40 + 1.0;

        score += Math.round(500.0 + lineMultiplier * spinMultiplier * b2bMultiplier * comboMultiplier * levelMultiplier);
        combo = Math.min(combo + 1, 25);

        if (linesCleared >= targetLines) {
            game.end();
        }
    }

    private long dropBonus(int rows, DropType drop) {
        if (drop == DropType.NONE)
            return 0;
        return (10L * rows) / 2;
    }


    @Override
    public void renderHUD(GraphicsContext g, GameTimer timer, long now) {
        // Render spin
        if (hud.shouldDisplay(now)) {
            double alpha = hud.getAlpha(now);
            g.setGlobalAlpha(alpha);

            g.setFill(Color.color(0, 0, 0, 0.6));
            g.fillRect(160, 150, 70, 60);

            g.setFill(Color.ORANGE);
            g.fillText(hud.getClearText(), 160, 200);

            g.setGlobalAlpha(1.0);
        }

        Color b2bColor = switch (getTier(b2bStreak)) {
            case 0 -> Color.YELLOW;
            case 1 -> Color.ORANGE;
            case 2 -> Color.RED;
            case 3 -> Color.PURPLE;
            case 4 -> Color.CYAN;
            default -> Color.SILVER; // silver for max tier
        };

        if(b2bActive && b2bStreak > 0)
        {
            g.setFill(b2bColor);
            g.fillText("B2B x" + b2bStreak, 200, 240);
        }

        long time = timer.getElapsedMs();
        g.setTextAlign(TextAlignment.RIGHT);
        g.setFill(Color.WHITE);
        g.fillText("MARATHON (150 Lines)", 240, 400);
        g.fillText("Level: " + level, 240, 420);
        g.fillText("Lines: " + linesCleared + "/" + targetLines, 240, 440);
        g.fillText("Score: " + score, 240, 460);
        g.setTextAlign(TextAlignment.LEFT);
        g.fillText("Time: " + GameTimer.formatTime(time), 560, 460);
    }
}