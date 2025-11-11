package bankingmanagementsystem;

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class MenuManager {

    public static void adminMenu(Scanner sc, Admin admin, List<Customer> customers, Map<Integer, Message> supportMessages) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n--- Admin Menu ---");
            System.out.println("1. View All Customers");
            System.out.println("2. View Specific Customer Details");
            System.out.println("3. View and Respond to Support Messages");
            System.out.println("4. Delete Customer Account (Single)");
            System.out.println("5. Batch Account Deletion (Multi-threaded)");
            System.out.println("6. Logout");
            System.out.print("Enter your choice: ");
            String choice = sc.nextLine();
            switch (choice) {
                case "1":
                    admin.printAllCustomers();
                    break;
                case "2":
                    ActionHandler.viewCustomerDetails(sc, customers);
                    break;
                case "3":
                    ActionHandler.manageSupportMessages(sc, supportMessages);
                    break;
                case "4":
                    ActionHandler.deleteCustomerAccount(sc, customers);
                    break;
                case "5":
                    ActionHandler.batchDeleteAccounts(sc, customers);
                    break;
                case "6":
                    loggedIn = false;
                    System.out.println("Admin logged out.");
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    public static void customerMenu(Customer customer, Scanner sc, List<Customer> customers, Map<Integer, Message> supportMessages, AtomicInteger messageIdCounter) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n--- Customer Menu: " + customer.getName() + " ---");
            System.out.println("1. View Account Details");
            System.out.println("2. Deposit Money");
            System.out.println("3. Withdraw Money");
            System.out.println("4. Send Money");
            System.out.println("5. View Transaction History");
            System.out.println("6. Apply for Loan");
            System.out.println("7. Pay Loan Installment");
            System.out.println("8. Invest in Fixed Deposit (FD)");
            System.out.println("9. Show My Investments"); // UPDATED: Changed menu text
            System.out.println("10. Contact Support");
            System.out.println("11. Logout");
            System.out.print("Enter your choice: ");
            String choice = sc.nextLine();
            switch (choice) {
                case "1":
                    System.out.println(customer);
                    break;
                case "2":
                    ActionHandler.depositMoney(customer, sc, customers);
                    break;
                case "3":
                    ActionHandler.withdrawMoney(customer, sc, customers);
                    break;
                case "4":
                    ActionHandler.sendMoney(customer, sc, customers);
                    break;
                case "5":
                    customer.printTransactionHistory();
                    break;
                case "6":
                    ActionHandler.applyForLoan(customer, sc, customers);
                    break;
                case "7":
                    ActionHandler.payLoanInstallment(customer, sc, customers);
                    break;
                case "8":
                    ActionHandler.addInvestment(customer, sc, customers);
                    break;
                case "9":
                    // UPDATED: Now calls viewInvestments() to show all active investments
                    customer.viewInvestments(); 
                    break;
                case "10":
                    ActionHandler.contactSupport(customer, sc, supportMessages, messageIdCounter);
                    break;
                case "11":
                    loggedIn = false;
                    System.out.println("Logged out successfully.");
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
}

