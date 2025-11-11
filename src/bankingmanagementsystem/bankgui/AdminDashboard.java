package bankingmanagementsystem.bankgui;

import bankingmanagementsystem.*;
import bankingmanagementsystem.exceptions.AccountNotFoundException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminDashboard extends JPanel {

    private BankingSystemGUI mainApp;
    private JTable customersTable;
    private DefaultTableModel customerModel;
    private JTable messagesTable;
    private DefaultTableModel messageModel;

    // --- New components for Specific Customer tab ---
    private JTextArea specificDetailArea;
    private JTextField searchAccField;
    // ----------------------------------------------

    public AdminDashboard(BankingSystemGUI mainApp) {
        this.mainApp = mainApp;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Admin Dashboard");
        welcomeLabel.setFont(new Font("Serif", Font.BOLD, 24));

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> mainApp.showLoginPanel());

        topPanel.add(welcomeLabel, BorderLayout.CENTER);
        topPanel.add(logoutButton, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();

        // --- UPDATED Tab order and added new tab ---
        tabbedPane.addTab("Manage Customers", createCustomerPanel());
        tabbedPane.addTab("Specific Customer", createSpecificCustomerPanel()); // <-- NEW TAB
        tabbedPane.addTab("Support Messages", createSupportPanel());
        // -------------------------------------------

        add(tabbedPane, BorderLayout.CENTER);
    }

    public void refreshData() {
        refreshCustomerTable();
        refreshSupportTable();
        // Clear specific customer search on refresh
        if (specificDetailArea != null) {
            specificDetailArea.setText("");
        }
        if (searchAccField != null) {
            searchAccField.setText("");
        }
    }

    private JPanel createCustomerPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // --- UPDATED: Simplified columns ---
        String[] customerColumns = {"Account Number", "Name"};
        // -----------------------------------
        customerModel = new DefaultTableModel(customerColumns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        customersTable = new JTable(customerModel);
        customersTable.setRowSorter(new TableRowSorter<>(customerModel));
        panel.add(new JScrollPane(customersTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        
        JButton deleteButton = new JButton("Delete Selected Customer");
        deleteButton.addActionListener(e -> deleteSelectedCustomer());
        bottomPanel.add(deleteButton);
        
        JButton batchDeleteButton = new JButton("Batch Delete by Account Number");
        batchDeleteButton.addActionListener(e -> batchDeleteCustomers());
        bottomPanel.add(batchDeleteButton);
        
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    // --- NEW: Panel for Specific Customer Search ---
    private JPanel createSpecificCustomerPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Account Number:"));
        searchAccField = new JTextField(15);
        topPanel.add(searchAccField);

        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchSpecificCustomer());
        topPanel.add(searchButton);

        panel.add(topPanel, BorderLayout.NORTH);

        specificDetailArea = new JTextArea();
        specificDetailArea.setEditable(false);
        specificDetailArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        panel.add(new JScrollPane(specificDetailArea), BorderLayout.CENTER);

        return panel;
    }

    // --- NEW: Logic for Specific Customer Search ---
    private void searchSpecificCustomer() {
        String accNum = searchAccField.getText().trim();
        if (accNum.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an account number.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Customer customer = findCustomerByAccountNumber(accNum);
            
            // Build the details string as requested
            StringBuilder sb = new StringBuilder();
            sb.append("--------------------------------------------\n");
            sb.append("Full Name: ").append(customer.getName()).append("\n");
            // Assuming Customer has getDob() and Account has getMinBalance() based on RegistrationPanel
            sb.append("Date of Birth: ").append(customer.getDob()).append("\n"); 
            sb.append("Address: ").append(customer.getAddress()).append("\n");
            sb.append("Salary: ").append(String.format("%.2f", customer.getSalary())).append("\n");
            sb.append("Account Type: ").append(customer.getAccount().getAccountType()).append("\n");
            sb.append("Min Balance: ").append(String.format("%.2f", customer.getAccount().getMinBalance())).append("\n"); 
            sb.append("Current Balance: ").append(String.format("%.2f", customer.getAccount().getBalance())).append("\n\n");

            // Investments
            sb.append("Investments:\n");
            if (customer.getInvestments() == null || customer.getInvestments().isEmpty()) {
                sb.append("  None\n");
            } else {
                for (Investment inv : customer.getInvestments()) {
                    // Based on CustomerDashboard fields
                    sb.append("  - ").append(inv.getInvestmentType()).append(" - ₹").append(String.format("%.2f", inv.getAmountInvested())).append("\n");
                }
            }
            sb.append("\n");

            // Loans
            sb.append("Loans:\n");
            if (customer.getLoans() == null || customer.getLoans().isEmpty()) {
                sb.append("  None\n");
            } else {
                for (Loan loan : customer.getLoans()) {
                    // Based on CustomerDashboard fields
                     sb.append("  - ").append(loan.getLoanType()).append(" - Due: ₹").append(String.format("%.2f", loan.getAmountDue())).append("\n");
                }
            }
            sb.append("--------------------------------------------\n");

            specificDetailArea.setText(sb.toString());
            specificDetailArea.setCaretPosition(0); // Scroll to top

        } catch (AccountNotFoundException e) {
            specificDetailArea.setText(""); // Clear the text area
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            // Catching potential NullPointers if getDob() or getMinBalance() don't exist
            specificDetailArea.setText("Could not retrieve full details for customer: " + accNum + "\nError: " + e.getMessage());
            e.printStackTrace(); // For debugging
        }
    }
    // -----------------------------------------------

    private void deleteSelectedCustomer() {
        int selectedRow = customersTable.convertRowIndexToModel(customersTable.getSelectedRow());
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a customer from the table to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String accNum = (String) customerModel.getValueAt(selectedRow, 0);
        String name = (String) customerModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete the account for " + name + " (Acc: " + accNum + ")?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Customer customer = findCustomerByAccountNumber(accNum);
                mainApp.getCustomers().remove(customer);
                DataManager.saveCustomers(mainApp.getCustomers());
                JOptionPane.showMessageDialog(this, "Customer account " + accNum + " deleted.", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshCustomerTable();
            } catch (AccountNotFoundException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void batchDeleteCustomers() {
        String input = JOptionPane.showInputDialog(this,
                "Enter account numbers to delete, separated by commas (e.g., 123,456,789):",
                "Batch Deletion",
                JOptionPane.PLAIN_MESSAGE);

        if (input == null || input.trim().isEmpty()) {
            return;
        }

        String[] accountNumbersToDelete = input.split(",");
        List<Customer> customers = mainApp.getCustomers();
        List<Customer> customersToDelete = new java.util.ArrayList<>();
        StringBuilder warnings = new StringBuilder();

        for (String accNum : accountNumbersToDelete) {
            try {
                Customer customer = findCustomerByAccountNumber(accNum.trim());
                customersToDelete.add(customer);
            } catch (AccountNotFoundException e) {
                warnings.append("Warning: Account ").append(accNum.trim()).append(" not found. Skipping.\n");
            }
        }

        if (customersToDelete.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No valid accounts found for deletion.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        String names = customersToDelete.stream().map(Customer::getName).collect(Collectors.joining(", "));
        int confirm = JOptionPane.showConfirmDialog(this,
                "The following " + customersToDelete.size() + " accounts will be deleted:\n" + names + "\n\nThis will run a multi-threaded deletion process. Continue?",
                "Confirm Batch Deletion",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            customers.removeAll(customersToDelete);
            DataManager.saveCustomers(customers);
            
            if (warnings.length() > 0) {
                JOptionPane.showMessageDialog(this, warnings.toString() + "\nBatch deletion complete.", "Batch Complete with Warnings", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Batch deletion complete. All selected accounts removed.", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            refreshCustomerTable();
        }
    }


    private JPanel createSupportPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        String[] messageColumns = {"ID", "Date", "From", "Account", "Status", "Issue"};
        messageModel = new DefaultTableModel(messageColumns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        messagesTable = new JTable(messageModel);
        messagesTable.setRowSorter(new TableRowSorter<>(messageModel));
        panel.add(new JScrollPane(messagesTable), BorderLayout.CENTER);

        JTextArea messageContentArea = new JTextArea(8, 0);
        messageContentArea.setEditable(false);
        messageContentArea.setLineWrap(true);
        messageContentArea.setWrapStyleWord(true);
        JScrollPane contentScrollPane = new JScrollPane(messageContentArea);
        contentScrollPane.setBorder(BorderFactory.createTitledBorder("Selected Message Details"));
        
        messagesTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    int viewRow = messagesTable.getSelectedRow();
                    if (viewRow == -1) return;
                    int modelRow = messagesTable.convertRowIndexToModel(viewRow);
                    
                    int msgId = (Integer) messageModel.getValueAt(modelRow, 0);
                    Message msg = mainApp.getSupportMessages().get(msgId);
                    if (msg != null) {
                        messageContentArea.setText(
                                "Issue:\n" + msg.getContent() +
                                "\n\nReply:\n" + msg.getReply()
                        );
                    }
                }
            }
        });

        JButton replyButton = new JButton("Reply/Resolve Selected Message");
        replyButton.addActionListener(e -> replyToMessage());

        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.add(contentScrollPane, BorderLayout.CENTER);
        bottomPanel.add(replyButton, BorderLayout.SOUTH);
        
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }
    
    private void replyToMessage() {
        int viewRow = messagesTable.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a message to reply to.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int modelRow = messagesTable.convertRowIndexToModel(viewRow);
        int msgId = (Integer) messageModel.getValueAt(modelRow, 0);
        Message message = mainApp.getSupportMessages().get(msgId);
        
        if (message == null) return;
        
        if (message.isResolved()) {
            JOptionPane.showMessageDialog(this, "This message is already resolved.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String reply = JOptionPane.showInputDialog(this,
                "Enter your reply for Message ID: " + msgId + "\nFrom: " + message.getCustomerName(),
                "Send Reply",
                JOptionPane.PLAIN_MESSAGE);

        if (reply != null && !reply.trim().isEmpty()) {
            message.setReply(reply);
            message.setResolved(true);
            DataManager.saveSupportMessages(mainApp.getSupportMessages());
            JOptionPane.showMessageDialog(this, "Reply sent and message marked as resolved.", "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshSupportTable();
        }
    }


    private void refreshCustomerTable() {
        customerModel.setRowCount(0);
        List<Customer> customers = mainApp.getCustomers();
        for (Customer c : customers) {
            // --- UPDATED: Simplified row data ---
            customerModel.addRow(new Object[]{
                    c.getAccount().getAccountId(),
                    c.getName()
            });
            // ------------------------------------
        }
    }

    private void refreshSupportTable() {
        messageModel.setRowCount(0);
        Map<Integer, Message> messages = mainApp.getSupportMessages();
        for (Message m : messages.values()) {
            messageModel.addRow(new Object[]{
                    m.getMessageId(),
                    m.getDateCreated().toLocalDate().toString(),
                    m.getCustomerName(),
                    m.getCustomerAccountNumber(),
                    m.isResolved() ? "Resolved" : "Pending",
                    m.getContent().substring(0, Math.min(m.getContent().length(), 50)) + "..."
            });
        }
    }

    private Customer findCustomerByAccountNumber(String accountNumber) throws AccountNotFoundException {
        return mainApp.getCustomers().stream()
                .filter(c -> c.getAccount().getAccountId().equals(accountNumber))
                .findFirst()
                .orElseThrow(() -> new AccountNotFoundException("No customer found with account number: " + accountNumber));
    }
}