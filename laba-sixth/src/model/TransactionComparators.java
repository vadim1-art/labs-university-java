package model;

import java.util.Comparator;

/**
 * Альтернативные порядки сортировки транзакций.
 * Естественного порядка (Comparable) у Transaction нет, поэтому каждый порядок —
 * отдельный Comparator, который выбирают под конкретную задачу.
 */
public final class TransactionComparators {

    private TransactionComparators() {
    }

    /** По дате: от старых к новым. */
    public static final Comparator<Transaction> BY_DATE =
            Comparator.comparing(Transaction::getDate);

    /** По сумме: от меньшей к большей. */
    public static final Comparator<Transaction> BY_AMOUNT =
            Comparator.comparing(Transaction::getAmount);

    /** Сначала по категории (по алфавиту), внутри одной категории — по дате. */
    public static final Comparator<Transaction> BY_CATEGORY_THEN_DATE =
            Comparator.comparing(Transaction::getCategory)
                    .thenComparing(Transaction::getDate);
}
