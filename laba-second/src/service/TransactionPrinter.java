package service;

import model.Transaction;

public class TransactionPrinter {

    // Overloading (Перегрузка 1) — печать одной транзакции
    public void print(Transaction transaction) {
        System.out.println(transaction.toString());
    }

    // Overloading (Перегрузка 2) — печать массива транзакций
    public void print(Transaction[] transactions) {
        System.out.println("=== СПИСОК ТРАНЗАКЦИЙ ===");
        for (Transaction t : transactions) {
            print(t); // Вызов первого метода
        }
    }
}
