package util;

import model.Category;
import model.Transaction;
import model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Начальные операции (первый запуск без файла) и данные для автопроверки.
 */
public final class SampleData {

    private SampleData() {
    }

    public static List<Transaction> transactions() {
        Category food = new Category("FOOD");
        Category cafe = new Category("CAFE");
        Category salary = new Category("SALARY");
        Category transport = new Category("TRANSPORT");

        List<Transaction> list = new ArrayList<>();
        // Строка из примера в задании
        list.add(new Transaction(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
                LocalDate.parse("2026-09-01"), TransactionType.EXPENSE, new BigDecimal("750.00"), food, "Обед"));
        list.add(expense("2026-09-01", "120.00", cafe, "Кофе"));
        list.add(new Transaction(LocalDate.parse("2026-09-01"), TransactionType.INCOME,
                new BigDecimal("50000.00"), salary, "Зарплата"));
        list.add(expense("2026-09-02", "2300.00", food, "Продукты"));
        list.add(expense("2026-09-02", "65.00", transport, "Метро"));
        list.add(expense("2026-09-03", "350.00", cafe, "Кофе с собой"));
        return list;
    }

    private static Transaction expense(String date, String amount, Category category, String description) {
        return new Transaction(LocalDate.parse(date), TransactionType.EXPENSE,
                new BigDecimal(amount), category, description);
    }
}
