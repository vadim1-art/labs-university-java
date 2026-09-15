package model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Transaction {
    private final long id;
    private final BigDecimal amount;
    private final LocalDate date;
    private final TransactionType type;
    private final Category category;
    private String description;

    // Конструктор 1: с описанием
    public Transaction(long id, BigDecimal amount, LocalDate date, TransactionType type, Category category, String description) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма транзакции должна быть больше нуля");
        }
        if (date == null) {
            throw new IllegalArgumentException("Дата транзакции не может быть null");
        }
        if (type == null) {
            throw new IllegalArgumentException("Тип транзакции обязателен");
        }
        if (category == null) {
            throw new IllegalArgumentException("Категория обязательна");
        }

        this.id = id;
        this.amount = amount;
        this.date = date;
        this.type = type;
        this.category = category;
        setDescription(description);
    }

    // Конструктор 2 (перегруженный): без описания по умолчанию
    public Transaction(long id, BigDecimal amount, LocalDate date, TransactionType type, Category category) {
        this(id, amount, date, type, category, "Без описания");
    }

    public void setDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Описание не может быть пустым");
        }
        this.description = description.trim();
    }

    public long getId() { return id; }
    public BigDecimal getAmount() { return amount; }
    public LocalDate getDate() { return date; }
    public TransactionType getType() { return type; }
    public Category getCategory() { return category; }
    public String getDescription() { return description; }
}
