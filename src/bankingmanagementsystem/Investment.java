package bankingmanagementsystem;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

public class Investment implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final AtomicInteger idCounter = new AtomicInteger(500);
    private String investmentId;
    private String investmentType;
    private String description;
    private double amountInvested;
    private LocalDate startDate;
    private LocalDate endDate;
    private double interestRate;
    private double maturityValue;

    public Investment(String investmentType, String description, double amountInvested, LocalDate startDate, int tenureInYears, double interestRate) {
        this.investmentId = "INV" + idCounter.incrementAndGet();
        this.investmentType = investmentType;
        this.description = description;
        this.amountInvested = amountInvested;
        this.startDate = startDate;
        this.endDate = startDate.plusYears(tenureInYears);
        this.interestRate = interestRate;

        // --- Corrected: Compound Interest Calculation ---
        double rate = this.interestRate / 100.0;
        int n = 1; // Compounded annually
        this.maturityValue = this.amountInvested * Math.pow(1 + (rate / n), n * tenureInYears);
    }

    // --- Corrected: Added all necessary getter methods ---
    public String getInvestmentType() { return investmentType; }
    public double getAmountInvested() { return amountInvested; }
    public double getMaturityValue() { return maturityValue; }
    public String getInvestmentId() { return investmentId; }
    public LocalDate getEndDate() { return endDate; }

    @Override
    public String toString() {
        return String.format(
                "  - Investment [ID: %s, Type: %s, Amount: %.2f, Rate: %.1f%%, Maturity Value: %.2f, End: %s]",
                investmentId, investmentType, amountInvested, interestRate, maturityValue, endDate
        );
    }
}

