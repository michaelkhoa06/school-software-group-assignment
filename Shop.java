public class Shop {
    private PointsAccount points;
    private boolean bonusPointsBought = false;
    private boolean cheaperSkipsBought = false;
    private boolean moreBreaksBought = false;

    public Shop(PointsAccount points) {
        this.points = points;
    }

    // Upgrade 1 costs 50 points. Each upgrade can be bought once.
    public boolean buyBonusPoints() {
        if (bonusPointsBought) {
            return false;
        }
        if (points.spendPoints(50)) {
            bonusPointsBought = true;
            return true;
        }
        return false;
    }

    // Upgrade 2 costs 40 points.
    public boolean buyCheaperSkips() {
        if (cheaperSkipsBought) {
            return false;
        }
        if (points.spendPoints(40)) {
            cheaperSkipsBought = true;
            return true;
        }
        return false;
    }

    // Upgrade 3 costs 60 points.
    public boolean buyMoreBreaks() {
        if (moreBreaksBought) {
            return false;
        }
        if (points.spendPoints(60)) {
            moreBreaksBought = true;
            return true;
        }
        return false;
    }

    // Call after the task manager accepts a completed user task.
    public void completeTask() {
        if (bonusPointsBought) {
            points.addPoints(15);
        } else {
            points.addPoints(10);
        }
    }

    // Call after the task manager accepts a skipped user task.
    public void skipTask() {
        if (cheaperSkipsBought) {
            points.removePoints(3);
        } else {
            points.removePoints(5);
        }
    }

    // The roulette code uses this when calculating the break.
    public double getBreakWeightMultiplier() {
        if (moreBreaksBought) {
            return 1.5;
        }
        return 1.0;
    }

    public boolean isBonusPointsBought() {
        return bonusPointsBought;
    }

    public boolean isCheaperSkipsBought() {
        return cheaperSkipsBought;
    }

    public boolean isMoreBreaksBought() {
        return moreBreaksBought;
    }

    // Use only when restoring upgrades from the saved file.
    public void loadUpgrades(boolean bonusPoints, boolean cheaperSkips, boolean moreBreaks) {
        bonusPointsBought = bonusPoints;
        cheaperSkipsBought = cheaperSkips;
        moreBreaksBought = moreBreaks;
    }
}
