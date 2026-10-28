package util;

import model.Transaction;

import java.math.BigDecimal;
import java.util.Collection;

/**
 * Generic-методы для демонстрации PECS: Producer Extends, Consumer Super.
 */
public final class CollectionUtils {

    private CollectionUtils() {
    }

    /**
     * Копирует все элементы из source в target.
     *
     * source — ПРОИЗВОДИТЕЛЬ: мы из него только читаем элементы T -> {@code ? extends T}.
     * target — ПОТРЕБИТЕЛЬ: мы в него только кладем элементы T -> {@code ? super T}.
     *
     * Благодаря этому можно скопировать List<Transaction> в List<Object>.
     * С сигнатурой copyAll(Collection<T>, Collection<T>) такой вызов не скомпилировался бы.
     */
    public static <T> void copyAll(Collection<? extends T> source, Collection<? super T> target) {
        for (T item : source) {
            target.add(item);
        }
    }

    /**
     * Сумма любой коллекции операций: List, Set, Collection подтипов Transaction.
     * Коллекция здесь только производитель (читаем), поэтому {@code ? extends}.
     */
    public static BigDecimal totalAmount(Collection<? extends Transaction> transactions) {
        BigDecimal total = BigDecimal.ZERO;
        for (Transaction transaction : transactions) {
            total = total.add(transaction.getAmount());
        }
        return total;
    }
}
