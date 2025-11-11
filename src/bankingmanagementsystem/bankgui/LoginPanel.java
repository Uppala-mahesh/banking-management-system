package bankingmanagementsystem.bankgui;

import bankingmanagementsystem.Customer;
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

        JButton forgotPasswordButton = new JButton("Forgot Password?");
        forgotPasswordButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        forgotPasswordButton.addActionListener(e -> mainApp.showForgotPasswordPanel());
        gbc.gridy = 2;
        buttonPanel.add(forgotPasswordButton, gbc);

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