package bankingmanagementsystem;

import java.io.Serializable;
import java.util.List;

public class Admin implements Serializable {
    private static final long serialVersionUID = 1L;
    private String userId;
    private String name;
    private String adminId;
    private String password;
    private String securityQuestion;
    private String securityAnswer;
    private transient List<Customer> customers;

    public Admin(String userId, String name, String adminId, String password) {
        this.userId = userId;
        this.name = name;
        this.adminId = adminId;
        this.password = password;
    }

    public String getAdminId() {
        return adminId;
    }

    public String getName() {
        return name;
    }

    public void setCustomers(List<Customer> customers) {
        this.customers = customers;
    }

    public String getSecurityQuestion() {
        return securityQuestion;
    }

    public void setSecurityQuestion(String securityQuestion) {
        this.securityQuestion = securityQuestion;
    }

    public String getSecurityAnswer() {
        return securityAnswer;
    }

    public void setSecurityAnswer(String securityAnswer) {
        this.securityAnswer = securityAnswer;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean checkPassword(String inputPassword) {
        return this.password.equals(inputPassword);
    }

    public void printAllCustomers() {
        System.out.println("\n--- All Bank Customers ---");
        if (customers == null || customers.isEmpty()) {
            System.out.println("No customers found.");
            return;
        }
        for (Customer customer : customers) {
            System.out.printf("Name: %-20s | Account Number: %s | Balance: %.2f\n",
                    customer.getName(),
                    customer.getAccount().getAccountId(),
                    customer.getAccount().getBalance());
        }
    }
}

