package service;

import model.Transaction;

public class TransactionPrinter {
    public void print(Transaction transaction) {
        System.out.printf("[%s] ID: %d | %-7s | %-12s | %8.2f руб. | %s%n",
                transaction.getDate(),
                transaction.getId(),
                transaction.getType(),
                transaction.getCategory().getName(),
                transaction.getAmount(),
                transaction.getDescription());
    }
}
