package bankingmanagementsystem;

import bankingmanagementsystem.exceptions.InsufficientFundsException;
import bankingmanagementsystem.exceptions.InvalidAmountException;
import java.io.Serializable;

public class Account implements IAccount, Serializable {
    private static final long serialVersionUID = 1L;
    private String accountId;
    private double balance;
    private String accountType;  // <-- Missing getter caused error
    private double minBalance;

    public Account(String accountId, String accountType, double minBalance) {
        this.accountId = accountId;
        this.accountType = accountType;
        this.minBalance = minBalance;
        this.balance = 0.0;
    }

    @Override
    public synchronized void deposit(double amount) throws InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException("Deposit amount must be positive.");
        }
        balance += amount;
    }

    @Override
    public synchronized void withdraw(double amount) throws InvalidAmountException, InsufficientFundsException {
        if (amount <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be positive.");
        }
        if ((balance - amount) < minBalance) {
            throw new InsufficientFundsException("Insufficient balance. Minimum balance of " + minBalance + " must be maintained.");
        }
        balance -= amount;
    }

    @Override
    public double getBalance() {
        return balance;
    }

    public String getAccountId() {
        return accountId;
    }

    // --- NEW Getter to fix AdminDashboard error ---
    public String getAccountType() {
        return accountType;
    }

    public double getMinBalance() {
        return minBalance;
    }

    @Override
    public String toString() {
        return String.format(
                "Account Details:\n" +
                "  Account Number: %s\n" +
                "  Account Type: %s\n" +
                "  Balance: %.2f\n" +
                "  Minimum Balance Required: %.2f",
                accountId, accountType, balance, minBalance
        );
    }
}
