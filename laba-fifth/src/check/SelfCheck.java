package check;

import model.Category;
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
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Автопроверка лабораторной без внешних библиотек (JUnit появится в лабораторной про тестирование).
 * Каждая проверка печатает [OK] или [FAIL]; в конце — итог.
 * Можно запускать отдельно (main) или из Main.
 */
public class SelfCheck {

    private int passed;
    private int failed;

    public static void main(String[] args) {
        if (!new SelfCheck().runAll()) {
            System.exit(1);
        }
    }

    public boolean runAll() {
        System.out.println("\n=== АВТОПРОВЕРКА ===");
        checkRepositoryCrud();
        checkMissingIdReturnsEmptyOptional();
        checkWrongTypeIsRejected();
        checkSetUniqueness();
        checkEqualsHashCode();
        checkSorting();
        checkGroupingAndRepeatedCategories();
        checkPecsMethods();
        System.out.printf("Итого: пройдено %d из %d проверок%n", passed, passed + failed);
        return failed == 0;
    }

    private void checkRepositoryCrud() {
        Repository<Transaction, UUID> repository = new InMemoryRepository<>();
        Transaction lunch = SampleData.transactions().get(0);

        repository.save(lunch);
        check("CRUD: save + findById находит сохраненную операцию",
                repository.findById(lunch.getId()).orElse(null) == lunch);
        check("CRUD: findAll возвращает 1 элемент", repository.findAll().size() == 1);

        Transaction corrected = new Transaction(lunch.getId(), lunch.getDate(), lunch.getType(),
                new BigDecimal("800.00"), lunch.getCategory(), "Обед (исправлено)");
        repository.save(corrected);
        check("CRUD: save с тем же id обновляет запись, а не дублирует",
                repository.findAll().size() == 1
                        && repository.findById(lunch.getId()).get().getDescription().equals("Обед (исправлено)"));

        check("CRUD: deleteById существующего id -> true", repository.deleteById(lunch.getId()));
        check("CRUD: после удаления findById пуст", repository.findById(lunch.getId()).isEmpty());
        check("CRUD: повторный deleteById -> false", !repository.deleteById(lunch.getId()));

        Repository<Wallet, String> wallets = new InMemoryRepository<>();
        wallets.save(new Wallet("CARD", "Карта", new BigDecimal("100.00")));
        check("CRUD: тот же generic-репозиторий работает для Wallet со String id",
                wallets.findById("CARD").isPresent());
    }

