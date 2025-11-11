package bankingmanagementsystem;

import bankingmanagementsystem.exceptions.AccountNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class BankingSystem {
    private static List<Customer> customers;
    private static Admin admin;
    private static Map<Integer, Message> supportMessages;
    private static AtomicInteger messageIdCounter;

    public static void main(String[] args) {
        customers = DataManager.loadCustomers();
        admin = DataManager.loadAdmin();
        supportMessages = DataManager.loadSupportMessages();
        int maxId = supportMessages.keySet().stream().max(Integer::compare).orElse(0);
        messageIdCounter = new AtomicInteger(maxId);
        admin.setCustomers(customers);

        Scanner sc = new Scanner(System.in);
        boolean running = true;
        while (running) {
            System.out.println("\n**************************************************************");
            System.out.println("* *");
            System.out.println("* P R I M E    B A N K                      *");
            System.out.println("* *");
            System.out.println("* Experience Banking Beyond Limits.                *");
            System.out.println("* *");
            System.out.println("**************************************************************");
            System.out.println("\n1. Admin Login");
            System.out.println("2. Existing Customer Login");
            System.out.println("3. New Customer Registration");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");
            String choice = sc.nextLine();
            switch (choice) {
                case "1":
                    adminLogin(sc);
                    break;
                case "2":
                    customerLogin(sc);
                    break;
                case "3":
                    ActionHandler.registerNewCustomer(sc, customers);
                    break;
                case "4":
                    running = false;
                    System.out.println("Thank you for using our services. Shutting down.");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
        sc.close();
    }

    public static List<Customer> getCustomers() {
        return customers;
    }

    private static void adminLogin(Scanner sc) {
        System.out.print("Enter Admin ID: ");
        String adminId = sc.nextLine();
        System.out.print("Enter Admin Password: ");
        String password = sc.nextLine();
        if (admin.getAdminId().equals(adminId) && admin.checkPassword(password)) {
            System.out.println("\nAdmin login successful. Welcome, " + admin.getName() + "!");
            MenuManager.adminMenu(sc, admin, customers, supportMessages);
        } else {
            System.out.println("Invalid Admin ID or Password.");
        }
    }

    private static void customerLogin(Scanner sc) {
        try {
            System.out.print("Enter your Bank Account Number: ");
            String accountNumber = sc.nextLine();
            System.out.print("Enter your Password: ");
            String password = sc.nextLine();
            Customer customer = findCustomerByAccountNumber(accountNumber);
            if (customer.checkPassword(password)) {
                System.out.println("\nLogin successful. Welcome, " + customer.getName() + "!");
                MenuManager.customerMenu(customer, sc, customers, supportMessages, messageIdCounter);
            } else {
                System.out.println("Invalid Account Number or Password.");
            }
        } catch (AccountNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    private static Customer findCustomerByAccountNumber(String accountNumber) throws AccountNotFoundException {
        return customers.stream()
                .filter(c -> c.getAccount().getAccountId().equals(accountNumber))
                .findFirst()
                .orElseThrow(() -> new AccountNotFoundException("No customer found with account number: " + accountNumber));
    }
}

