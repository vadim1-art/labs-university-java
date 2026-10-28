package experiment;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Короткий эксперимент на стирание типов (type erasure).
 */
public final class TypeErasureDemo {

    private TypeErasureDemo() {
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void run() {
        System.out.println("\n=== ЭКСПЕРИМЕНТ: стирание типов (type erasure) ===");

        List<String> strings = new ArrayList<>();
        List<Integer> integers = new ArrayList<>();

        // true: после компиляции <String> и <Integer> стираются, в runtime оба — просто ArrayList
        System.out.println("strings.getClass() == integers.getClass(): " + (strings.getClass() == integers.getClass()));
        System.out.println("Класс в runtime: " + strings.getClass().getName() + " (без параметра типа)");

        // Параметр типа проверяется только компилятором:
        // strings.add(42);  // <- не скомпилируется: compile-time type safety

        // Raw type отключает эту проверку: компилятор только предупреждает
        List raw = strings;
        raw.add(42); // в List<String> попало число — "загрязнение кучи" (heap pollution)
        try {
            String first = strings.get(0); // компилятор вставил сюда скрытый каст (String)
            System.out.println("Прочитали: " + first);
        } catch (ClassCastException e) {
            System.out.println("Raw type: число попало в List<String>, ошибка вылезла только при чтении -> "
                    + e.getClass().getSimpleName());
        }

        // new T() написать нельзя: в runtime T неизвестен (стерт), компилятор не знает, какой конструктор вызвать.
        // Обходной путь — передать "фабрику" объекта явно.
        List<String> created = create(ArrayList::new);
        System.out.println("Вместо new T() — фабрика Supplier<T>: создан " + created.getClass().getSimpleName());
    }

    private static <T> T create(Supplier<T> factory) {
        return factory.get();
    }
}
