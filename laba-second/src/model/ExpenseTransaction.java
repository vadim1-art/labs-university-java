package model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class ExpenseTransaction extends Transaction {

    private final String category;

    public ExpenseTransaction(UUID id, LocalDate date, BigDecimal amount, String category) {
        super(id, date, amount);
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    @Override
    public BigDecimal balanceImpact() {
        // Расход уменьшает баланс
        return getAmount().negate();
    }
}