    private void checkMissingIdReturnsEmptyOptional() {
        Repository<Transaction, UUID> repository = new InMemoryRepository<>();
        repository.save(SampleData.transactions().get(0));
        check("Поиск отсутствующего id -> Optional.empty()",
                repository.findById(UUID.randomUUID()).equals(Optional.empty()));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void checkWrongTypeIsRejected() {
        Repository<Transaction, UUID> repository = new InMemoryRepository<>();
        // repository.save("строка");  // <- не компилируется: String cannot be converted to Transaction

        // Обойти компилятор можно только через raw type. Но и тогда объект не попадет в хранилище:
        // в мостовом методе save(Object) компилятор вставил каст к Identifiable -> ClassCastException.
        Repository raw = repository;
        boolean rejected;
        try {
            raw.save("строка");
            rejected = false;
        } catch (ClassCastException e) {
            rejected = true;
        }
        check("Объект неправильного типа не попадает в репозиторий",
                rejected && repository.findAll().isEmpty());
    }

    private void checkSetUniqueness() {
        Set<Category> categories = new HashSet<>();
        categories.add(new Category("FOOD"));
        categories.add(new Category("food"));
        categories.add(new Category(" Food "));
        categories.add(new Category("CAFE"));
        check("Set: одинаковые категории не дублируются", categories.size() == 2);

        TransactionService service = serviceWithSampleData();
        check("getUniqueCategories: FOOD, CAFE, SALARY, TRANSPORT — по одному разу",
                service.getUniqueCategories().size() == 4);
    }

    private void checkEqualsHashCode() {
        UUID id = UUID.randomUUID();
        Category food = new Category("FOOD");
        Transaction a = new Transaction(id, LocalDate.of(2026, 9, 1), TransactionType.EXPENSE,
                new BigDecimal("100.00"), food, "A");
        Transaction sameId = new Transaction(id, LocalDate.of(2026, 9, 5), TransactionType.INCOME,
                new BigDecimal("999.00"), new Category("SALARY"), "B");
        Transaction other = new Transaction(LocalDate.of(2026, 9, 1), TransactionType.EXPENSE,
                new BigDecimal("100.00"), food, "A");

        check("equals: операции с одинаковым id равны (симметрично)", a.equals(sameId) && sameId.equals(a));
        check("hashCode: у равных объектов одинаковый hashCode", a.hashCode() == sameId.hashCode());
        check("equals: разные id -> не равны, даже если остальные поля совпадают", !a.equals(other));
        check("equals: рефлексивность и сравнение с null", a.equals(a) && !a.equals(null));

        Set<Transaction> set = new HashSet<>(List.of(a, sameId, other));
        check("HashSet<Transaction> опирается на equals/hashCode: 3 объекта -> 2 элемента", set.size() == 2);
        check("Category: equals/hashCode согласованы",
                new Category("food").equals(food) && new Category("food").hashCode() == food.hashCode());
    }

    private void checkSorting() {
        TransactionService service = serviceWithSampleData();

        List<Transaction> byDate = service.getSorted(TransactionComparators.BY_DATE);
        check("Сортировка по дате: даты не убывают", isSorted(byDate, TransactionComparators.BY_DATE));

        List<Transaction> byAmount = service.getSorted(TransactionComparators.BY_AMOUNT);
        check("Сортировка по сумме: суммы не убывают", isSorted(byAmount, TransactionComparators.BY_AMOUNT));
        check("Сортировка по сумме: первая — 65.00 (Метро), последняя — 50000.00 (Зарплата)",
                byAmount.get(0).getDescription().equals("Метро")
                        && byAmount.get(byAmount.size() - 1).getDescription().equals("Зарплата"));

        List<Transaction> byCategory = service.getSorted(TransactionComparators.BY_CATEGORY_THEN_DATE);
        List<String> descriptions = new ArrayList<>();
        for (Transaction transaction : byCategory) {
            descriptions.add(transaction.getDescription());
        }
        check("Сортировка по категории, затем по дате",
                descriptions.equals(List.of("Кофе", "Кофе с собой", "Продукты", "Обед", "Зарплата", "Метро")));
    }

    private void checkGroupingAndRepeatedCategories() {
        TransactionService service = serviceWithSampleData();

        NavigableMap<LocalDate, BigDecimal> sums = service.sumByDate(TransactionType.EXPENSE);
        check("Суммы расходов по датам: 3 даты, ключи по возрастанию",
                sums.size() == 3 && sums.firstKey().equals(LocalDate.of(2026, 9, 1)));
        check("Суммы расходов: 01.09 = 120 + 2300 = 2420.00",
                sums.get(LocalDate.of(2026, 9, 1)).compareTo(new BigDecimal("2420.00")) == 0);

        Set<Category> repeated = service.getRepeatedCategories();
        check("Повторяющиеся категории: FOOD и CAFE",
                repeated.size() == 2 && repeated.contains(new Category("FOOD")) && repeated.contains(new Category("CAFE")));
    }

    private void checkPecsMethods() {
        List<Transaction> source = SampleData.transactions();
        List<Object> target = new ArrayList<>();
        CollectionUtils.copyAll(source, target);
        check("PECS: List<Transaction> копируется в List<Object>",
                target.size() == source.size() && target.containsAll(source));
        check("PECS: totalAmount принимает Set<Transaction>",
                CollectionUtils.totalAmount(new HashSet<>(source)).compareTo(new BigDecimal("53585.00")) == 0);
    }

    private TransactionService serviceWithSampleData() {
        TransactionService service = new TransactionService(new InMemoryRepository<>());
        for (Transaction transaction : SampleData.transactions()) {
            service.add(transaction);
        }
        return service;
    }

    private static <T> boolean isSorted(List<T> list, Comparator<? super T> comparator) {
        for (int i = 1; i < list.size(); i++) {
            if (comparator.compare(list.get(i - 1), list.get(i)) > 0) {
                return false;
            }
        }
        return true;
    }

    private void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("[OK]   " + name);
        } else {
            failed++;
            System.out.println("[FAIL] " + name);
        }
    }
}
