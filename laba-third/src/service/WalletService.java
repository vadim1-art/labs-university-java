package service;

import exception.InsufficientFundsException;
import exception.InvalidTransactionException;
import exception.WalletNotFoundException;
import model.Wallet;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WalletService {

    private static final Logger logger = Logger.getLogger(WalletService.class.getName());
    private final Map<String, Wallet> wallets = new HashMap<>();

    public void addWallet(Wallet wallet) {
        if (wallet == null) {
            throw new InvalidTransactionException("Передан пустой кошелек (null)");
        }
        wallets.put(wallet.getId(), wallet);
        logger.info(() -> String.format("Зарегистрирован кошелек: %s [%s]", wallet.getName(), wallet.getId()));
    }

    public Wallet getWallet(String id) {
        Wallet wallet = wallets.get(id);
        if (wallet == null) {
            throw new WalletNotFoundException("Кошелек с ID '" + id + "' не найден в системе.");
        }
        return wallet;
    }

    /**
     * Перевод средств между кошельками.
     * @throws InsufficientFundsException checked-исключение при нехватке денег на счете
     * @throws InvalidTransactionException unchecked-исключение при некорректных аргументах
     * @throws WalletNotFoundException unchecked-исключение при отсутствии кошелька
     */
    public void transfer(String sourceId, String targetId, BigDecimal amount) throws InsufficientFundsException {
        logger.log(Level.INFO, "Запрос на перевод {0} руб. со счета {1} на счет {2}",
                new Object[]{amount, sourceId, targetId});

        // 1. Валидация аргументов (Unchecked)
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            InvalidTransactionException ex = new InvalidTransactionException("Сумма перевода должна быть строго больше 0. Передано: " + amount);
            logger.log(Level.WARNING, "Ошибка валидации перевода", ex);
            throw ex;
        }

        if (sourceId != null && sourceId.equals(targetId)) {
            throw new InvalidTransactionException("Нельзя выполнить перевод на один и тот же кошелек.");
        }

        // 2. Поиск кошельков (Unchecked)
        Wallet source = getWallet(sourceId);
        Wallet target = getWallet(targetId);

        // 3. Проверка баланса (Checked)
        if (source.getBalance().compareTo(amount) < 0) {
            InsufficientFundsException ex = new InsufficientFundsException(
                    String.format("Недостаточно средств на кошельке '%s'. Баланс: %s, запрошено: %s",
                            source.getName(), source.getBalance(), amount),
                    source.getBalance(),
                    amount
            );
            logger.log(Level.WARNING, "Ошибка списания средств: недостаточно баланса", ex);
            throw ex;
        }

        // 4. Транзакция (атомарное изменение состояния)
        source.withdraw(amount);
        target.deposit(amount);

        logger.log(Level.INFO, "Перевод успешно выполнен. Новый баланс {0}: {1}",
                new Object[]{source.getName(), source.getBalance()});
    }
}
