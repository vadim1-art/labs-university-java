package experiment;

import model.Transaction;
import util.TransactionGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Контрольная задача: 100 000 операций, многократный поиск по id.
 * Что выбрать — List с полным проходом или Map<UUID, Transaction>?
 */
public final class SearchEfficiencyExperiment {

    private static final int TRANSACTIONS = 100_000;
    private static final int SEARCHES = 1_000;

    private SearchEfficiencyExperiment() {
    }

    public static void run() {
        System.out.println("\n=== КОНТРОЛЬНАЯ ЗАДАЧА: поиск по id среди 100 000 операций ===");
        List<Transaction> list = TransactionGenerator.generate(TRANSACTIONS, 7);

        // Какие id будем искать: случайные существующие
        Random random = new Random(1);
        UUID[] idsToFind = new UUID[SEARCHES];
        for (int i = 0; i < SEARCHES; i++) {
            idsToFind[i] = list.get(random.nextInt(TRANSACTIONS)).getId();
        }

        // Вариант 1: List, полный проход — O(n) на каждый поиск
        long start = System.nanoTime();
        int foundInList = 0;
        for (UUID id : idsToFind) {
            if (findInList(list, id) != null) {
                foundInList++;
            }
        }
        long listNanos = System.nanoTime() - start;

        // Вариант 2: Map — один раз строим индекс O(n), дальше каждый поиск O(1)
        start = System.nanoTime();
        Map<UUID, Transaction> index = new HashMap<>();
        for (Transaction transaction : list) {
            index.put(transaction.getId(), transaction);
        }
        long buildNanos = System.nanoTime() - start;

        start = System.nanoTime();
        int foundInMap = 0;
        for (UUID id : idsToFind) {
            if (index.get(id) != null) {
                foundInMap++;
            }
        }
        long mapNanos = System.nanoTime() - start;

        System.out.printf("List, полный проход: %,d поисков за %8.2f мс (найдено %d)%n",
                SEARCHES, listNanos / 1_000_000.0, foundInList);
        System.out.printf("HashMap: построение индекса %8.2f мс, %,d поисков за %8.3f мс (найдено %d)%n",
                buildNanos / 1_000_000.0, SEARCHES, mapNanos / 1_000_000.0, foundInMap);
        System.out.println("Вывод: для многократного поиска выбираем Map<UUID, Transaction> — "
                + "индекс строится один раз за O(n), а каждый поиск стоит O(1) вместо O(n).");
    }

    private static Transaction findInList(List<Transaction> list, UUID id) {
        for (Transaction transaction : list) {
            if (transaction.getId().equals(id)) {
                return transaction;
            }
        }
        return null;
    }
}
