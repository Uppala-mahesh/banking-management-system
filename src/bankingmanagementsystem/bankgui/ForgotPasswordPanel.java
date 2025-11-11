package bankingmanagementsystem.bankgui;

import bankingmanagementsystem.*;
import bankingmanagementsystem.exceptions.AccountNotFoundException;

import javax.swing.*;
import java.awt.*;

public class ForgotPasswordPanel extends JPanel {

    private BankingSystemGUI mainApp;
    private CardLayout innerCardLayout;
    private JPanel centerPanel;

    private static final String CUSTOMER_FORGOT = "CustomerForgot";
    private static final String ADMIN_FORGOT = "AdminForgot";
    private static final String TYPE_SELECTION = "TypeSelection";

    public ForgotPasswordPanel(BankingSystemGUI mainApp) {
        this.mainApp = mainApp;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Reset Password", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 24));
        add(titleLabel, BorderLayout.NORTH);

        innerCardLayout = new CardLayout();
        centerPanel = new JPanel(innerCardLayout);

        centerPanel.add(createTypeSelectionPanel(), TYPE_SELECTION);
        centerPanel.add(createCustomerForgotPanel(), CUSTOMER_FORGOT);
        centerPanel.add(createAdminForgotPanel(), ADMIN_FORGOT);

        add(centerPanel, BorderLayout.CENTER);

        innerCardLayout.show(centerPanel, TYPE_SELECTION);
    }

    private JPanel createTypeSelectionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipadx = 50;
        gbc.ipady = 20;

        JButton customerButton = new JButton("Customer Password Reset");
        customerButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        customerButton.addActionListener(e -> innerCardLayout.show(centerPanel, CUSTOMER_FORGOT));
        gbc.gridy = 0;
        panel.add(customerButton, gbc);

        JButton adminButton = new JButton("Admin Password Reset");
        adminButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        adminButton.addActionListener(e -> innerCardLayout.show(centerPanel, ADMIN_FORGOT));
        gbc.gridy = 1;
        panel.add(adminButton, gbc);

        return panel;
    }

    private JPanel createCustomerForgotPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Account Number:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        JTextField accountField = new JTextField(15);
        panel.add(accountField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Security Question:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        JTextArea questionArea = new JTextArea(3, 15);
        questionArea.setEditable(false);
        questionArea.setLineWrap(true);
        questionArea.setWrapStyleWord(true);
        panel.add(new JScrollPane(questionArea), gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Your Answer:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        JTextField answerField = new JTextField(15);
        panel.add(answerField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("New Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        JPasswordField newPasswordField = new JPasswordField(15);
        panel.add(newPasswordField, gbc);

        JButton verifyButton = new JButton("Verify & Reset");
        verifyButton.addActionListener(e -> {
            String accountNum = accountField.getText().trim();
            if (accountNum.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter account number.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Customer customer = findCustomerByAccountNumber(accountNum);
                
                if (customer.getSecurityQuestion() == null || customer.getSecurityAnswer() == null) {
                    JOptionPane.showMessageDialog(this, "Security question not set. Please contact admin.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String providedAnswer = answerField.getText().trim();
                if (!PasswordResetManager.verifyCustomerSecurityAnswer(customer, providedAnswer)) {
                    JOptionPane.showMessageDialog(this, "Security answer is incorrect.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String newPassword = new String(newPasswordField.getPassword()).trim();
                if (newPassword.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "New password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                PasswordResetManager.resetCustomerPassword(customer, newPassword);
                DataManager.saveCustomers(mainApp.getCustomers());

                JOptionPane.showMessageDialog(this, "Password reset successful! Please log in with your new password.", "Success", JOptionPane.INFORMATION_MESSAGE);
                mainApp.showLoginPanel();

            } catch (AccountNotFoundException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        panel.add(verifyButton, gbc);

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> innerCardLayout.show(centerPanel, TYPE_SELECTION));
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        panel.add(backButton, gbc);

        // Update question when account is entered
        accountField.addActionListener(e -> {
            String accountNum = accountField.getText().trim();
            if (!accountNum.isEmpty()) {
                try {
                    Customer customer = findCustomerByAccountNumber(accountNum);
                    if (customer.getSecurityQuestion() != null) {
                        questionArea.setText(customer.getSecurityQuestion());
                    } else {
                        questionArea.setText("No security question set.");
                    }
                } catch (AccountNotFoundException ex) {
                    questionArea.setText("Account not found.");
                }
            }
        });

        return panel;
    }

    private JPanel createAdminForgotPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Admin ID:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        JTextField adminIdField = new JTextField(15);
        panel.add(adminIdField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Security Question:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        JTextArea questionArea = new JTextArea(3, 15);
        questionArea.setEditable(false);
        questionArea.setLineWrap(true);
        questionArea.setWrapStyleWord(true);
        panel.add(new JScrollPane(questionArea), gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Your Answer:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        JTextField answerField = new JTextField(15);
        panel.add(answerField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("New Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        JPasswordField newPasswordField = new JPasswordField(15);
        panel.add(newPasswordField, gbc);

        JButton verifyButton = new JButton("Verify & Reset");
        verifyButton.addActionListener(e -> {
            String adminId = adminIdField.getText().trim();
            if (adminId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter admin ID.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Admin admin = mainApp.getAdmin();
            if (!admin.getAdminId().equals(adminId)) {
                JOptionPane.showMessageDialog(this, "Admin ID not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (admin.getSecurityQuestion() == null || admin.getSecurityAnswer() == null) {
                JOptionPane.showMessageDialog(this, "Security question not set. Please contact system administrator.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String providedAnswer = answerField.getText().trim();
            if (!PasswordResetManager.verifyAdminSecurityAnswer(admin, providedAnswer)) {
                JOptionPane.showMessageDialog(this, "Security answer is incorrect.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String newPassword = new String(newPasswordField.getPassword()).trim();
            if (newPassword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "New password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            PasswordResetManager.resetAdminPassword(admin, newPassword);
            DataManager.saveAdmin(admin);

            JOptionPane.showMessageDialog(this, "Password reset successful! Please log in with your new password.", "Success", JOptionPane.INFORMATION_MESSAGE);
            mainApp.showLoginPanel();
        });

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        panel.add(verifyButton, gbc);

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> innerCardLayout.show(centerPanel, TYPE_SELECTION));
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        panel.add(backButton, gbc);

        // Update question when admin ID is entered
        adminIdField.addActionListener(e -> {
            Admin admin = mainApp.getAdmin();
            if (admin.getAdminId().equals(adminIdField.getText().trim())) {
                if (admin.getSecurityQuestion() != null) {
                    questionArea.setText(admin.getSecurityQuestion());
                } else {
                    questionArea.setText("No security question set.");
                }
            }
        });

        return panel;
    }

    private Customer findCustomerByAccountNumber(String accountNumber) throws AccountNotFoundException {
        return mainApp.getCustomers().stream()
                .filter(c -> c.getAccount().getAccountId().equals(accountNumber))
                .findFirst()
                .orElseThrow(() -> new AccountNotFoundException("No customer found with account number: " + accountNumber));
    }
}
