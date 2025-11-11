package bankingmanagementsystem.bankgui;

import bankingmanagementsystem.*;
import bankingmanagementsystem.exceptions.AccountNotFoundException;
import bankingmanagementsystem.exceptions.InsufficientFundsException;
import bankingmanagementsystem.exceptions.InvalidAmountException;
import bankingmanagementsystem.exceptions.LoanNotFoundException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class CustomerDashboard extends JPanel {

    private BankingSystemGUI mainApp;
    private Customer currentCustomer;

    private JLabel welcomeLabel;
    private JLabel balanceLabel;
    private JTextArea accountDetailsArea;
    private JTextArea transactionArea; 
    private JTable loansTable;
    private JTable investmentsTable;

    public CustomerDashboard(BankingSystemGUI mainApp) {
        this.mainApp = mainApp;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new BorderLayout());
        welcomeLabel = new JLabel("Welcome, ");
        welcomeLabel.setFont(new Font("Serif", Font.BOLD, 24));
        
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> mainApp.showLoginPanel());

        topPanel.add(welcomeLabel, BorderLayout.CENTER);
        topPanel.add(logoutButton, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();

        // --- UPDATED: Renamed "History" tab ---
        tabbedPane.addTab("Account", createAccountPanel());
        tabbedPane.addTab("Transaction History", createTransactionHistoryPanel()); // <-- RENAMED
        tabbedPane.addTab("Deposit", createDepositPanel());
        tabbedPane.addTab("Withdraw", createWithdrawPanel());
        tabbedPane.addTab("Transfer", createTransferPanel());
        tabbedPane.addTab("Loans", createLoansPanel());
        tabbedPane.addTab("Investments", createInvestmentsPanel());
        tabbedPane.addTab("Support", createSupportPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    public void setCurrentCustomer(Customer customer) {
        this.currentCustomer = customer;
        refreshAllData();
    }

    private void refreshAllData() {
        if (currentCustomer == null) return;

        welcomeLabel.setText("Welcome, " + currentCustomer.getName());
        balanceLabel.setText(String.format("Current Balance: ₹%.2f", currentCustomer.getAccount().getBalance()));

        accountDetailsArea.setText(currentCustomer.toString());

        transactionArea.setText(""); 
        if (currentCustomer.getTransactionHistory().isEmpty()) {
            transactionArea.setText("No transactions found.");
        } else {
            for (Transaction tx : currentCustomer.getTransactionHistory()) {
                transactionArea.append(tx.toString() + "\n");
            }
        }

        DefaultTableModel loanModel = (DefaultTableModel) loansTable.getModel();
        loanModel.setRowCount(0); 
        for (Loan loan : currentCustomer.getLoans()) {
            loanModel.addRow(new Object[]{
                    loan.getLoanId(),
                    loan.getLoanType(),
                    String.format("%.2f", loan.getPrincipalAmount()),
                    String.format("%.2f", loan.getTotalRepayableAmount()),
                    String.format("%.2f", loan.getAmountDue())
            });
        }

        DefaultTableModel investmentModel = (DefaultTableModel) investmentsTable.getModel();
        investmentModel.setRowCount(0); 
        for (Investment inv : currentCustomer.getInvestments()) {
            investmentModel.addRow(new Object[]{
                    inv.getInvestmentId(),
                    inv.getInvestmentType(),
                    String.format("%.2f", inv.getAmountInvested()),
                    String.format("%.2f", inv.getMaturityValue()),
                    inv.getEndDate().toString()
            });
        }
    }

    private GridBagConstraints gbc(int x, int y) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    private JPanel createAccountPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        
        balanceLabel = new JLabel("Current Balance: ₹0.00");
        balanceLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        panel.add(balanceLabel, BorderLayout.NORTH);

        accountDetailsArea = new JTextArea(10, 40);
        accountDetailsArea.setEditable(false);
        accountDetailsArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        panel.add(new JScrollPane(accountDetailsArea), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createTransactionHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        transactionArea = new JTextArea(15, 40);
        transactionArea.setEditable(false);
        transactionArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane txScrollPane = new JScrollPane(transactionArea);
        txScrollPane.setBorder(BorderFactory.createTitledBorder("Transaction History"));
        panel.add(txScrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createDepositPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Amount to Deposit:"), gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        JTextField amountField = new JTextField(15);
        panel.add(amountField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        JButton depositButton = new JButton("Deposit");
        depositButton.addActionListener(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText());
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(this, "Deposit amount must be positive.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                currentCustomer.getAccount().deposit(amount);
                currentCustomer.addTransaction(new Transaction("Deposit", amount, "Self-deposit into account"));
                DataManager.saveCustomers(mainApp.getCustomers());

                JOptionPane.showMessageDialog(this, "Successfully deposited ₹" + String.format("%.2f", amount), "Success", JOptionPane.INFORMATION_MESSAGE);
                amountField.setText("");
                refreshAllData(); 
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid amount. Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (InvalidAmountException ex) {
                JOptionPane.showMessageDialog(this, "Deposit failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(depositButton, gbc);

        return panel;
    }

    private JPanel createWithdrawPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Amount to Withdraw:"), gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        JTextField amountField = new JTextField(15);
        panel.add(amountField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        JButton withdrawButton = new JButton("Withdraw");
        withdrawButton.addActionListener(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText());
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(this, "Withdrawal amount must be positive.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                currentCustomer.getAccount().withdraw(amount);
                currentCustomer.addTransaction(new Transaction("Withdrawal", amount, "Self-withdrawal from account"));
                DataManager.saveCustomers(mainApp.getCustomers());

                JOptionPane.showMessageDialog(this, "Successfully withdrew ₹" + String.format("%.2f", amount), "Success", JOptionPane.INFORMATION_MESSAGE);
                amountField.setText("");
                refreshAllData(); 
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid amount. Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (InvalidAmountException | InsufficientFundsException ex) {
                JOptionPane.showMessageDialog(this, "Withdrawal failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(withdrawButton, gbc);

        return panel;
    }

    private JPanel createTransferPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Recipient's Account Number:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        JTextField recipientField = new JTextField(15);
        panel.add(recipientField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Amount to Send:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        JTextField amountField = new JTextField(15);
        panel.add(amountField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        JButton transferButton = new JButton("Send Money");
        transferButton.addActionListener(e -> {
            try {
                String recipientAccNumber = recipientField.getText();
                double amount = Double.parseDouble(amountField.getText());

                if (currentCustomer.getAccount().getAccountId().equals(recipientAccNumber)) {
                    JOptionPane.showMessageDialog(this, "You cannot send money to yourself.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(this, "Transfer amount must be positive.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Customer recipient = mainApp.getCustomers().stream()
                        .filter(c -> c.getAccount().getAccountId().equals(recipientAccNumber))
                        .findFirst()
                        .orElseThrow(() -> new AccountNotFoundException("No customer found with account number: " + recipientAccNumber));

                currentCustomer.getAccount().withdraw(amount);
                try {
                    recipient.getAccount().deposit(amount);
                } catch (InvalidAmountException depositException) {
                    currentCustomer.getAccount().deposit(amount); // Refund
                    throw new Exception("Transaction failed: Could not deposit to recipient. Your account has been refunded.");
                }

                String senderDesc = "Transferred to " + recipient.getName() + " (Acc: " + recipientAccNumber + ")";
                currentCustomer.addTransaction(new Transaction("Transfer", amount, senderDesc));
                String recipientDesc = "Received from " + currentCustomer.getName() + " (Acc: " + currentCustomer.getAccount().getAccountId() + ")";
                recipient.addTransaction(new Transaction("Deposit", amount, recipientDesc));

                DataManager.saveCustomers(mainApp.getCustomers());
                
                JOptionPane.showMessageDialog(this, "Successfully sent ₹" + String.format("%.2f", amount) + " to " + recipient.getName(), "Success", JOptionPane.INFORMATION_MESSAGE);
                recipientField.setText("");
                amountField.setText("");
                refreshAllData();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid amount entered.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Transaction failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(transferButton, gbc);

        return panel;
    }

    private JPanel createLoansPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        String[] loanColumns = {"Loan ID", "Type", "Principal", "Total Repayable", "Amount Due"};
        DefaultTableModel loanModel = new DefaultTableModel(loanColumns, 0) {
            public boolean isCellEditable(int row, int column) { return false; } 
        };
        loansTable = new JTable(loanModel);
        panel.add(new JScrollPane(loansTable), BorderLayout.CENTER);

        JTabbedPane loanActionsPane = new JTabbedPane();
        loanActionsPane.addTab("Apply for New Loan", createApplyLoanPanel());
        loanActionsPane.addTab("Pay Loan Installment", createPayLoanPanel());

        panel.add(loanActionsPane, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createApplyLoanPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        
        JComboBox<String> loanTypeBox = new JComboBox<>(new String[]{"Personal", "Student", "Business"});
        JTextField loanAmountField = new JTextField(10);
        JTextField loanTenureField = new JTextField(10); 

        int gridy = 0;
        panel.add(new JLabel("Loan Type:"), gbc(0, gridy));
        panel.add(loanTypeBox, gbc(1, gridy++));
        
        panel.add(new JLabel("Loan Amount:"), gbc(0, gridy));
        panel.add(loanAmountField, gbc(1, gridy++));
        
        panel.add(new JLabel("Loan Tenure (Years):"), gbc(0, gridy));
        panel.add(loanTenureField, gbc(1, gridy++));

        JButton applyButton = new JButton("Apply for Loan");
        GridBagConstraints gbc = gbc(0, gridy);
        gbc.gridwidth = 2;
        panel.add(applyButton, gbc);

        applyButton.addActionListener(e -> {
            try {
                String loanType = (String) loanTypeBox.getSelectedItem();
                double amount = Double.parseDouble(loanAmountField.getText());
                int tenure = Integer.parseInt(loanTenureField.getText());
                
                if (amount <= 0 || tenure <= 0) {
                     JOptionPane.showMessageDialog(this, "Amount and tenure must be positive.", "Error", JOptionPane.ERROR_MESSAGE);
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
                        approved = currentCustomer.getSalary() > 20000 && amount < (currentCustomer.getSalary() * 100);
                        if (!approved) rejectionReason = "Loan not approved based on salary/amount.";
                        break;
                    default: 
                        interestRate = 8.5;
                        approved = currentCustomer.getSalary() > 20000 && amount < (currentCustomer.getSalary() * 100);
                        if (!approved) rejectionReason = "Loan not approved based on salary/amount.";
                        break;
                }

                if (approved) {
                    Loan loan = new Loan(loanType, amount, interestRate, LocalDate.now(), tenure);
                    currentCustomer.applyLoan(loan);
                    DataManager.saveCustomers(mainApp.getCustomers());
                    
                    String message = "Loan approved for ₹" + String.format("%.2f", amount) + "!\n" +
                                     "The amount has been credited to your account.\n" +
                                     "Total repayable: ₹" + String.format("%.2f", loan.getTotalRepayableAmount());
                    JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshAllData();
                } else {
                    JOptionPane.showMessageDialog(this, rejectionReason, "Loan Not Approved", JOptionPane.WARNING_MESSAGE);
                }
                
                loanAmountField.setText("");
                loanTenureField.setText("");

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid amount or tenure entered.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Loan application failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    private JPanel createPayLoanPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        
        JTextField loanIdField = new JTextField(10);
        JTextField payAmountField = new JTextField(10);

        int gridy = 0;
        panel.add(new JLabel("Loan ID to Pay:"), gbc(0, gridy));
        panel.add(loanIdField, gbc(1, gridy++));
        
        panel.add(new JLabel("Amount to Pay:"), gbc(0, gridy));
        panel.add(payAmountField, gbc(1, gridy++));

        JButton payButton = new JButton("Pay Installment");
        GridBagConstraints gbc = gbc(0, gridy);
        gbc.gridwidth = 2;
        panel.add(payButton, gbc);

        payButton.addActionListener(e -> {
            try {
                String loanId = loanIdField.getText();
                double amountToPay = Double.parseDouble(payAmountField.getText());

                Loan loanToPay = currentCustomer.getLoans().stream()
                    .filter(loan -> loan.getLoanId().equals(loanId))
                    .findFirst()
                    .orElseThrow(() -> new LoanNotFoundException("No active loan found with ID: " + loanId));
                
                if (loanToPay.getAmountDue() <= 0) {
                    JOptionPane.showMessageDialog(this, "This loan is already paid.", "Info", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                if (amountToPay > currentCustomer.getAccount().getBalance()) {
                    throw new InsufficientFundsException("Insufficient balance for this payment.");
                }

                double amountDue = loanToPay.getAmountDue();
                double actualPayment = Math.min(amountToPay, amountDue);

                if (amountToPay > amountDue) {
                    JOptionPane.showMessageDialog(this, "Payment is more than amount due. Capping at ₹" + String.format("%.2f", amountDue), "Info", JOptionPane.INFORMATION_MESSAGE);
                }

                currentCustomer.repayLoan(loanId, actualPayment);
                DataManager.saveCustomers(mainApp.getCustomers());

                String message = (loanToPay.getAmountDue() <= 0) ?
                    "Congratulations! Loan " + loanId + " fully paid." :
                    "Payment of ₹" + String.format("%.2f", actualPayment) + " successful.";
                
                JOptionPane.showMessageDialog(this, message, "Payment Successful", JOptionPane.INFORMATION_MESSAGE);
                loanIdField.setText("");
                payAmountField.setText("");
                refreshAllData();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid amount entered.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Payment failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        return panel;
    }

    private JPanel createInvestmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        String[] invColumns = {"Inv. ID", "Type", "Amount", "Maturity Value", "End Date"};
        DefaultTableModel invModel = new DefaultTableModel(invColumns, 0) {
            public boolean isCellEditable(int row, int column) { return false; } 
        };
        investmentsTable = new JTable(invModel);
        panel.add(new JScrollPane(investmentsTable), BorderLayout.CENTER);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("New Fixed Deposit (FD)"));
        
        JTextField investAmountField = new JTextField(10);
        JTextField investTenureField = new JTextField(10);

        int gridy = 0;
        formPanel.add(new JLabel("Amount to Invest:"), gbc(0, gridy));
        formPanel.add(investAmountField, gbc(1, gridy++));
        
        formPanel.add(new JLabel("Tenure (Years):"), gbc(0, gridy));
        formPanel.add(investTenureField, gbc(1, gridy++));

        JButton investButton = new JButton("Create FD");
        GridBagConstraints gbc = gbc(0, gridy);
        gbc.gridwidth = 2;
        formPanel.add(investButton, gbc);

        investButton.addActionListener(e -> {
            try {
                double amount = Double.parseDouble(investAmountField.getText());
                int tenure = Integer.parseInt(investTenureField.getText());

                if (amount <= 0 || tenure <= 0) {
                    JOptionPane.showMessageDialog(this, "Amount and tenure must be positive.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                double interestRate = 6.5; 
                Investment investment = new Investment("Fixed Deposit", "FD Investment", amount, LocalDate.now(), tenure, interestRate);
                
                currentCustomer.addInvestment(investment);
                DataManager.saveCustomers(mainApp.getCustomers());
                
                String message = "Investment of ₹" + String.format("%.2f", amount) + " in a " + tenure + "-year FD was successful.\n" +
                                 "Maturity value: ₹" + String.format("%.2f", investment.getMaturityValue());
                JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
                
                investAmountField.setText("");
                investTenureField.setText("");
                refreshAllData();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid amount or tenure entered.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Investment failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(formPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createSupportPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Contact Support"));

        panel.add(new JLabel("Please describe your issue:"), BorderLayout.NORTH);
        
        JTextArea issueArea = new JTextArea(10, 30);
        issueArea.setLineWrap(true);
        issueArea.setWrapStyleWord(true);
        panel.add(new JScrollPane(issueArea), BorderLayout.CENTER);
        
        JButton submitButton = new JButton("Submit Ticket");
        panel.add(submitButton, BorderLayout.SOUTH);

        submitButton.addActionListener(e -> {
            String content = issueArea.getText();
            if (content.isBlank()) {
                JOptionPane.showMessageDialog(this, "Message cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int messageId = mainApp.getMessageIdCounter().incrementAndGet();
            Message newMessage = new Message(messageId, currentCustomer.getAccount().getAccountId(), currentCustomer.getName(), content);
            mainApp.getSupportMessages().put(messageId, newMessage);
            DataManager.saveSupportMessages(mainApp.getSupportMessages());

            JOptionPane.showMessageDialog(this, "Your message has been sent. Message ID: " + messageId, "Success", JOptionPane.INFORMATION_MESSAGE);
            issueArea.setText("");
        });

        return panel;
    }
}