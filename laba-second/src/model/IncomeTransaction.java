package model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class IncomeTransaction extends Transaction {

    private final String source;

    public IncomeTransaction(UUID id, LocalDate date, BigDecimal amount, String source) {
        super(id, date, amount);
        this.source = source;
    }

    public String getSource() {
        return source;
    }

    @Override
    public BigDecimal balanceImpact() {
        // Доход увеличивает баланс
        return getAmount();
    }
}
