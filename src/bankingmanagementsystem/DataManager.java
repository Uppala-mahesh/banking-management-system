package bankingmanagementsystem;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DataManager {
    private static final String CUSTOMERS_FILE = "customers.dat";
    private static final String ADMIN_FILE = "admin.dat";
    private static final String MESSAGES_FILE = "support_messages.dat";
    public static void saveCustomers(List<Customer> customers) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(CUSTOMERS_FILE))) {
            oos.writeObject(customers);
        } catch (IOException e) {
            System.err.println("Error saving customer data: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static List<Customer> loadCustomers() {
        File file = new File(CUSTOMERS_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                List<Customer> customers = (List<Customer>) ois.readObject();

                Set<String> usedAccountNumbers = customers.stream()
                        .map(c -> c.getAccount().getAccountId())
                        .collect(Collectors.toSet());
                AccountNumberGenerator.loadUsedAccountNumbers(usedAccountNumbers);

                return customers;
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error loading customer data: " + e.getMessage());
            }
        }
        return new ArrayList<>();
    }

    public static void saveAdmin(Admin admin) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ADMIN_FILE))) {
            oos.writeObject(admin);
        } catch (IOException e) {
            System.err.println("Error saving admin data: " + e.getMessage());
        }
    }

    public static Admin loadAdmin() {
        File file = new File(ADMIN_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                return (Admin) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error loading admin data: " + e.getMessage());
            }
        }
        // If no admin file exists, create a default one.
        Admin defaultAdmin = new Admin("admin001", "Default Admin", "ADMIN001", "adminpass");
        saveAdmin(defaultAdmin);
        return defaultAdmin;
    }

 
    public static void saveSupportMessages(Map<Integer, Message> messages) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(MESSAGES_FILE))) {
            oos.writeObject(messages);
        } catch (IOException e) {
            System.err.println("Error saving support messages: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<Integer, Message> loadSupportMessages() {
        File file = new File(MESSAGES_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                return (Map<Integer, Message>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error loading support messages: " + e.getMessage());
            }
        }
        return new ConcurrentHashMap<>();
    }
}
