package model;

import java.math.BigDecimal;

public class Wallet {
    private final String name;
    private BigDecimal balance;

    public Wallet(String name, BigDecimal initialBalance) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя кошелька не может быть пустым");
        }
        if (initialBalance == null || initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Начальный баланс не может быть отрицательным");
        }
        this.name = name;
        this.balance = initialBalance;
    }

    public void deposit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма пополнения должна быть больше 0");
        }
        this.balance = this.balance.add(amount);
    }

    public void withdraw(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма списания должна быть больше 0");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Недостаточно средств на балансе");
        }
        this.balance = this.balance.subtract(amount);
    }

    public String getName() { return name; }
    public BigDecimal getBalance() { return balance; }
}
