package model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class TransferTransaction extends Transaction implements Commissionable {

    private final String targetAccount;
    private final boolean isOutgoing;
    private final BigDecimal fee;

    public TransferTransaction(UUID id, LocalDate date, BigDecimal amount, String targetAccount, boolean isOutgoing, BigDecimal fee) {
        super(id, date, amount);
        this.targetAccount = targetAccount;
        this.isOutgoing = isOutgoing;
        this.fee = (fee != null) ? fee : BigDecimal.ZERO;
    }

    public String getTargetAccount() {
        return targetAccount;
    }

    public boolean isOutgoing() {
        return isOutgoing;
    }

    @Override
    public BigDecimal commission() {
        return fee;
    }

    @Override
    public BigDecimal balanceImpact() {
        if (isOutgoing) {
            // При исходящем переводе списывается сумма транзакции + комиссия
            return getAmount().add(fee).negate();
        } else {
            // Входящий перевод
            return getAmount();
        }
    }
}
