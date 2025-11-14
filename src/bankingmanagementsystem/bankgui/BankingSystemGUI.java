package bankingmanagementsystem.bankgui;

import bankingmanagementsystem.*;
import bankingmanagementsystem.exceptions.AccountNotFoundException;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class BankingSystemGUI {
    private static List<Customer> customers;
    private static Admin admin;
    private static Map<Integer, Message> supportMessages;
    private static AtomicInteger messageIdCounter;

    private JFrame mainFrame;
    private JPanel mainPanel;
    private CardLayout cardLayout;

    public static final String LOGIN_PANEL = "LoginPanel";
    public static final String REGISTRATION_PANEL = "RegistrationPanel";
    public static final String CUSTOMER_DASHBOARD = "CustomerDashboard";
    public static final String ADMIN_DASHBOARD = "AdminDashboard";

    public BankingSystemGUI() {
        loadData();

        mainFrame = new JFrame("Prime Bank");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(800, 600);
        mainFrame.setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Existing panels
        LoginPanel loginPanel = new LoginPanel(this);
        RegistrationPanel registrationPanel = new RegistrationPanel(this);
        CustomerDashboard customerDashboard = new CustomerDashboard(this);
        AdminDashboard adminDashboard = new AdminDashboard(this);

        // Add panels to main container
        mainPanel.add(loginPanel, LOGIN_PANEL);
        mainPanel.add(registrationPanel, REGISTRATION_PANEL);
        mainPanel.add(customerDashboard, CUSTOMER_DASHBOARD);
        mainPanel.add(adminDashboard, ADMIN_DASHBOARD);

        mainFrame.add(mainPanel);
        mainFrame.setVisible(true);

        showLoginPanel();
    }

    private void loadData() {
        customers = DataManager.loadCustomers();
        admin = DataManager.loadAdmin();
        supportMessages = DataManager.loadSupportMessages();

        int maxId = supportMessages.keySet().stream().max(Integer::compare).orElse(0);
        messageIdCounter = new AtomicInteger(maxId);

        admin.setCustomers(customers);
    }

    public void showLoginPanel() {
        cardLayout.show(mainPanel, LOGIN_PANEL);
    }

    public void showRegistrationPanel() {
        cardLayout.show(mainPanel, REGISTRATION_PANEL);
    }

    public void showCustomerDashboard(Customer customer) {
        CustomerDashboard dashboard = (CustomerDashboard) getPanel(CUSTOMER_DASHBOARD);
        dashboard.setCurrentCustomer(customer);
        cardLayout.show(mainPanel, CUSTOMER_DASHBOARD);
    }

    public void showAdminDashboard() {
        AdminDashboard dashboard = (AdminDashboard) getPanel(ADMIN_DASHBOARD);
        dashboard.refreshData();
        cardLayout.show(mainPanel, ADMIN_DASHBOARD);
    }

    private Component getPanel(String panelName) {
        for (Component comp : mainPanel.getComponents()) {
            // A safer way to get the panel without relying on class name matching
            if (comp instanceof LoginPanel && panelName.equals(LOGIN_PANEL)) return comp;
            if (comp instanceof RegistrationPanel && panelName.equals(REGISTRATION_PANEL)) return comp;
            if (comp instanceof CustomerDashboard && panelName.equals(CUSTOMER_DASHBOARD)) return comp;
            if (comp instanceof AdminDashboard && panelName.equals(ADMIN_DASHBOARD)) return comp;
        }
        
        // Fallback for safety, though the above is preferred
        for (Component comp : mainPanel.getComponents()) {
            if (comp.getClass().getSimpleName().equals(panelName)) {
                return comp;
            }
        }
        return null;
    }

    public Customer handleCustomerLogin(String accountNumber, String password) throws AccountNotFoundException {
        Customer customer = findCustomerByAccountNumber(accountNumber);
        if (customer.checkPassword(password)) {
            return customer;
        } else {
            throw new AccountNotFoundException("Invalid Account Number or Password.");
        }
    }

    public boolean handleAdminLogin(String adminId, String password) {
        return admin.getAdminId().equals(adminId) && admin.checkPassword(password);
    }

    private Customer findCustomerByAccountNumber(String accountNumber) throws AccountNotFoundException {
        return customers.stream()
                .filter(c -> c.getAccount().getAccountId().equals(accountNumber))
                .findFirst()
                .orElseThrow(() -> new AccountNotFoundException("No customer found with account number: " + accountNumber));
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public Admin getAdmin() {
        return admin;
    }

    public Map<Integer, Message> getSupportMessages() {
        return supportMessages;
    }

    public AtomicInteger getMessageIdCounter() {
        return messageIdCounter;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(BankingSystemGUI::new);
    }
}