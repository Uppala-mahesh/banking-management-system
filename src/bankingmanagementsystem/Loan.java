package bankingmanagementsystem;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

public class Loan implements Serializable {
    private static final long serialVersionUID = 1L;
    private String loanId;
    private String loanType;
    private double principalAmount;
    private double totalRepayableAmount;
    private double amountDue;
    private double interestRate;
    private LocalDate startDate;
    private LocalDate endDate;

    public Loan(String loanType, double principalAmount, double interestRate, LocalDate startDate, int tenureInYears) {
        this.loanId = "LN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.loanType = loanType;
        this.principalAmount = principalAmount;
        this.interestRate = interestRate;
        this.startDate = startDate;
        this.endDate = startDate.plusYears(tenureInYears);

        // --- Corrected: Compound Interest Calculation ---
        double rate = this.interestRate / 100.0;
        int n = 1; // Compounded annually
        this.totalRepayableAmount = this.principalAmount * Math.pow(1 + (rate / n), n * tenureInYears);
        this.amountDue = this.totalRepayableAmount;
    }

    // --- Corrected: Added all necessary getter methods ---
    public String getLoanId() { return loanId; }
    public String getLoanType() { return loanType; }
    public double getPrincipalAmount() { return principalAmount; }
    public double getTotalRepayableAmount() { return totalRepayableAmount; }
    public double getAmountDue() { return amountDue; }

    public void makePayment(double amount) {
        if (amount > 0) {
            this.amountDue -= amount;
            if (this.amountDue < 0) {
                this.amountDue = 0;
            }
        }
    }

    @Override
    public String toString() {
        return String.format(
                "  - Loan ID: %s | Type: %s | Principal: %.2f | Total Repayable: %.2f | Amount Due: %.2f | End Date: %s",
                loanId, loanType, principalAmount, totalRepayableAmount, amountDue, endDate
        );
    }
}

