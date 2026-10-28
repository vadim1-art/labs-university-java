package check;

import io.CsvFormatException;
import io.CsvTransactionFileRepository;
import io.TransactionFileRepository;
import model.Category;
import model.Transaction;
import model.TransactionType;
import util.SampleData;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

/**
 * Автопроверка лабораторной 6 без внешних библиотек.
 * Все файлы создаются во временном каталоге и удаляются в конце.
 */
public class SelfCheck {

    private final TransactionFileRepository repository = new CsvTransactionFileRepository();
    private int passed;
    private int failed;

    public static void main(String[] args) {
        if (!new SelfCheck().runAll()) {
            System.exit(1);
        }
    }

    public boolean runAll() {
        System.out.println("\n=== АВТОПРОВЕРКА ===");
        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("laba6-check");
            checkRoundTrip(tempDir);
            checkRussianText(tempDir);
            checkResourcesClosed(tempDir);
            checkMissingFile(tempDir);
            checkDirectoryCreated(tempDir);
            checkForbiddenSeparator(tempDir);
            checkBrokenLine(tempDir);
            checkEmptyFiles(tempDir);
        } catch (IOException e) {
            check("Неожиданная ошибка ввода-вывода: " + e, false);
        } finally {
            if (tempDir != null) {
                deleteRecursively(tempDir);
            }
        }
        System.out.printf("Итого: пройдено %d из %d проверок%n", passed, passed + failed);
        return failed == 0;
    }

    private void checkRoundTrip(Path dir) throws IOException {
        List<Transaction> original = SampleData.transactions();
        original.add(new Transaction(LocalDate.of(2026, 9, 4), TransactionType.EXPENSE,
                new BigDecimal("10"), new Category("FOOD"), "")); // пустое описание — крайний случай

        Path file = dir.resolve("round-trip.csv");
        repository.save(file, original);
        List<Transaction> loaded = repository.load(file);

        boolean allFieldsEqual = loaded.size() == original.size();
        for (int i = 0; allFieldsEqual && i < original.size(); i++) {
            allFieldsEqual = sameFields(original.get(i), loaded.get(i));
        }
        check("Round-trip: объекты -> CSV -> объекты, все поля совпадают (" + original.size() + " шт.)", allFieldsEqual);
    }

    private void checkRussianText(Path dir) throws IOException {
        Path file = dir.resolve("russian.csv");
        repository.save(file, SampleData.transactions());
        String content = Files.readString(file, StandardCharsets.UTF_8);
        List<Transaction> loaded = repository.load(file);
        check("Русские символы записаны в файл в UTF-8", content.contains("Обед") && content.contains("Кофе с собой"));
        check("Русские символы восстановлены после чтения", loaded.get(5).getDescription().equals("Кофе с собой"));
    }

    /**
     * В Windows открытый файл нельзя удалить. Если delete прошел — значит, поток был закрыт.
     * Особенно важно проверить случай, когда чтение прервалось исключением.
     */
    private void checkResourcesClosed(Path dir) throws IOException {
        Path file = dir.resolve("closed.csv");
        repository.save(file, SampleData.transactions());
        repository.load(file);
        check("Ресурс закрывается: после save/load файл можно удалить", tryDelete(file));

        Path broken = dir.resolve("closed-broken.csv");
        Files.writeString(broken, CsvTransactionFileRepository.HEADER + "\nэто;не;операция\n", StandardCharsets.UTF_8);
        try {
            repository.load(broken);
        } catch (CsvFormatException expected) {
            // ожидаемо: нас интересует только то, что файл после этого закрыт
        }
        check("Ресурс закрывается даже после исключения при чтении", tryDelete(broken));
    }

    private void checkMissingFile(Path dir) throws IOException {
        boolean predictable;
        try {
            repository.load(dir.resolve("нет-такого-файла.csv"));
            predictable = false;
        } catch (NoSuchFileException e) {
            predictable = true;
        }
        check("Несуществующий файл -> NoSuchFileException (конкретная, предсказуемая ошибка)", predictable);
    }

    private void checkDirectoryCreated(Path dir) throws IOException {
        Path nestedDir = dir.resolve("a").resolve("b").resolve("c");
        Path file = nestedDir.resolve("transactions.csv");
        repository.save(file, SampleData.transactions());
        check("Каталог a/b/c создается при необходимости", Files.isDirectory(nestedDir) && Files.isRegularFile(file));
    }

    private void checkForbiddenSeparator(Path dir) throws IOException {
        Path file = dir.resolve("separator.csv");
        repository.save(file, SampleData.transactions());
        String before = Files.readString(file, StandardCharsets.UTF_8);

        List<Transaction> withSemicolon = SampleData.transactions();
        withSemicolon.add(new Transaction(LocalDate.of(2026, 9, 5), TransactionType.EXPENSE,
                new BigDecimal("200"), new Category("CAFE"), "Кофе; с молоком"));
        boolean rejected;
        try {
            repository.save(file, withSemicolon);
            rejected = false;
        } catch (IllegalArgumentException e) {
            rejected = true;
        }
        check("Символ ';' в описании запрещен при сохранении", rejected);
        check("При такой ошибке старый файл не испорчен", before.equals(Files.readString(file, StandardCharsets.UTF_8)));
    }

    private void checkBrokenLine(Path dir) throws IOException {
        Path file = dir.resolve("broken.csv");
        Files.writeString(file, CsvTransactionFileRepository.HEADER + "\n"
                + "550e8400-e29b-41d4-a716-446655440000;2026-09-01;EXPENSE;750.00;FOOD;Обед\n"
                + "550e8400-e29b-41d4-a716-446655440001;2026-13-45;EXPENSE;750.00;FOOD;Плохая дата\n",
                StandardCharsets.UTF_8);
        int lineNumber = -1;
        try {
            repository.load(file);
        } catch (CsvFormatException e) {
            lineNumber = e.getLineNumber();
        }
        check("Испорченная строка -> CsvFormatException с номером строки 3", lineNumber == 3);
    }

    private void checkEmptyFiles(Path dir) throws IOException {
        Path empty = dir.resolve("empty.csv");
        Files.writeString(empty, "", StandardCharsets.UTF_8);
        Path headerOnly = dir.resolve("header-only.csv");
        Files.writeString(headerOnly, CsvTransactionFileRepository.HEADER + "\n", StandardCharsets.UTF_8);
        check("Пустой файл и файл только с заголовком -> пустой список",
                repository.load(empty).isEmpty() && repository.load(headerOnly).isEmpty());
    }

    private static boolean sameFields(Transaction a, Transaction b) {
        return a.getId().equals(b.getId())
                && a.getDate().equals(b.getDate())
                && a.getType() == b.getType()
                && a.getAmount().compareTo(b.getAmount()) == 0
                && a.getCategory().equals(b.getCategory())
                && a.getDescription().equals(b.getDescription());
    }

    private static boolean tryDelete(Path file) {
        try {
            Files.delete(file);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static void deleteRecursively(Path path) {
        try {
            if (Files.isDirectory(path)) {
                try (DirectoryStream<Path> children = Files.newDirectoryStream(path)) {
                    for (Path child : children) {
                        deleteRecursively(child);
                    }
                }
            }
            Files.deleteIfExists(path);
        } catch (IOException e) {
            System.out.println("Не удалось удалить временный файл " + path + ": " + e.getMessage());
        }
    }

    private void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("[OK]   " + name);
        } else {
            failed++;
            System.out.println("[FAIL] " + name);
        }
    }
}
