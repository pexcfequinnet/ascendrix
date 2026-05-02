package org.example.ascendrix;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

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
    private int b2bStreak = 0;  // counts consecutive B2B actions


    public MarathonModeHandler(int targetLines) {
        this.ruleset = MarathonRuleset.create();
        this.targetLines = targetLines;
    }

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
    public PieceSpinHandler getSpinHandler() {
        return spinHandler;
    }


    @Override
    public SpinType filterSpin(SpinType spin) {
        return spin;
    }



    @Override
    // Scoring system
    public void onLinesCleared(int lines, SpinType spin, int dropRows, DropType dropType, GameEngine game) {
        // drop bonus always applies regardless of line clear
        score += dropBonus(dropRows, dropType);

        if (lines == 0) {
            combo = 0;
            return;
        }

        linesCleared += lines;
        System.out.println("onLinesCleared called, level: " + level);
        level = linesCleared / 10 + 1;

        ruleset.onLevelChanged(level);
        double spinMultiplier = switch (spin) {
            case T_SPIN -> 1.5;
            case NONE -> 1.0;
            default -> 1.25;
        };

        boolean isB2B = (lines == 4 || spin != SpinType.NONE);

        double b2bMultiplier;
        if (isB2B && b2bActive) {
            b2bStreak++;
            b2bMultiplier = B2B_TIERS[getTier(b2bStreak)];
        } else {
            b2bStreak = 0;
            b2bMultiplier = 1.0;
        }
        b2bActive = isB2B;

        double lineMultiplier = Math.pow(lines / 2.0, 1.25);
        double comboMultiplier = Math.log(combo + 1) / LOG_COMBO_CAP + 1.0;
        double levelMultiplier = 4.0 * (Math.log(level) / LOG_40) + 1.0;

        score += Math.round(500.0 * lineMultiplier * spinMultiplier * b2bMultiplier * comboMultiplier * levelMultiplier);
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
    public HUDHandler getHUD() {
        hud.updateStats(linesCleared, targetLines, "", 0);
        return hud;
    }

    @Override
    public boolean isFinished() {
        return linesCleared >= targetLines;
    }

    @Override
    public void renderHUD(GraphicsContext g, GameTimer timer, long now) {
        long time = timer.getElapsedMs();
        g.setFill(Color.WHITE);
        g.fillText("MARATHON (150 Lines)", 10, 380);
        g.fillText("Time: " + GameTimer.formatTime(time), 10, 400);
        g.fillText("Level: " + level, 10, 420);
        g.fillText("Lines: " + linesCleared + "/" + targetLines, 10, 440);
        g.fillText("Score: " + score, 10, 460);

        if (hud.shouldDisplay(now)) {
            double alpha = hud.getAlpha(now);
            g.setGlobalAlpha(alpha);

            g.setFill(Color.color(0, 0, 0, 0.6));
            g.fillRect(250, 150, 110, 60);

            g.setFill(Color.ORANGE);
            g.fillText(hud.getClearText(), 10, 200);

            g.setGlobalAlpha(1.0);
        }

        Color b2bColor = switch (getTier(b2bStreak)) {
            case 0 -> Color.YELLOW;
            case 1 -> Color.ORANGE;
            case 2 -> Color.RED;
            case 3 -> Color.PURPLE;
            case 4 -> Color.CYAN;
            default -> Color.SILVER; // purple/magenta for max tier
        };

        if(b2bActive && b2bStreak > 0)
        {
            g.setFill(b2bColor);
            g.fillText("B2B x" + b2bStreak, 20, 240);
        }
    }
}

