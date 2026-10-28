package service;

import model.Category;
import model.Transaction;
import model.TransactionType;
import repository.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Задачи лабораторной над операциями. Для каждой выбрана своя структура данных
 * (обоснование — в README.md).
 */
public class TransactionService {

    private final Repository<Transaction, UUID> repository;

    public TransactionService(Repository<Transaction, UUID> repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public void add(Transaction transaction) {
        repository.save(transaction);
    }

    public List<Transaction> getAll() {
        return repository.findAll();
    }

    /** Поиск по UUID: внутри репозитория Map, поэтому O(1) вместо прохода по списку. */
    public Optional<Transaction> findById(UUID id) {
        return repository.findById(id);
    }

    /** Удаление по UUID: Map.remove — тоже O(1). */
    public boolean delete(UUID id) {
        return repository.deleteById(id);
    }

    /**
     * Уникальные категории. Set сам отбрасывает дубликаты (по equals/hashCode).
     * LinkedHashSet, а не HashSet — чтобы категории выводились в порядке первого появления.
     */
    public Set<Category> getUniqueCategories() {
        Set<Category> categories = new LinkedHashSet<>();
        for (Transaction transaction : repository.findAll()) {
            categories.add(transaction.getCategory());
        }
        return categories;
    }

    /**
     * Суммы по датам без Stream API.
     * TreeMap хранит ключи отсортированными, поэтому даты сразу идут по порядку.
     */
    public NavigableMap<LocalDate, BigDecimal> sumByDate(TransactionType type) {
        NavigableMap<LocalDate, BigDecimal> sums = new TreeMap<>();
        for (Transaction transaction : repository.findAll()) {
            if (transaction.getType() != type) {
                continue;
            }
            BigDecimal current = sums.getOrDefault(transaction.getDate(), BigDecimal.ZERO);
            sums.put(transaction.getDate(), current.add(transaction.getAmount()));
        }
        return sums;
    }

    /**
     * Категории, которые встречаются больше чем в одной операции.
     * Set.add() возвращает false, если элемент уже был, — так за один проход O(n)
     * находим повторы без вложенных циклов.
     */
    public Set<Category> getRepeatedCategories() {
        Set<Category> seen = new HashSet<>();
        Set<Category> repeated = new LinkedHashSet<>();
        for (Transaction transaction : repository.findAll()) {
            if (!seen.add(transaction.getCategory())) {
                repeated.add(transaction.getCategory());
            }
        }
        return repeated;
    }

    /**
     * Отсортированная копия операций. Comparator<? super Transaction> — потребитель (PECS):
     * подойдет и компаратор для Transaction, и, например, Comparator<Object>.
     */
    public List<Transaction> getSorted(Comparator<? super Transaction> comparator) {
        List<Transaction> result = repository.findAll();
        result.sort(comparator);
        return result;
    }
}
