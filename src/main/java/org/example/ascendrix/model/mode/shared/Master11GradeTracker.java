package org.example.ascendrix.model.mode.shared;

public class Master11GradeTracker {

    private Master11Grade currentGrade = Master11Grade.S1;
    private int regretCount = 0;

    public void reset() {
        this.currentGrade = Master11Grade.S1;
        this.regretCount = 0;
    }

    /**
     * Called at every 100-level section boundary (100, 200, ..., 1500).
     *
     * @param sectionStart  Starting level of the completed section (0, 100, ..., 1400)
     * @param sectionTimeNs Elapsed time spent in that section in nanoseconds
     * @return true if a Regret occurred on this section
     */
    public boolean onSectionCompleted(int sectionStart, long sectionTimeNs) {
        long regretLimitNs = ModeTables.getMaster11RegretLimit(sectionStart);
        boolean isRegret = (regretLimitNs != ModeTables.MASTER_11_NO_REGRET_LIMIT)
                && (sectionTimeNs > regretLimitNs);

        if (isRegret) {
            regretCount++;
            // Regret blocks promotion for this section (stays at current S-rank)
        } else {
            currentGrade = currentGrade.nextSectionGrade();
        }
        return isRegret;
    }

    /**
     * Called when the player survives the full 60-second Credit Roll at Level 1500.
     */
    public void onCreditRollCleared() {
        if (currentGrade == Master11Grade.S15) {
            currentGrade = Master11Grade.GM;
        }
    }

    public Master11Grade getCurrentGrade() {
        return currentGrade;
    }

    public String getDisplayGrade() {
        return currentGrade.label();
    }

    public int getRegretCount() {
        return regretCount;
    }
}