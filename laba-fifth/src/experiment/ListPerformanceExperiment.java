package experiment;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.function.Supplier;

/**
 * Эксперимент ArrayList vs LinkedList на четырех сценариях.
 *
 * Важно: это не профессиональный бенчмарк (для этого есть JMH). На время влияют JIT-компиляция,
 * прогрев JVM, сборщик мусора. Поэтому каждый сценарий сначала прогревается, затем
 * берется лучшее из нескольких замеров. Главное — сравнить порядок величин с теорией (O-нотацией).
 */
public final class ListPerformanceExperiment {

    private static final int SIZE = 100_000;          // размер списка для сценариев 1–3
    private static final int RANDOM_GETS = 2_000;     // сколько раз вызываем get(i) по случайному индексу
    private static final int INSERT_SIZE = 20_000;    // размер списка для вставки через ListIterator
    private static final int WARMUP_ROUNDS = 2;
    private static final int MEASURE_ROUNDS = 3;

    // Результаты складываем сюда, чтобы JIT не выбросил "бесполезный" код как мертвый
    private static long blackhole;

    private ListPerformanceExperiment() {
    }

    public static void run() {
        System.out.println("\n=== ЭКСПЕРИМЕНТ: ArrayList vs LinkedList ===");
        System.out.printf("Размер списка: %,d; случайных get(i): %,d; вставка через ListIterator в список из %,d%n",
                SIZE, RANDOM_GETS, INSERT_SIZE);
        System.out.printf("%-40s %12s %12s   %s%n", "Сценарий", "ArrayList", "LinkedList", "Теория (AL / LL)");

        List<Integer> arrayList = fill(new ArrayList<>(), SIZE);
        List<Integer> linkedList = fill(new LinkedList<>(), SIZE);
        printRow("1. Последовательный проход (for-each)",
                measure(() -> iterate(arrayList)),
                measure(() -> iterate(linkedList)),
                "O(n) / O(n)");

        int[] indexes = randomIndexes(RANDOM_GETS, SIZE);
        printRow("2. get(i) по случайным индексам",
                measure(() -> randomGet(arrayList, indexes)),
                measure(() -> randomGet(linkedList, indexes)),
                "O(1) / O(n) на вызов");

        printRow("3. Добавление в конец add(e)",
                measure(() -> addToEnd(ArrayList::new)),
                measure(() -> addToEnd(LinkedList::new)),
                "O(1)* / O(1)");

        printRow("4. Вставка через ListIterator.add()",
                measure(() -> insertWithIterator(ArrayList::new)),
                measure(() -> insertWithIterator(LinkedList::new)),
                "O(n) / O(1) на вставку");

        System.out.println("* амортизированно: изредка ArrayList копирует массив в новый, больший");
    }

    private static void iterate(List<Integer> list) {
        long sum = 0;
        for (int value : list) {
            sum += value;
        }
        blackhole += sum;
    }

    private static void randomGet(List<Integer> list, int[] indexes) {
        long sum = 0;
        for (int index : indexes) {
            // ArrayList: вычисляет адрес в массиве сразу. LinkedList: идет по узлам от ближайшего конца.
            sum += list.get(index);
        }
        blackhole += sum;
    }

    private static void addToEnd(Supplier<List<Integer>> listFactory) {
        List<Integer> list = listFactory.get();
        for (int i = 0; i < SIZE; i++) {
            list.add(i);
        }
        blackhole += list.size();
    }

    /** После каждого элемента вставляем новый: список удваивается. */
    private static void insertWithIterator(Supplier<List<Integer>> listFactory) {
        List<Integer> list = fill(listFactory.get(), INSERT_SIZE);
        ListIterator<Integer> iterator = list.listIterator();
        while (iterator.hasNext()) {
            iterator.next();
            // ArrayList сдвигает весь "хвост" массива вправо, LinkedList только перевешивает две ссылки
            iterator.add(-1);
        }
        blackhole += list.size();
    }

    private static List<Integer> fill(List<Integer> list, int size) {
        for (int i = 0; i < size; i++) {
            list.add(i);
        }
        return list;
    }

    private static int[] randomIndexes(int count, int bound) {
        Random random = new Random(42);
        int[] indexes = new int[count];
        for (int i = 0; i < count; i++) {
            indexes[i] = random.nextInt(bound);
        }
        return indexes;
    }

    /** Прогрев + лучшее из нескольких замеров, в наносекундах. */
    private static long measure(Runnable scenario) {
        for (int i = 0; i < WARMUP_ROUNDS; i++) {
            scenario.run();
        }
        long best = Long.MAX_VALUE;
        for (int i = 0; i < MEASURE_ROUNDS; i++) {
            long start = System.nanoTime();
            scenario.run();
            best = Math.min(best, System.nanoTime() - start);
        }
        return best;
    }

    private static void printRow(String name, long arrayNanos, long linkedNanos, String theory) {
        System.out.printf("%-40s %9.2f мс %9.2f мс   %s%n",
                name, arrayNanos / 1_000_000.0, linkedNanos / 1_000_000.0, theory);
    }
}
