package bankingmanagementsystem;

import bankingmanagementsystem.exceptions.InsufficientFundsException;
import bankingmanagementsystem.exceptions.InvalidAmountException;
import bankingmanagementsystem.exceptions.LoanNotFoundException;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Customer implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private LocalDate dob;  // <-- Missing getter caused error
    private String address;
    private double salary;
    private String password;
    private String securityQuestion;
    private String securityAnswer;
    private Account account;
    private List<Loan> loans;
    private List<Investment> investments;
    private List<Transaction> transactionHistory;

    public Customer(String name, LocalDate dob, String address, double salary, String accountType, double minBalance, String password) {
        this.name = name;
        this.dob = dob;
        this.address = address;
        this.salary = salary;
        this.password = password;
        this.account = new Account(AccountNumberGenerator.generateUniqueAccountNumber(), accountType, minBalance);
        if (minBalance > 0) {
            try {
                this.account.deposit(minBalance);
            } catch (InvalidAmountException e) {
                System.out.println("Critical Error: Initial minimum balance deposit failed: " + e.getMessage());
            }
        }
        this.loans = new ArrayList<>();
        this.investments = new ArrayList<>();
        this.transactionHistory = new ArrayList<>();
    }

    // --- Existing getters ---
    public String getName() { return name; }
    public Account getAccount() { return account; }
    public double getSalary() { return salary; }
    public List<Loan> getLoans() { return loans; }
    public List<Investment> getInvestments() { return investments; }
    public List<Transaction> getTransactionHistory() { return transactionHistory; }
    public String getAddress() { return address; }

    // --- NEW Getter to fix AdminDashboard error ---
    public LocalDate getDob() { 
        return dob; 
    }

    public String getSecurityQuestion() {
        return securityQuestion;
    }

    public void setSecurityQuestion(String securityQuestion) {
        this.securityQuestion = securityQuestion;
    }

    public String getSecurityAnswer() {
        return securityAnswer;
    }

    public void setSecurityAnswer(String securityAnswer) {
        this.securityAnswer = securityAnswer;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean checkPassword(String inputPassword) {
        return this.password.equals(inputPassword);
    }

    public void addTransaction(Transaction transaction) {
        this.transactionHistory.add(transaction);
    }

    public void printTransactionHistory() {
        System.out.println("\n--- Transaction History for " + name + " ---");
        if (transactionHistory.isEmpty()) {
            System.out.println("No transactions found.");
        } else {
            transactionHistory.forEach(System.out::println);
        }
    }

    public void applyLoan(Loan loan) throws InvalidAmountException {
        loans.add(loan);
        account.deposit(loan.getPrincipalAmount());
        addTransaction(new Transaction("Loan Disbursal", loan.getPrincipalAmount(), "Loan Disbursed: " + loan.getLoanType()));
    }

    public void repayLoan(String loanId, double amount) throws InsufficientFundsException, InvalidAmountException, LoanNotFoundException {
        Loan loanToRepay = loans.stream()
                .filter(l -> l.getLoanId().equals(loanId))
                .findFirst()
                .orElseThrow(() -> new LoanNotFoundException("Loan ID not found: " + loanId));

        account.withdraw(amount);
        loanToRepay.makePayment(amount);
        addTransaction(new Transaction("Loan Repayment", amount, "Paid installment for loan " + loanId));
    }

    public void addInvestment(Investment investment) throws InsufficientFundsException, InvalidAmountException {
        account.withdraw(investment.getAmountInvested());
        investments.add(investment);
        addTransaction(new Transaction("New Investment", investment.getAmountInvested(), "FD Created: " + investment.getInvestmentId()));
    }

    public void matureInvestments() {
        List<Investment> matured = investments.stream()
                .filter(inv -> !inv.getEndDate().isAfter(LocalDate.now()))
                .collect(Collectors.toList());

        if (matured.isEmpty()) {
            System.out.println("\nYou have no investments that are due for maturity.");
            return;
        }

        for (Investment inv : matured) {
            double maturityAmount = inv.getMaturityValue();
            try {
                account.deposit(maturityAmount);
                addTransaction(new Transaction("Investment Matured", maturityAmount, "Maturity of " + inv.getInvestmentId()));
                System.out.println("Investment " + inv.getInvestmentId() + " has matured. Amount " + String.format("%.2f", maturityAmount) + " has been credited.");
            } catch (InvalidAmountException e) {
                System.out.println("Error processing maturity for " + inv.getInvestmentId() + ": " + e.getMessage());
            }
        }

        investments.removeAll(matured);
        DataManager.saveCustomers(BankingSystem.getCustomers());
        System.out.println("New account balance: " + String.format("%.2f", account.getBalance()));
    }

    public void viewLoans() {
        System.out.println("\n--- Loans for " + name + " ---");
        if (loans.isEmpty()) {
            System.out.println("No active loans.");
        } else {
            loans.forEach(System.out::println);
        }
    }

    public void viewInvestments() {
        System.out.println("\n--- Investments for " + name + " ---");
        if (investments.isEmpty()) {
            System.out.println("No investments found.");
        } else {
            investments.forEach(System.out::println);
        }
    }

    @Override
    public String toString() {
        return String.format(
                "Customer Details:\n" +
                "  Name: %s\n" +
                "  Date of Birth: %s\n" +
                "  Address: %s\n" +
                "  Salary: %.2f\n" +
                "  %s",
                name, dob.toString(), address, salary, account.toString()
        );
    }
}
