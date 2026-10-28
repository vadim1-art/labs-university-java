package util;

import model.Category;
import model.Transaction;
import model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Генератор большого количества операций для экспериментов (100 000 штук и т.п.).
 */
public final class TransactionGenerator {

    private static final Category SALARY = new Category("SALARY");
    private static final Category[] EXPENSE_CATEGORIES = {
            new Category("FOOD"), new Category("CAFE"), new Category("TRANSPORT"),
            new Category("HEALTH"), new Category("ENTERTAINMENT")
    };
    private static final LocalDate START_DATE = LocalDate.of(2026, 9, 1);

    private TransactionGenerator() {
    }

    /**
     * @param seed зерно генератора: с одинаковым seed получаются одинаковые даты/суммы,
     *             поэтому эксперименты повторяемы
     */
    public static List<Transaction> generate(int count, long seed) {
        Random random = new Random(seed);
        List<Transaction> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            LocalDate date = START_DATE.plusDays(random.nextInt(60));
            // Сумма от 1.00 до 5000.00 в копейках -> BigDecimal со scale 2
            BigDecimal amount = BigDecimal.valueOf(100 + random.nextInt(500_000), 2);
            if (random.nextInt(10) == 0) {
                result.add(new Transaction(date, TransactionType.INCOME, amount, SALARY, "Доход #" + i));
            } else {
                Category category = EXPENSE_CATEGORIES[random.nextInt(EXPENSE_CATEGORIES.length)];
                result.add(new Transaction(date, TransactionType.EXPENSE, amount, category, "Расход #" + i));
            }
        }
        return result;
    }
}
