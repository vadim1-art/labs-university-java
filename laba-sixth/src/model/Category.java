package model;

import java.util.Locale;
import java.util.Objects;

/**
 * Категория операции: FOOD, CAFE, SALARY ...
 *
 * Две категории равны, если у них одинаковый код. На контракте equals/hashCode
 * держится уникальность категорий в HashSet / LinkedHashSet.
 *
 * В отличие от Transaction, у категории есть естественный порядок — по алфавиту,
 * поэтому она реализует Comparable. compareTo согласован с equals:
 * compareTo == 0 тогда и только тогда, когда equals == true.
 */
public final class Category implements Comparable<Category> {
    private final String code;

    public Category(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Код категории не может быть пустым");
        }
        // Приводим к одному виду, чтобы "food" и " FOOD " считались одной категорией
        this.code = code.trim().toUpperCase(Locale.ROOT);
    }

    public String getCode() { return code; }

    @Override
    public int compareTo(Category other) {
        return code.compareTo(other.code);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return code.equals(category.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return code;
    }
}
