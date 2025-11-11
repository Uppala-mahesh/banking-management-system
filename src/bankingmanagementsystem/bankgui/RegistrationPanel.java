package bankingmanagementsystem.bankgui;

import bankingmanagementsystem.Customer;
import bankingmanagementsystem.DataManager;
import bankingmanagementsystem.PasswordResetManager;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class RegistrationPanel extends JPanel {

    private BankingSystemGUI mainApp;

    private JTextField nameField;
    private JTextField dobField;
    private JTextField addressField;
    private JTextField salaryField;
    private JComboBox<String> accountTypeBox;
    private JComboBox<Double> minBalanceBox;
    private JPasswordField passwordField;
    private JComboBox<String> securityQuestionBox;
    private JTextField securityAnswerField;

    public RegistrationPanel(BankingSystemGUI mainApp) {
        this.mainApp = mainApp;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("New Customer Registration", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 24));
        add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        nameField = new JTextField(20);
        dobField = new JTextField(20);
        dobField.setToolTipText("YYYY-MM-DD");
        addressField = new JTextField(20);
        salaryField = new JTextField(20);
        accountTypeBox = new JComboBox<>(new String[]{"Personal", "Business"});
        minBalanceBox = new JComboBox<>(new Double[]{0.0, 5000.0, 10000.0});
        passwordField = new JPasswordField(20);
        securityQuestionBox = new JComboBox<>(PasswordResetManager.SECURITY_QUESTIONS);
        securityAnswerField = new JTextField(20);

        int gridy = 0;
        formPanel.add(new JLabel("Full Name:"), gbc(0, gridy));
        formPanel.add(nameField, gbc(1, gridy++));
        
        formPanel.add(new JLabel("Date of Birth (YYYY-MM-DD):"), gbc(0, gridy));
        formPanel.add(dobField, gbc(1, gridy++));

        formPanel.add(new JLabel("Address:"), gbc(0, gridy));
        formPanel.add(addressField, gbc(1, gridy++));

        formPanel.add(new JLabel("Monthly Salary:"), gbc(0, gridy));
        formPanel.add(salaryField, gbc(1, gridy++));

        formPanel.add(new JLabel("Account Type:"), gbc(0, gridy));
        formPanel.add(accountTypeBox, gbc(1, gridy++));

        formPanel.add(new JLabel("Minimum Balance:"), gbc(0, gridy));
        formPanel.add(minBalanceBox, gbc(1, gridy++));

        formPanel.add(new JLabel("Password:"), gbc(0, gridy));
        formPanel.add(passwordField, gbc(1, gridy++));

        formPanel.add(new JLabel("Security Question:"), gbc(0, gridy));
        formPanel.add(securityQuestionBox, gbc(1, gridy++));

        formPanel.add(new JLabel("Your Answer:"), gbc(0, gridy));
        formPanel.add(securityAnswerField, gbc(1, gridy++));

        add(new JScrollPane(formPanel), BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(e -> handleRegistration());
        buttonsPanel.add(registerButton);

        JButton backButton = new JButton("Back to Login");
        backButton.addActionListener(e -> mainApp.showLoginPanel());
        buttonsPanel.add(backButton);

        add(buttonsPanel, BorderLayout.SOUTH);
    }

    private GridBagConstraints gbc(int x, int y) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    private void handleRegistration() {
        try {
            String name = nameField.getText();
            if (name.trim().isEmpty()) throw new Exception("Name cannot be blank.");

            LocalDate dob = LocalDate.parse(dobField.getText());
            if (dob.isAfter(LocalDate.now())) throw new Exception("Date of Birth cannot be in the future.");

            String address = addressField.getText();
            if (address.trim().isEmpty()) throw new Exception("Address cannot be blank.");

            double salary = Double.parseDouble(salaryField.getText());
            if (salary < 0) throw new Exception("Salary cannot be negative.");

            String accountType = (String) accountTypeBox.getSelectedItem();
            double minBalance = (Double) minBalanceBox.getSelectedItem();
            
            String password = new String(passwordField.getPassword());
            if (password.trim().isEmpty()) throw new Exception("Password cannot be blank.");

            String securityQuestion = (String) securityQuestionBox.getSelectedItem();
            String securityAnswer = securityAnswerField.getText().trim();
            if (securityAnswer.isEmpty()) throw new Exception("Security answer cannot be blank.");

            Customer newCustomer = new Customer(name, dob, address, salary, accountType, minBalance, password);
            newCustomer.setSecurityQuestion(securityQuestion);
            newCustomer.setSecurityAnswer(securityAnswer);
            mainApp.getCustomers().add(newCustomer);
            DataManager.saveCustomers(mainApp.getCustomers());

            String successMessage = "Registration successful!\n" +
                                    "Your new Bank Account Number is: " + newCustomer.getAccount().getAccountId() + "\n" +
                                    "Please use this to log in.";
            JOptionPane.showMessageDialog(this, successMessage, "Registration Successful", JOptionPane.INFORMATION_MESSAGE);

            clearFields();
            mainApp.showLoginPanel();

        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Invalid date format! Please use YYYY-MM-DD.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid salary. Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Registration failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        nameField.setText("");
        dobField.setText("");
        addressField.setText("");
        salaryField.setText("");
        accountTypeBox.setSelectedIndex(0);
        minBalanceBox.setSelectedIndex(0);
        passwordField.setText("");
        securityQuestionBox.setSelectedIndex(0);
        securityAnswerField.setText("");
    }
}