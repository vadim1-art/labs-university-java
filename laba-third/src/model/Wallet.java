package model;

import java.math.BigDecimal;

public class Wallet {
    private final String id;
    private final String name;
    private BigDecimal balance;

    public Wallet(String id, String name, BigDecimal initialBalance) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID кошелька не может быть пустым");
        }
        if (initialBalance == null || initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Начальный баланс не может быть отрицательным");
        }
        this.id = id;
        this.name = name;
        this.balance = initialBalance;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getBalance() { return balance; }

    public void deposit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    public void withdraw(BigDecimal amount) {
        this.balance = this.balance.subtract(amount);
    }
}
