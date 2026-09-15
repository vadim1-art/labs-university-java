package exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends Exception {

    private final BigDecimal currentBalance;
    private final BigDecimal requestedAmount;

    public InsufficientFundsException(String message, BigDecimal currentBalance, BigDecimal requestedAmount) {
        super(message);
        this.currentBalance = currentBalance;
        this.requestedAmount = requestedAmount;
    }

    public InsufficientFundsException(String message, Throwable cause, BigDecimal currentBalance, BigDecimal requestedAmount) {
        super(message, cause);
        this.currentBalance = currentBalance;
        this.requestedAmount = requestedAmount;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }
}
