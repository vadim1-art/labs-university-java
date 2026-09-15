package model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public abstract class Transaction {

    private final UUID id;
    private final LocalDate date;
    private final BigDecimal amount;

    protected Transaction(UUID id, LocalDate date, BigDecimal amount) {
        if (id == null) {
            throw new IllegalArgumentException("Идентификатор (id) не может быть null");
        }
        if (date == null) {
            throw new IllegalArgumentException("Дата транзакции не может быть null");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма транзакции должна быть больше нуля");
        }
        this.id = id;
        this.date = date;
        this.amount = amount;
    }

    public final UUID getId() {
        return id;
    }

    public final LocalDate getDate() {
        return date;
    }

    public final BigDecimal getAmount() {
        return amount;
    }


    public abstract BigDecimal balanceImpact();

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Transaction that = (Transaction) obj;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("%s{id=%s, date=%s, amount=%.2f, impact=%.2f}",
                getClass().getSimpleName(), id, date, amount, balanceImpact());
    }
}
