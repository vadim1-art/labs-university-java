import check.SelfCheck;
import experiment.ListPerformanceExperiment;
import experiment.SearchEfficiencyExperiment;
import experiment.TypeErasureDemo;
import model.Identifiable;
import model.Transaction;
import model.TransactionComparators;
import model.TransactionType;
import model.Wallet;
import repository.InMemoryRepository;
import repository.Repository;
import service.TransactionService;
import util.CollectionUtils;
import util.SampleData;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {
        Repository<Transaction, UUID> repository = new InMemoryRepository<>();
        TransactionService service = new TransactionService(repository);
        for (Transaction transaction : SampleData.transactions()) {
            service.add(transaction);
        }

        printSection("Все операции (в порядке добавления)");
        printAll(service.getAll());
        Transaction lunch = service.getAll().get(0);

        printSection("1. Поиск по UUID — Map<UUID, Transaction>");
        System.out.println("Ищем существующий id: "
                + service.findById(lunch.getId()).map(Transaction::toString).orElse("не найдена"));
        Optional<Transaction> missing = service.findById(UUID.randomUUID());
        System.out.println("Ищем случайный id: "
                + (missing.isPresent() ? missing.get() : "не найдена (Optional.empty)"));

        printSection("2. Уникальные категории — Set<Category>");
        System.out.println(service.getUniqueCategories());

        printSection("3. Расходы по датам без Stream API — TreeMap<LocalDate, BigDecimal>");
        for (Map.Entry<LocalDate, BigDecimal> entry : service.sumByDate(TransactionType.EXPENSE).entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }

        printSection("4. Повторяющиеся категории (встречаются в 2+ операциях)");
        System.out.println(service.getRepeatedCategories());

        printSection("5. Сортировка через Comparator");
        System.out.println("-- по дате:");
        printAll(service.getSorted(TransactionComparators.BY_DATE));
        System.out.println("-- по сумме:");
        printAll(service.getSorted(TransactionComparators.BY_AMOUNT));
        System.out.println("-- по категории, затем по дате:");
        printAll(service.getSorted(TransactionComparators.BY_CATEGORY_THEN_DATE));

        printSection("6. Удаление операции");
        System.out.println("Удаляем \"" + lunch.getDescription() + "\": " + service.delete(lunch.getId()));
        System.out.println("Удаляем повторно: " + service.delete(lunch.getId()));
        System.out.println("Осталось операций: " + service.getAll().size());

        printSection("7. Тот же InMemoryRepository для кошельков — Repository<Wallet, String>");
        Repository<Wallet, String> wallets = new InMemoryRepository<>();
        wallets.save(new Wallet("CARD", "Дебетовая карта", new BigDecimal("15000.00")));
        wallets.save(new Wallet("CASH", "Наличные", new BigDecimal("2500.00")));
        // wallets.save(lunch);  // <- не скомпилируется: Transaction не является Wallet
        System.out.println(wallets.findById("CARD").orElseThrow());
        System.out.println("Всего кошельков: " + wallets.findAll().size());

        printSection("8. PECS: copyAll(Collection<? extends T>, Collection<? super T>)");
        List<Transaction> remaining = service.getAll();
        List<Object> journal = new ArrayList<>();
        journal.add("Журнал операций");
        CollectionUtils.copyAll(remaining, journal);
        System.out.println("List<Transaction> из " + remaining.size()
                + " операций скопирован в List<Object>, в нем теперь " + journal.size() + " элементов");
        List<Identifiable<UUID>> identifiables = new ArrayList<>();
        CollectionUtils.copyAll(remaining, identifiables);
        System.out.println("Тот же метод копирует в List<Identifiable<UUID>>: " + identifiables.size() + " элементов");
        System.out.println("Сумма оставшихся операций (totalAmount): " + CollectionUtils.totalAmount(remaining));

        TypeErasureDemo.run();
        ListPerformanceExperiment.run();
        SearchEfficiencyExperiment.run();
        new SelfCheck().runAll();
    }

    private static void printSection(String title) {
        System.out.println("\n=== " + title + " ===");
    }

    private static void printAll(List<Transaction> transactions) {
        for (Transaction transaction : transactions) {
            System.out.println("  " + transaction);
        }
    }
}
