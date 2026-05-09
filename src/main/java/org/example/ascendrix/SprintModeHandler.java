package org.example.ascendrix;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

public class SprintModeHandler implements GameModeHandler {
    private final SprintRuleset ruleset;
    private final int targetLines;
    private int linesCleared = 0;
    private boolean b2bActive = false;
    private int b2bStreak = 0;

    public SprintModeHandler(int targetLines) {
        this.ruleset = SprintRuleset.create();
        this.targetLines = targetLines;
    }
    private final HUDHelper hud = new HUDHelper();

    @Override
    public void setPerfectClearFlag(boolean flag){}

    @Override
    public boolean supportsPerfectClear() {return true;}

    @Override
    public RulesetHandler getRuleset() { return ruleset; }


    @Override
    public void onLinesCleared(int lines, SpinType spin, int pendingDropRows, DropType pendingDropType, GameEngine game){
        linesCleared += lines;
        boolean isB2B = lines == 4 || spin != SpinType.NONE;

        if (isB2B && b2bActive) {
            b2bStreak++;
        } else {
            b2bStreak = 0;
        }

        b2bActive = isB2B; // update after the check

        if (linesCleared >= targetLines) {
            game.end();
        }
        hud.showClear(spin, lines, System.nanoTime());
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

        if(b2bActive && b2bStreak > 0)
        {
            g.setFill(Color.YELLOW);
            g.fillText("B2B x" + b2bStreak, 200, 240);
        }

        g.setFill(Color.WHITE);
        g.setTextAlign(TextAlignment.RIGHT);
        g.fillText("SPRINT", 240, 400);

        long time = timer.getElapsedMs();
        g.fillText("Time: " + GameTimer.formatTime(time), 240, 420);
        g.fillText("Lines: " + linesCleared + "/" + targetLines, 240, 440);
        g.setTextAlign(TextAlignment.LEFT);





    }
}