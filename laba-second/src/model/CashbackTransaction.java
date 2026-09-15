package model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class CashbackTransaction extends Transaction {

    private final String merchant;

    public CashbackTransaction(UUID id, LocalDate date, BigDecimal amount, String merchant) {
        super(id, date, amount);
        this.merchant = merchant;
    }

    public String getMerchant() {
        return merchant;
    }

    @Override
    public BigDecimal balanceImpact() {
        // Кэшбэк возвращает деньги — увеличение баланса
        return getAmount();
    }
}
