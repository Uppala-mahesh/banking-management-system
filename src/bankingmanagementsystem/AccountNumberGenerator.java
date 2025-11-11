package bankingmanagementsystem;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
public class AccountNumberGenerator {
    private static final Set<String> usedAccountNumbers = new HashSet<>();
    private static final Random random = new Random();
    public static String generateUniqueAccountNumber() {
        String accountNumber;
        do {
            long number = 1000000000L + (long)(random.nextDouble() * 9000000000L);
            accountNumber = String.valueOf(number);
        } while (usedAccountNumbers.contains(accountNumber));

        usedAccountNumbers.add(accountNumber);
        return accountNumber;
    }
    public static void loadUsedAccountNumbers(Set<String> numbers) {
        if (numbers != null) {
            usedAccountNumbers.addAll(numbers);
        }
    }
}
