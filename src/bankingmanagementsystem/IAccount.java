package bankingmanagementsystem;

import bankingmanagementsystem.exceptions.InsufficientFundsException;
import bankingmanagementsystem.exceptions.InvalidAmountException;

public interface IAccount {
    void deposit(double amount) throws InvalidAmountException;
    void withdraw(double amount) throws InsufficientFundsException, InvalidAmountException;
    double getBalance();
}

