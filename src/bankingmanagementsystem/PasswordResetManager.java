package bankingmanagementsystem;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages password reset functionality for customers and admins.
 * Uses security questions for verification.
 */
public class PasswordResetManager implements Serializable {
    private static final long serialVersionUID = 1L;

    // Predefined security questions
    public static final String[] SECURITY_QUESTIONS = {
        "What is your mother's maiden name?",
        "What was the name of your first pet?",
        "What city were you born in?",
        "What is your favorite movie?",
        "What is your favorite book?",
        "What is the name of your best friend from childhood?"
    };

    /**
     * Verifies security question answer for a customer.
     * Case-insensitive comparison.
     */
    public static boolean verifyCustomerSecurityAnswer(Customer customer, String providedAnswer) {
        if (customer.getSecurityAnswer() == null) {
            return false;
        }
        return customer.getSecurityAnswer().trim().equalsIgnoreCase(providedAnswer.trim());
    }

    /**
     * Verifies security question answer for an admin.
     * Case-insensitive comparison.
     */
    public static boolean verifyAdminSecurityAnswer(Admin admin, String providedAnswer) {
        if (admin.getSecurityAnswer() == null) {
            return false;
        }
        return admin.getSecurityAnswer().trim().equalsIgnoreCase(providedAnswer.trim());
    }

    /**
     * Generates a reset token (could be used for email verification in future).
     */
    public static String generateResetToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * Resets customer password after security answer verification.
     */
    public static boolean resetCustomerPassword(Customer customer, String newPassword) {
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return false;
        }
        customer.setPassword(newPassword);
        return true;
    }

    /**
     * Resets admin password after security answer verification.
     */
    public static boolean resetAdminPassword(Admin admin, String newPassword) {
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return false;
        }
        admin.setPassword(newPassword);
        return true;
    }
}
