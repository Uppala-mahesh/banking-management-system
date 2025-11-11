package bankingmanagementsystem;

import bankingmanagementsystem.exceptions.AccountNotFoundException;
import bankingmanagementsystem.exceptions.InsufficientFundsException;
import bankingmanagementsystem.exceptions.InvalidAmountException;
import bankingmanagementsystem.exceptions.LoanNotFoundException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ActionHandler {

    // --- Private Nested Class for the Batch Deletion Logic ---
    private static class AccountDeletionTask implements Runnable {
        private final Customer customerToDelete;
        private final double bankCharges = 140.00;
        private final double governmentTax = 30.00;

        public AccountDeletionTask(Customer customer) {
            this.customerToDelete = customer;
        }

        @Override
        public void run() {
            String threadName = Thread.currentThread().getName();
            Account account = customerToDelete.getAccount();
            double cautionDeposit = account.getMinBalance();

            // Step 1: Refund caution deposit (if applicable)
            System.out.println("\n[" + threadName + "] Refunding caution deposit: " + String.format("%.2f", cautionDeposit) + " for " + customerToDelete.getName());
            if (cautionDeposit > 0) { // Only attempt deposit if there is something to refund.
                try {
                    account.deposit(cautionDeposit);
                } catch (InvalidAmountException e) {
                     System.out.println("[" + threadName + "] Error during refund for " + customerToDelete.getName() + ": " + e.getMessage());
                }
            }
            
            // Step 2: Attempt to deduct charges and tax
            System.out.println("[" + threadName + "] Deducting bank charges: " + String.format("%.2f", bankCharges) + " and government tax: " + String.format("%.2f", governmentTax));
            try {
                account.withdraw(bankCharges + governmentTax);
            } catch (InvalidAmountException | InsufficientFundsException e) {
                // Silently fail as requested. Do nothing.
            }
            
            // Step 3: Always confirm the deletion as requested
            System.out.println("[" + threadName + "] " + customerToDelete.getName() + " account deleted successfully.");
        }
    }

    private static Customer findCustomerByAccountNumber(String accountNumber, List<Customer> customers) throws AccountNotFoundException {
        return customers.stream()
                .filter(c -> c.getAccount().getAccountId().equals(accountNumber))
                .findFirst()
                .orElseThrow(() -> new AccountNotFoundException("No customer found with account number: " + accountNumber));
    }

    public static void depositMoney(Customer customer, Scanner sc, List<Customer> customers) {
        try {
            System.out.print("Enter amount to deposit: ");
            double amount = Double.parseDouble(sc.nextLine());
            if (amount <= 0) {
                System.out.println("Deposit amount must be a positive number.");
                return;
            }
            customer.getAccount().deposit(amount);
            customer.addTransaction(new Transaction("Deposit", amount, "Self-deposit into account"));
            DataManager.saveCustomers(customers);
            System.out.println("Successfully deposited " + String.format("%.2f", amount) + ". New balance: " + String.format("%.2f", customer.getAccount().getBalance()));
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount. Please enter a valid number.");
        } catch (InvalidAmountException e) {
            System.out.println("Deposit failed: " + e.getMessage());
        }
    }

    public static void withdrawMoney(Customer customer, Scanner sc, List<Customer> customers) {
        try {
            System.out.print("Enter amount to withdraw: ");
            double amount = Double.parseDouble(sc.nextLine());
            if (amount <= 0) {
                System.out.println("Withdrawal amount must be a positive number.");
                return;
            }
            customer.getAccount().withdraw(amount);
            customer.addTransaction(new Transaction("Withdrawal", amount, "Self-withdrawal from account"));
            DataManager.saveCustomers(customers);
            System.out.println("Successfully withdrew " + String.format("%.2f", amount) + ". New balance is " + String.format("%.2f", customer.getAccount().getBalance()));
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount. Please enter a valid number.");
        } catch (InvalidAmountException | InsufficientFundsException e) {
            System.out.println("Withdrawal failed: " + e.getMessage());
        }
    }

    public static void sendMoney(Customer sender, Scanner sc, List<Customer> customers) {
        try {
            System.out.print("Enter recipient's Account Number: ");
            String recipientAccNumber = sc.nextLine();
            if (sender.getAccount().getAccountId().equals(recipientAccNumber)) {
                System.out.println("You cannot send money to yourself.");
                return;
            }
            Customer recipient = findCustomerByAccountNumber(recipientAccNumber, customers);
            System.out.print("Enter amount to send: ");
            double amount = Double.parseDouble(sc.nextLine());
             if (amount <= 0) {
                System.out.println("Transfer amount must be a positive number.");
                return;
            }
            
            sender.getAccount().withdraw(amount);
            try {
                recipient.getAccount().deposit(amount);
            } catch (InvalidAmountException depositException) {
                sender.getAccount().deposit(amount); 
                System.out.println("Transaction failed: Could not deposit to recipient. Your account has been refunded.");
                return;
            }

            String senderDesc = "Transferred to " + recipient.getName() + " (Acc: " + recipientAccNumber + ")";
            sender.addTransaction(new Transaction("Transfer", amount, senderDesc));
            String recipientDesc = "Received from " + sender.getName() + " (Acc: " + sender.getAccount().getAccountId() + ")";
            recipient.addTransaction(new Transaction("Deposit", amount, recipientDesc));
            
            DataManager.saveCustomers(customers);
            System.out.println("Successfully sent " + String.format("%.2f", amount) + " to " + recipient.getName());
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount entered.");
        } catch (AccountNotFoundException | InvalidAmountException | InsufficientFundsException e) {
            System.out.println("Transaction failed: " + e.getMessage());
        }
    }
    
    public static void registerNewCustomer(Scanner sc, List<Customer> customers) {
        try {
            System.out.println("\n--- New Customer Registration ---");
            String name;
            while (true) {
                System.out.print("Enter Full Name: ");
                name = sc.nextLine();
                if (name != null && !name.trim().isEmpty()) break;
                System.out.println("Name cannot be blank. Please try again.");
            }

            LocalDate dob = null;
            while (true) {
                System.out.print("Enter Date of Birth (YYYY-MM-DD): ");
                String dobInput = sc.nextLine();
                 if (dobInput == null || dobInput.trim().isEmpty()) {
                    System.out.println("Date of Birth cannot be blank. Please try again.");
                    continue;
                }
                try {
                    dob = LocalDate.parse(dobInput);
                    if (dob.isAfter(LocalDate.now())) {
                        System.out.println("Invalid date! The date of birth cannot be in the future.");
                        continue;
                    }
                    break;
                } catch (DateTimeParseException e) {
                    System.out.println("Invalid date format! Please use YYYY-MM-DD.");
                }
            }

            String address;
            while (true) {
                System.out.print("Enter Address: ");
                address = sc.nextLine();
                if (address != null && !address.trim().isEmpty()) break;
                System.out.println("Address cannot be blank. Please try again.");
            }
            
            double salary;
            while (true) {
                System.out.print("Enter Monthly Salary: ");
                String salaryInput = sc.nextLine();
                try {
                    salary = Double.parseDouble(salaryInput);
                    if (salary < 0) {
                        System.out.println("Salary cannot be negative. Please enter a valid amount.");
                        continue;
                    }
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("Invalid number format for salary. Please try again.");
                }
            }

            String accountType;
            while (true) {
                System.out.print("Enter Account Type (Personal/Business): ");
                accountType = sc.nextLine();
                if (accountType != null && (accountType.equalsIgnoreCase("Personal") || accountType.equalsIgnoreCase("Business"))) {
                    break;
                }
                System.out.println("Invalid Account Type. Please enter 'Personal' or 'Business'.");
            }

            double minBalance;
            List<Double> allowedBalances = Arrays.asList(0.0, 5000.0, 10000.0);
            while (true) {
                System.out.print("Choose Minimum Balance (0, 5000, 10000): ");
                String minBalanceInput = sc.nextLine();
                try {
                    minBalance = Double.parseDouble(minBalanceInput);
                    if (allowedBalances.contains(minBalance)) {
                        break;
                    } else {
                        System.out.println("Invalid choice. Please select from 0, 5000, or 10000.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid number format. Please enter a valid number.");
                }
            }
            
            String password;
            while (true) {
                System.out.print("Create a Password: ");
                password = sc.nextLine();
                if (password != null && !password.trim().isEmpty()) break;
                System.out.println("Password cannot be blank. Please try again.");
            }

            Customer newCustomer = new Customer(name, dob, address, salary, accountType, minBalance, password);
            customers.add(newCustomer);
            DataManager.saveCustomers(customers);
            System.out.println("\nRegistration successful!");
            System.out.println("Your new Bank Account Number is: " + newCustomer.getAccount().getAccountId());
            System.out.println("Please use this to log in.");

        } catch (Exception e) {
             System.out.println("An unexpected error occurred during registration. Please try again. " + e.getMessage());
        }
    }
    
    public static void applyForLoan(Customer customer, Scanner sc, List<Customer> customers) {
        try {
            System.out.println("\n--- Loan Application ---");
            String loanType;
            while (true) {
                System.out.print("Select Loan Type (Personal, Student, Business): ");
                loanType = sc.nextLine();
                if (loanType != null && (loanType.equalsIgnoreCase("Personal") || loanType.equalsIgnoreCase("Student") || loanType.equalsIgnoreCase("Business"))) {
                    break;
                }
                System.out.println("Invalid Loan Type. Please enter 'Personal', 'Student', or 'Business'.");
            }
            
            System.out.print("Enter Loan Amount: ");
            double amount = Double.parseDouble(sc.nextLine());
            if (amount <= 0) {
                System.out.println("Loan amount must be a positive number. Please try again.");
                return;
            }

            System.out.print("Enter Loan Tenure (in years): ");
            int tenure = Integer.parseInt(sc.nextLine());
             if (tenure <= 0) {
                System.out.println("Loan tenure must be at least 1 year. Please try again.");
                return;
            }

            double interestRate;
            boolean approved;
            String rejectionReason = "Loan not approved.";

            switch (loanType.toLowerCase()) {
                case "student":
                    interestRate = 4.5;
                    approved = amount <= 500000;
                    if (!approved) rejectionReason = "Maximum Student Loan is 500,000.00.";
                    break;
                case "business":
                    interestRate = 10.5;
                    approved = customer.getSalary() > 20000 && amount < (customer.getSalary() * 100);
                    if (!approved) rejectionReason = "Loan not approved based on salary/amount.";
                    break;
                default: // Handles "personal"
                    interestRate = 8.5;
                    approved = customer.getSalary() > 20000 && amount < (customer.getSalary() * 100);
                    if (!approved) rejectionReason = "Loan not approved based on salary/amount.";
                    break;
            }

            if (approved) {
                LocalDate startDate = LocalDate.now();
                Loan loan = new Loan(loanType, amount, interestRate, startDate, tenure);
                customer.applyLoan(loan);
                DataManager.saveCustomers(customers);
                System.out.println("\nLoan approved for " + String.format("%.2f", amount) + "!");
                System.out.println("The amount has been credited to your account.");
                System.out.println("New balance: " + String.format("%.2f", customer.getAccount().getBalance()));
                System.out.println("Total repayable amount: " + String.format("%.2f", loan.getTotalRepayableAmount()));
            } else {
                System.out.println(rejectionReason);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount or tenure entered.");
        } catch (InvalidAmountException e) {
            System.out.println("Loan application failed: " + e.getMessage());
        }
    }

    public static void payLoanInstallment(Customer customer, Scanner sc, List<Customer> customers) {
        if (customer.getLoans().isEmpty() || customer.getLoans().stream().allMatch(l -> l.getAmountDue() <= 0)) {
            System.out.println("You have no active loans.");
            return;
        }
        customer.viewLoans();
        try {
            System.out.print("\nEnter the Loan ID you want to pay an installment for: ");
            String loanId = sc.nextLine();
            Loan loanToPay = customer.getLoans().stream()
                    .filter(loan -> loan.getLoanId().equals(loanId))
                    .findFirst()
                    .orElseThrow(() -> new LoanNotFoundException("No active loan found with ID: " + loanId));

            if (loanToPay.getAmountDue() <= 0) {
                System.out.println("This loan has already been fully paid.");
                return;
            }

            System.out.print("Enter amount to pay: ");
            double amountToPay = Double.parseDouble(sc.nextLine());
            if (amountToPay > customer.getAccount().getBalance()) {
                throw new InsufficientFundsException("Insufficient balance for this payment.");
            }

            double amountDue = loanToPay.getAmountDue();
            double actualPayment = Math.min(amountToPay, amountDue);

            if (amountToPay > amountDue) {
                System.out.println("Your payment of " + String.format("%.2f", amountToPay) + " is more than the amount due. Payment will be capped at " + String.format("%.2f", amountDue) + ".");
            }

            customer.repayLoan(loanId, actualPayment);

            if (loanToPay.getAmountDue() <= 0) {
                System.out.println("\nCongratulations! Loan " + loanId + " fully paid.");
            } else {
                System.out.println("\nPayment of " + String.format("%.2f", actualPayment) + " successful.");
            }

            DataManager.saveCustomers(customers);
            System.out.println("New account balance: " + String.format("%.2f", customer.getAccount().getBalance()));
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount entered.");
        } catch (LoanNotFoundException | InvalidAmountException | InsufficientFundsException e) {
            System.out.println("Payment failed: " + e.getMessage());
        }
    }

    public static void addInvestment(Customer customer, Scanner sc, List<Customer> customers) {
        try {
            System.out.println("\n--- New Fixed Deposit (FD) ---");
            System.out.print("Enter Amount to Invest: ");
            double amount = Double.parseDouble(sc.nextLine());
            if (amount <= 0) {
                System.out.println("Investment amount must be a positive number. Please try again.");
                return;
            }
            System.out.print("Enter Investment Tenure (in years): ");
            int tenure = Integer.parseInt(sc.nextLine());
            if (tenure <= 0) {
                System.out.println("Investment tenure must be at least 1 year.");
                return;
            }
            double interestRate = 6.5; 
            Investment investment = new Investment("Fixed Deposit", "FD Investment", amount, LocalDate.now(), tenure, interestRate);
            
            customer.addInvestment(investment);
            DataManager.saveCustomers(customers);
            System.out.println("\nInvestment of " + String.format("%.2f", amount) + " in a " + tenure + "-year FD was successful.");
            System.out.println("The final maturity value is estimated to be: " + String.format("%.2f", investment.getMaturityValue()));
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount or tenure entered.");
        } catch (InvalidAmountException | InsufficientFundsException e) {
            System.out.println("Investment failed: " + e.getMessage());
        }
    }

    public static void viewCustomerDetails(Scanner sc, List<Customer> customers) {
        System.out.print("Enter customer's Account Number to view details: ");
        String accNum = sc.nextLine();
        try {
            Customer customer = findCustomerByAccountNumber(accNum, customers);
            System.out.println("\n--- Details for " + customer.getName() + " ---");
            System.out.println(customer);
            customer.viewLoans();
            customer.viewInvestments();
            customer.printTransactionHistory();
        } catch (AccountNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void contactSupport(Customer customer, Scanner sc, Map<Integer, Message> supportMessages, AtomicInteger messageIdCounter) {
        System.out.println("\n--- Contact Support ---");
        System.out.println("Please describe your issue:");
        String content = sc.nextLine();
        if(content.isBlank()){
            System.out.println("Message cannot be empty.");
            return;
        }
        int messageId = messageIdCounter.incrementAndGet();
        Message newMessage = new Message(messageId, customer.getAccount().getAccountId(), customer.getName(), content);
        supportMessages.put(messageId, newMessage);
        DataManager.saveSupportMessages(supportMessages);
        System.out.println("Your message has been sent. Message ID: " + messageId);
    }

    public static void manageSupportMessages(Scanner sc, Map<Integer, Message> supportMessages) {
        System.out.println("\n--- Support Inbox ---");
        long unresolvedCount = supportMessages.values().stream().filter(m -> !m.isResolved()).count();
        if (unresolvedCount == 0) {
            System.out.println("No unresolved support messages.");
            return;
        }
        supportMessages.values().stream().filter(m -> !m.isResolved()).forEach(System.out::println);
        System.out.print("\nEnter Message ID to reply (or type 'back' to return): ");
        String input = sc.nextLine();
        if (input.equalsIgnoreCase("back")) return;

        try {
            int msgId = Integer.parseInt(input);
            Message message = supportMessages.get(msgId);
            if (message != null && !message.isResolved()) {
                System.out.print("Enter your reply: ");
                String reply = sc.nextLine();
                message.setReply(reply);
                message.setResolved(true);
                DataManager.saveSupportMessages(supportMessages);
                System.out.println("Reply sent and message marked as resolved.");
            } else {
                System.out.println("Invalid Message ID or message already resolved.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Enter a numeric Message ID.");
        }
    }

    public static void deleteCustomerAccount(Scanner sc, List<Customer> customers) {
        System.out.print("Enter the Account Number of the customer to delete: ");
        String accNum = sc.nextLine();
        try {
            Customer customer = findCustomerByAccountNumber(accNum, customers);
            System.out.println("Are you sure you want to delete the account for " + customer.getName() + "? (Type 'YES')");
            String confirmation = sc.nextLine();
            if (confirmation.equalsIgnoreCase("YES")) {
                customers.remove(customer);
                DataManager.saveCustomers(customers);
                System.out.println("Customer account " + accNum + " deleted.");
            } else {
                System.out.println("Account deletion cancelled.");
            }
        } catch (AccountNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * UPDATED: Multi-threaded batch account deletion process.
     */
    public static void batchDeleteAccounts(Scanner sc, List<Customer> customers) {
        System.out.println("Enter account numbers to delete, separated by commas (e.g., 123,456,789):");
        String input = sc.nextLine();
        String[] accountNumbersToDelete = input.split(",");
        
        List<Customer> customersToDelete = new ArrayList<>();
        for (String accNum : accountNumbersToDelete) {
            try {
                Customer customer = findCustomerByAccountNumber(accNum.trim(), customers);
                customersToDelete.add(customer);
            } catch (AccountNotFoundException e) {
                System.out.println("Warning: Account " + accNum.trim() + " not found. Skipping.");
            }
        }

        if (customersToDelete.isEmpty()) {
            System.out.println("No valid accounts selected for deletion.");
            return;
        }

        String names = customersToDelete.stream().map(Customer::getName).collect(Collectors.joining(", "));
        System.out.println("\nSelected " + customersToDelete.size() + " accounts for deletion: " + names);
        System.out.println("\n--- Starting batch account deletion ---");

        // --- UPDATED: Use a thread pool for concurrent processing and a custom ThreadFactory ---
        ThreadFactory namedThreadFactory = new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "Thread-" + threadNumber.getAndIncrement());
            }
        };
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(customersToDelete.size(), 10), namedThreadFactory);
        
        for (Customer customer : customersToDelete) {
            Runnable task = new AccountDeletionTask(customer);
            executor.submit(task);
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }

        // Remove the customers from the main list after processing is complete
        customers.removeAll(customersToDelete);
        System.out.println("\n--- Batch deletion complete ---");
        DataManager.saveCustomers(customers);
        System.out.println("Updated customer list saved successfully.");
    }
}

