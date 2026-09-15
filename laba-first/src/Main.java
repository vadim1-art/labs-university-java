import model.Category;
import model.Transaction;
import model.TransactionType;
import model.Wallet;
import service.TransactionCalculator;
import service.TransactionPrinter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Создание кошельков
        Wallet mainWallet = new Wallet("Основной счет", new BigDecimal("50000"));
        Wallet cryptoWallet = new Wallet("Криптокошелек", new BigDecimal("1000"));

        // Категории
        Category salary = new Category(1, "Зарплата");
        Category food = new Category(2, "Продукты");
        Category transport = new Category(3, "Транспорт");

        // Создание 5 транзакций
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction(1, new BigDecimal("100000"), LocalDate.now().minusDays(3), TransactionType.INCOME, salary, "Аванс"));
        transactions.add(new Transaction(2, new BigDecimal("2500"), LocalDate.now().minusDays(2), TransactionType.EXPENSE, food, "Супермаркет"));
        transactions.add(new Transaction(3, new BigDecimal("150"), LocalDate.now().minusDays(2), TransactionType.EXPENSE, transport)); // Перегруженный конструктор
        transactions.add(new Transaction(4, new BigDecimal("4300"), LocalDate.now().minusDays(1), TransactionType.EXPENSE, food, "Ресторан"));
        transactions.add(new Transaction(5, new BigDecimal("15000"), LocalDate.now(), TransactionType.INCOME, salary, "Премия"));

        // Вывод транзакций
        TransactionPrinter printer = new TransactionPrinter();
        System.out.println("СПИСОК ТРАНЗАКЦИЙ");
        for (Transaction t : transactions) {
            printer.print(t);
        }

        // Вычисления
        TransactionCalculator calculator = new TransactionCalculator();
        System.out.println("\nИТОГИ");
        System.out.println("Общий доход: " + calculator.calculateTotal(transactions, TransactionType.INCOME));
        System.out.println("Общий расход: " + calculator.calculateTotal(transactions, TransactionType.EXPENSE));
        System.out.println("Чистый баланс: " + calculator.calculateNetBalance(transactions));

        // Демонстрация валидации
        System.out.println("\nПРОВЕРКА ВАЛИДАЦИИ");
        try {
            new Transaction(6, new BigDecimal("-500"), LocalDate.now(), TransactionType.EXPENSE, food, "Ошибка");
        } catch (IllegalArgumentException e) {
            System.out.println("Поймана ошибка: " + e.getMessage());
        }
    }
}