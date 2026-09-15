package service;

import model.Transaction;

import java.math.BigDecimal;

public class TransactionCalculator {

    public BigDecimal calculateTotalImpact(Transaction[] transactions) {
        BigDecimal total = BigDecimal.ZERO;
        for (Transaction transaction : transactions) {
            // Позднее связывание (Late binding)
            total = total.add(transaction.balanceImpact());
        }
        return total;
    }
}
