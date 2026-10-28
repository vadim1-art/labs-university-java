import check.SelfCheck;
import experiment.IoExperiments;
import io.CsvFormatException;
import io.CsvTransactionFileRepository;
import io.TransactionFileRepository;
import model.Category;
import model.Transaction;
import model.TransactionComparators;
import model.TransactionType;
import repository.InMemoryRepository;
import service.TransactionService;
import util.SampleData;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Finance Tracker, который сохраняет операции в файл.
 *
 * Main играет роль UI-слоя: репозиторий только бросает IOException,
 * а здесь мы решаем, что записать в лог и что показать пользователю.
 */
public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        Path dataDir = Path.of("data");
        Path csv = dataDir.resolve("transactions.csv");
        TransactionFileRepository fileRepository = new CsvTransactionFileRepository();
        TransactionService service = new TransactionService(new InMemoryRepository<>());

        System.out.println("=== FINANCE TRACKER ===");
        System.out.println("Файл данных: " + csv.toAbsolutePath());

        // 1. Загрузка. Разные причины ошибки обрабатываем по-разному.
        List<Transaction> loaded;
        try {
            loaded = fileRepository.load(csv);
            System.out.println("Загружено операций из файла: " + loaded.size());
        } catch (NoSuchFileException e) {
            // Первый запуск: файла еще нет — это нормальная ситуация
            logger.log(Level.INFO, "Файл данных не найден: {0}", e.getFile());
            System.out.println("Файла еще нет — первый запуск, создаем начальные операции");
            loaded = SampleData.transactions();
        } catch (CsvFormatException e) {
            logger.log(Level.SEVERE, "Файл данных поврежден", e);
            System.out.println("Файл данных поврежден (строка " + e.getLineNumber() + "): " + e.getMessage());
            System.out.println("Исправьте или удалите файл. Перезаписывать его программа не будет.");
            return;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Не удалось прочитать файл данных", e);
            System.out.println("Не удалось прочитать файл данных: " + e.getMessage());
            return;
        }
        for (Transaction transaction : loaded) {
            service.add(transaction);
        }

        // 2. Каждый запуск добавляет одну операцию — после перезапуска видно, что прошлые не пропали
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        service.add(new Transaction(LocalDate.now(), TransactionType.EXPENSE,
                new BigDecimal("180.00"), new Category("CAFE"), "Кофе, запуск в " + time));

        System.out.println("\nОперации (по дате):");
        for (Transaction transaction : service.getSorted(TransactionComparators.BY_DATE)) {
            System.out.println("  " + transaction);
        }
        System.out.println("Расходы по датам:");
        for (Map.Entry<LocalDate, BigDecimal> entry : service.sumByDate(TransactionType.EXPENSE).entrySet()) {
            System.out.println("  " + entry.getKey() + " -> " + entry.getValue());
        }

        // 3. Сохранение
        try {
            fileRepository.save(csv, service.getAll());
            System.out.printf("%nСохранено операций: %d, размер файла: %d байт%n",
                    service.getAll().size(), Files.size(csv));
            System.out.println("Запустите программу еще раз — операции загрузятся из файла, и их станет на одну больше.");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Не удалось сохранить данные", e);
            System.out.println("Не удалось сохранить данные: " + e.getMessage());
        }

        // 4. Эксперименты с потоками, кодировками, File и Path
        try {
            IoExperiments.run(dataDir.resolve("experiments"));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Эксперимент ввода-вывода завершился ошибкой", e);
            System.out.println("Эксперимент прерван: " + e.getMessage());
        }

        // 5. Автопроверка
        new SelfCheck().runAll();
    }
}
