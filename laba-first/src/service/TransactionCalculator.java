package service;


import model.Transaction;
import model.TransactionType;

import java.math.BigDecimal;
import java.util.List;

public class TransactionCalculator {

    public BigDecimal calculateTotal(List<Transaction> transactions, TransactionType type) {
        BigDecimal total = BigDecimal.ZERO;
        for (Transaction t : transactions) {
            if (t.getType() == type) {
                total = total.add(t.getAmount());
            }
        }
        return total;
    }

    public BigDecimal calculateNetBalance(List<Transaction> transactions) {
        BigDecimal income = calculateTotal(transactions, TransactionType.INCOME);
        BigDecimal expense = calculateTotal(transactions, TransactionType.EXPENSE);
        return income.subtract(expense);
    }
}