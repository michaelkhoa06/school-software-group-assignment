public class PointsAccount {
    private int balance = 0;

    public int getBalance() {
        return balance;
    }

    // Use this when loading the saved points.
    public void setBalance(int savedBalance) {
        if (savedBalance < 0) {
            balance = 0;
        } else {
            balance = savedBalance;
        }
    }

    public void addPoints(int amount) {
        if (amount <= 0 || amount > Integer.MAX_VALUE - balance) {
            return;
        }
        balance = balance + amount;
    }

    public void removePoints(int amount) {
        if (amount <= 0) {
            return;
        }
        balance = balance - amount;
        if (balance < 0) {
            balance = 0;
        }
    }

    public boolean spendPoints(int cost) {
        if (cost < 0 || balance < cost) {
            return false;
        }
        balance = balance - cost;
        return true;
    }
}
