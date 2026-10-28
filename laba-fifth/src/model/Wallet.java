package model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Кошелек из лабораторной 4. Теперь он реализует Identifiable<String>,
 * поэтому тот же самый InMemoryRepository хранит и кошельки — с другим типом id.
 */
public class Wallet implements Identifiable<String> {
    private final String id;
    private final String name;
    private final BigDecimal balance;

    public Wallet(String id, String name, BigDecimal balance) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID кошелька не может быть пустым");
        }
        if (balance == null || balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Баланс не может быть отрицательным");
        }
        this.id = id;
        this.name = name;
        this.balance = balance;
    }

    @Override
    public String getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getBalance() { return balance; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Wallet wallet = (Wallet) o;
        return id.equals(wallet.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Wallet{id=%s, name=%s, balance=%s}", id, name, balance);
    }
}
