import model.*;
import service.TransactionCalculator;
import service.TransactionPrinter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        // 1. Создание объектов разных подтипов
        Transaction[] transactions = new Transaction[] {
                new IncomeTransaction(id1, LocalDate.now().minusDays(2),
                        new BigDecimal("50000.00"), "Зарплата"),
                new ExpenseTransaction(id2, LocalDate.now().minusDays(1),
                        new BigDecimal("3000.00"), "Супермаркет"),
                new TransferTransaction(UUID.randomUUID(), LocalDate.now(),
                        new BigDecimal("10000.00"), "Карта друга", true, new BigDecimal("150.00")),
                new CashbackTransaction(UUID.randomUUID(), LocalDate.now(),
                        new BigDecimal("500.00"), "Яндекс Маркет")
        };

        // 2. Демонстрация Перегрузки
        TransactionPrinter printer = new TransactionPrinter();
        printer.print(transactions);

        // 3. Полиморфный расчет общего влияния на баланс
        TransactionCalculator calculator = new TransactionCalculator();
        BigDecimal netImpact = calculator.calculateTotalImpact(transactions);

        System.out.println("\nРЕЗУЛЬТАТ РАСЧЕТА");
        System.out.println("Итоговое влияние на баланс: " + netImpact + " руб.");

        // 4. Проверка контракта equals() и hashCode()
        System.out.println("\n=== ПРОВЕРКА EQUALS / HASHCODE ===");
        Transaction t1 = new IncomeTransaction(id1, LocalDate.now(),
                new BigDecimal("50000.00"), "Зарплата");
        Transaction t2 = new ExpenseTransaction(id1, LocalDate.now(),
                new BigDecimal("50000.00"), "Иная транзакция с тем же UUID");

        System.out.println("t1.equals(t2): " + t1.equals(t2));
        System.out.println("t1.hashCode() == t2.hashCode(): " + (t1.hashCode() == t2.hashCode()));
    }
}