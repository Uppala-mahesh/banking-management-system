package bankingmanagementsystem.bankgui;

import bankingmanagementsystem.Customer;
import bankingmanagementsystem.Admin;
import bankingmanagementsystem.DataManager;
import bankingmanagementsystem.PasswordResetManager;
import bankingmanagementsystem.exceptions.AccountNotFoundException;

import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {

    private BankingSystemGUI mainApp;

    private CardLayout innerCardLayout;
    private JPanel centerPanel;
    private static final String BUTTON_PANEL = "ButtonPanel";
    private static final String CUSTOMER_LOGIN_PANEL = "CustomerLoginPanel";
    private static final String ADMIN_LOGIN_PANEL = "AdminLoginPanel";

    private JTextField customerAccountField;
    private JPasswordField customerPasswordField;
    private JTextField adminIdField;
    private JPasswordField adminPasswordField;

    public LoginPanel(BankingSystemGUI mainApp) {
        this.mainApp = mainApp;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Welcome to Prime Bank", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 28));
        add(titleLabel, BorderLayout.NORTH);

        innerCardLayout = new CardLayout();
        centerPanel = new JPanel(innerCardLayout);

        centerPanel.add(createButtonPanel(), BUTTON_PANEL);
        centerPanel.add(createCustomerLoginPanel(), CUSTOMER_LOGIN_PANEL);
        centerPanel.add(createAdminLoginPanel(), ADMIN_LOGIN_PANEL);

        add(centerPanel, BorderLayout.CENTER);

        JButton registerButton = new JButton("New Customer? Register Here");
        registerButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        registerButton.addActionListener(e -> mainApp.showRegistrationPanel());
        add(registerButton, BorderLayout.SOUTH);

        innerCardLayout.show(centerPanel, BUTTON_PANEL);
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipadx = 50;
        gbc.ipady = 20;

        JButton customerLoginButton = new JButton("Customer Login");
        customerLoginButton.setFont(new Font("SansSerif", Font.BOLD, 18));
        customerLoginButton.addActionListener(e -> innerCardLayout.show(centerPanel, CUSTOMER_LOGIN_PANEL));
        gbc.gridy = 0;
        buttonPanel.add(customerLoginButton, gbc);

        JButton adminLoginButton = new JButton("Admin Login");
        adminLoginButton.setFont(new Font("SansSerif", Font.BOLD, 18));
        adminLoginButton.addActionListener(e -> innerCardLayout.show(centerPanel, ADMIN_LOGIN_PANEL));
        gbc.gridy = 1;
        buttonPanel.add(adminLoginButton, gbc);
        

        return buttonPanel;
    }

    private JPanel createCustomerLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Customer Login"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Account Number:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        customerAccountField = new JTextField(15);
        panel.add(customerAccountField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        customerPasswordField = new JPasswordField(15);
        panel.add(customerPasswordField, gbc);

        JPanel buttonRow = new JPanel();
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> handleCustomerLogin());
        buttonRow.add(loginButton);

        JButton forgotButton = new JButton("Forgot Password?");
        forgotButton.addActionListener(e -> showCustomerForgotDialog());
        buttonRow.add(forgotButton);

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> innerCardLayout.show(centerPanel, BUTTON_PANEL));
        buttonRow.add(backButton);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(buttonRow, gbc);

        return panel;
    }

    private JPanel createAdminLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Admin Login"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Admin ID:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        adminIdField = new JTextField(15);
        panel.add(adminIdField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        adminPasswordField = new JPasswordField(15);
        panel.add(adminPasswordField, gbc);

        JPanel buttonRow = new JPanel();
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> handleAdminLogin());
        buttonRow.add(loginButton);

        JButton forgotButton = new JButton("Forgot Password?");
        forgotButton.addActionListener(e -> showAdminForgotDialog());
        buttonRow.add(forgotButton);

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> innerCardLayout.show(centerPanel, BUTTON_PANEL));
        buttonRow.add(backButton);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(buttonRow, gbc);

        return panel;
    }

    private void handleCustomerLogin() {
        String accountNumber = customerAccountField.getText();
        String password = new String(customerPasswordField.getPassword());

        if (accountNumber.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Account Number and Password.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Customer customer = mainApp.handleCustomerLogin(accountNumber, password);
            JOptionPane.showMessageDialog(this, "Welcome, " + customer.getName() + "!", "Login Successful", JOptionPane.INFORMATION_MESSAGE);
            customerAccountField.setText("");
            customerPasswordField.setText("");
            mainApp.showCustomerDashboard(customer); 
            innerCardLayout.show(centerPanel, BUTTON_PANEL); 
        } catch (AccountNotFoundException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showCustomerForgotDialog() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Customer Password Reset", Dialog.ModalityType.APPLICATION_MODAL);
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Account Number:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        JTextField accField = new JTextField(15);
        panel.add(accField, gbc);

    gbc.gridx = 0; gbc.gridy = 1;
    panel.add(new JLabel("Security Question:"), gbc);
    gbc.gridx = 1; gbc.gridy = 1;
    JComboBox<String> questionBox = new JComboBox<>(PasswordResetManager.SECURITY_QUESTIONS);
    questionBox.setPrototypeDisplayValue("What is the name of your best friend from childhood?");
    panel.add(questionBox, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Your Answer:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        JTextField answerField = new JTextField(15);
        panel.add(answerField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("New Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        JPasswordField newPass = new JPasswordField(15);
        panel.add(newPass, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        JButton resetButton = new JButton("Reset Password");
        panel.add(resetButton, gbc);

        accField.addActionListener(e -> {
            String acc = accField.getText().trim();
            if (acc.isEmpty()) return;
            Customer found = mainApp.getCustomers().stream().filter(c -> c.getAccount().getAccountId().equals(acc)).findFirst().orElse(null);
            if (found == null) {
                questionBox.setEnabled(false);
                JOptionPane.showMessageDialog(dialog, "Account not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (found.getSecurityQuestion() == null) {
                questionBox.setSelectedIndex(0);
                questionBox.setEnabled(true);
            } else {
                questionBox.setSelectedItem(found.getSecurityQuestion());
                questionBox.setEnabled(false);
            }
        });

        resetButton.addActionListener(e -> {
            String acc = accField.getText().trim();
            if (acc.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter account number.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Customer found = mainApp.getCustomers().stream().filter(c -> c.getAccount().getAccountId().equals(acc)).findFirst().orElse(null);
            if (found == null) {
                JOptionPane.showMessageDialog(dialog, "Account not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String selectedQuestion = (String) questionBox.getSelectedItem();
            String answer = answerField.getText().trim();
            if (answer.isEmpty()) { JOptionPane.showMessageDialog(dialog, "Please enter answer.", "Error", JOptionPane.ERROR_MESSAGE); return; }

            // If no question set, allow setting new question+answer then reset password
            if (found.getSecurityQuestion() == null) {
                found.setSecurityQuestion(selectedQuestion);
                found.setSecurityAnswer(answer);
                String np = new String(newPass.getPassword()).trim();
                if (np.isEmpty()) { JOptionPane.showMessageDialog(dialog, "Please enter new password.", "Error", JOptionPane.ERROR_MESSAGE); return; }
                PasswordResetManager.resetCustomerPassword(found, np);
                DataManager.saveCustomers(mainApp.getCustomers());
                JOptionPane.showMessageDialog(dialog, "Security question set and password reset successful.", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                return;
            }

            // Verify existing answer
            if (!PasswordResetManager.verifyCustomerSecurityAnswer(found, answer)) {
                JOptionPane.showMessageDialog(dialog, "Incorrect answer.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String np = new String(newPass.getPassword()).trim();
            if (np.isEmpty()) { JOptionPane.showMessageDialog(dialog, "Please enter new password.", "Error", JOptionPane.ERROR_MESSAGE); return; }
            PasswordResetManager.resetCustomerPassword(found, np);
            DataManager.saveCustomers(mainApp.getCustomers());
            JOptionPane.showMessageDialog(dialog, "Password reset successful.", "Success", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });

        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showAdminForgotDialog() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Admin Password Reset", Dialog.ModalityType.APPLICATION_MODAL);
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Admin ID:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        JTextField idField = new JTextField(15);
        panel.add(idField, gbc);

    gbc.gridx = 0; gbc.gridy = 1;
    panel.add(new JLabel("Security Question:"), gbc);
    gbc.gridx = 1; gbc.gridy = 1;
    JComboBox<String> questionBox = new JComboBox<>(PasswordResetManager.SECURITY_QUESTIONS);
    questionBox.setPrototypeDisplayValue("What is the name of your best friend from childhood?");
    panel.add(questionBox, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Your Answer:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        JTextField answerField = new JTextField(15);
        panel.add(answerField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("New Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        JPasswordField newPass = new JPasswordField(15);
        panel.add(newPass, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        JButton resetButton = new JButton("Reset Password");
        panel.add(resetButton, gbc);

        idField.addActionListener(e -> {
            String id = idField.getText().trim();
            if (id.isEmpty()) return;
            Admin admin = mainApp.getAdmin();
            if (!admin.getAdminId().equals(id)) { questionBox.setEnabled(false); JOptionPane.showMessageDialog(dialog, "Admin ID not found.", "Error", JOptionPane.ERROR_MESSAGE); return; }
            if (admin.getSecurityQuestion() == null) {
                questionBox.setSelectedIndex(0);
                questionBox.setEnabled(true);
            } else {
                questionBox.setSelectedItem(admin.getSecurityQuestion());
                questionBox.setEnabled(false);
            }
        });

        resetButton.addActionListener(e -> {
            String id = idField.getText().trim();
            if (id.isEmpty()) { JOptionPane.showMessageDialog(dialog, "Please enter Admin ID.", "Error", JOptionPane.ERROR_MESSAGE); return; }

            Admin admin = mainApp.getAdmin();
            if (!admin.getAdminId().equals(id)) { JOptionPane.showMessageDialog(dialog, "Admin ID not found.", "Error", JOptionPane.ERROR_MESSAGE); return; }

            String selectedQuestion = (String) questionBox.getSelectedItem();
            String answer = answerField.getText().trim();
            String np = new String(newPass.getPassword()).trim();

            if (admin.getSecurityQuestion() == null) {
                if (answer.isEmpty()) { JOptionPane.showMessageDialog(dialog, "Please enter an answer for the security question.", "Error", JOptionPane.ERROR_MESSAGE); return; }
                if (np.isEmpty()) { JOptionPane.showMessageDialog(dialog, "New password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE); return; }
                admin.setSecurityQuestion(selectedQuestion);
                admin.setSecurityAnswer(answer);
                PasswordResetManager.resetAdminPassword(admin, np);
                DataManager.saveAdmin(admin);
                JOptionPane.showMessageDialog(dialog, "Security question set and password reset successful.", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                return;
            }

            if (!PasswordResetManager.verifyAdminSecurityAnswer(admin, answer)) { JOptionPane.showMessageDialog(dialog, "Security answer incorrect.", "Error", JOptionPane.ERROR_MESSAGE); return; }
            if (np.isEmpty()) { JOptionPane.showMessageDialog(dialog, "New password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE); return; }
            PasswordResetManager.resetAdminPassword(admin, np);
            DataManager.saveAdmin(admin);
            JOptionPane.showMessageDialog(dialog, "Password reset successful.", "Success", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });

        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void handleAdminLogin() {
        String adminId = adminIdField.getText();
        String password = new String(adminPasswordField.getPassword());

        if (adminId.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Admin ID and Password.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (mainApp.handleAdminLogin(adminId, password)) {
            JOptionPane.showMessageDialog(this, "Welcome, " + mainApp.getAdmin().getName() + "!", "Login Successful", JOptionPane.INFORMATION_MESSAGE);
            adminIdField.setText("");
            adminPasswordField.setText("");
            mainApp.showAdminDashboard();
            innerCardLayout.show(centerPanel, BUTTON_PANEL); 
        } else {
            JOptionPane.showMessageDialog(this, "Invalid Admin ID or Password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}