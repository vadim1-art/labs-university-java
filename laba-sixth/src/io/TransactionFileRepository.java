package io;

import model.Transaction;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Хранение операций в файле.
 *
 * Методы бросают IOException наружу, а не печатают "Ошибка!": решать, что показать
 * пользователю и как записать проблему в лог, должен вызывающий (UI) слой.
 */
public interface TransactionFileRepository {

    /**
     * @throws java.nio.file.NoSuchFileException если файла нет (предсказуемый, конкретный тип ошибки)
     * @throws CsvFormatException если в файле испорченная строка
     */
    List<Transaction> load(Path path) throws IOException;

    /** Перезаписывает файл. Недостающие каталоги создаются автоматически. */
    void save(Path path, List<Transaction> transactions) throws IOException;
}
