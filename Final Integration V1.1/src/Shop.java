import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
-=[ About ]=-
Manages the user's point balance and purchased upgrades.

-=[ Usage ]=-
The Core tells the shop when to complete/skip/fail a task, and when to make a purchase.
(The UI initiates the request, then the Core tells the shop to do the above.)
*/

public class Shop {
    private int balance = 0;
    private List<Boolean> purchases;

    public Shop() {

    }

    public boolean buyUpgrade(ShopUpgradeIndices upgrade) {
        if (isUpgradeBought(upgrade)) {
            return false;
        }
        if (spendPoints(upgrade.cost)) {
            purchases.set(upgrade.ordinal(), true);
            return true;
        }
        return false;
    }

    // Call after the task manager accepts a completed user task.
    public int completeTask() {
        int before = getBalance();
        if (isUpgradeBought(ShopUpgradeIndices.BONUS_POINTS)) {
            addPoints(15);
        } else {
            addPoints(10);
        }
        return getBalance() - before;
    }

    // Call after the task manager accepts a skipped user task.
    public int skipTask() {
        int before = getBalance();
        if (isUpgradeBought(ShopUpgradeIndices.CHEAPER_SKIPS)) {
            removePoints(5);
        } else {
            removePoints(10);
        }
        return getBalance() - before;
    }

    public int failTask() {
        int before = getBalance();
        removePoints(15);
        return getBalance() - before;
    }


    // The roulette code uses this when calculating the break.
    public double getBreakWeightMultiplier() {
        if (isUpgradeBought(ShopUpgradeIndices.MORE_BREAKS)) {
            return 1.25;
        }
        return 1.0;
    }

    public boolean isUpgradeBought(ShopUpgradeIndices upgrade) {
        if (purchases.size() <= upgrade.ordinal()) {
            return false;
        }
        return purchases.get(upgrade.ordinal());
    }

    public List<Boolean> setPurchaseStatus(ShopUpgradeIndices upgrade, boolean status) {
        purchases.set(upgrade.ordinal(), status);
        return Collections.unmodifiableList(purchases);
    }

    // Use only when restoring upgrades from the saved file.
    public void loadData(int pointCount, List<Boolean> pruchaseList) {
        setBalance(pointCount);

        purchases = new ArrayList<>();

        for (int i = 0; i < ShopUpgradeIndices.values().length - 1; i++) {
            boolean hasPurchased = false;
            if (i < pruchaseList.size()) {
                hasPurchased = pruchaseList.get(i);
            }

            purchases.add(hasPurchased);
        }
    }


    public int getBalance() {
        return balance;
    }

    public List<Boolean> getPurchaseList() {
        return Collections.unmodifiableList(purchases);
    }

    private void setBalance(int savedBalance) {
        balance = Math.max(savedBalance, 0);
    }

    private void addPoints(int amount) {
        if (amount <= 0 || amount > Integer.MAX_VALUE - balance) {
            return;
        }
        balance = balance + amount;
    }

    private void removePoints(int amount) {
        if (amount <= 0) {
            return;
        }
        balance = balance - amount;
        if (balance < 0) {
            balance = 0;
        }
    }

    private boolean spendPoints(int cost) {
        if (cost < 0 || balance < cost) {
            return false;
        }
        balance = balance - cost;
        return true;
    }

    public static enum ShopUpgradeIndices {
        BONUS_POINTS(50),
        CHEAPER_SKIPS(40),
        MORE_BREAKS(60);

        public final int cost;

        ShopUpgradeIndices(int cost) {
            this.cost = cost;
        }
    }
}