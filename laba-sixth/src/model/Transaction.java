package model;

import exception.InvalidTransactionException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Финансовая операция.
 *
 * Реализует Identifiable<UUID>, чтобы ее можно было хранить в обобщенном
 * InMemoryRepository<Transaction, UUID>.
 *
 * Comparable НЕ реализуется намеренно: у операции нет одного "естественного" порядка.
 * Ее сортируют то по дате, то по сумме, то по категории — каждый такой порядок
 * вынесен в отдельный Comparator (см. TransactionComparators).
 */
public class Transaction implements Identifiable<UUID> {

    private final UUID id;
    private final LocalDate date;
    private final TransactionType type;
    private final BigDecimal amount;
    private final Category category;
    private final String description;

    public Transaction(UUID id, LocalDate date, TransactionType type,
                       BigDecimal amount, Category category, String description) {
        if (id == null) {
            throw new InvalidTransactionException("Идентификатор (id) не может быть null");
        }
        if (date == null) {
            throw new InvalidTransactionException("Дата транзакции не может быть null");
        }
        if (type == null) {
            throw new InvalidTransactionException("Тип транзакции обязателен");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Сумма транзакции должна быть больше нуля. Передано: " + amount);
        }
        if (category == null) {
            throw new InvalidTransactionException("Категория обязательна");
        }
        this.id = id;
        this.date = date;
        this.type = type;
        // Деньги храним с 2 знаками после запятой: 750 -> 750.00
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
        this.category = category;
        this.description = description == null ? "" : description.trim();
    }

    // Перегруженный конструктор: id генерируется автоматически
    public Transaction(LocalDate date, TransactionType type,
                       BigDecimal amount, Category category, String description) {
        this(UUID.randomUUID(), date, type, amount, category, description);
    }

    @Override
    public UUID getId() { return id; }
    public LocalDate getDate() { return date; }
    public TransactionType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public Category getCategory() { return category; }
    public String getDescription() { return description; }

    /**
     * Две операции — это одна и та же операция, если совпадает id.
     * Остальные поля (сумма, описание) могут быть исправлены, а сущность останется той же.
     * hashCode считается по тому же полю, иначе HashMap/HashSet работали бы неправильно.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("%s | %s | %-7s | %10s | %-13s | %s",
                id.toString().substring(0, 8), date, type, amount, category, description);
    }
}
