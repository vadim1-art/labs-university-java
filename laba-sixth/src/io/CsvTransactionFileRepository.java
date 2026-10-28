package io;

import exception.InvalidTransactionException;
import model.Category;
import model.Transaction;
import model.TransactionType;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Хранение операций в упрощенном CSV:
 *
 * <pre>
 * id;date;type;amount;category;description
 * 550e8400-e29b-41d4-a716-446655440000;2026-09-01;EXPENSE;750.00;FOOD;Обед
 * </pre>
 *
 * Это НЕ универсальный CSV-парсер: символ ';' и переводы строк внутри значений запрещены,
 * кавычки и экранирование не поддерживаются. Для настоящего CSV нужен полноценный парсер
 * (например, Apache Commons CSV или OpenCSV).
 */
public class CsvTransactionFileRepository implements TransactionFileRepository {

    public static final String HEADER = "id;date;type;amount;category;description";
    private static final String SEPARATOR = ";";
    private static final int COLUMNS = 6;
    // Кодировку указываем явно, а не полагаемся на настройки операционной системы
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    @Override
    public List<Transaction> load(Path path) throws IOException {
        List<Transaction> result = new ArrayList<>();
        // try-with-resources: reader закроется сам — и при обычном выходе, и при исключении
        try (BufferedReader reader = Files.newBufferedReader(path, CHARSET)) {
            String header = reader.readLine();
            if (header == null) {
                return result; // пустой файл — операций нет
            }
            if (header.startsWith("﻿")) {
                header = header.substring(1); // BOM, который добавляют Блокнот и Excel при сохранении в UTF-8
            }
            if (!HEADER.equals(header)) {
                throw new CsvFormatException(path, 1, "неожиданный заголовок: " + header);
            }
            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                result.add(parseLine(path, line, lineNumber));
            }
        }
        return result;
    }

    @Override
    public void save(Path path, List<Transaction> transactions) throws IOException {
        // Сначала превращаем все операции в строки. Если какая-то некорректна,
        // исключение вылетит ДО открытия файла, и старый файл не будет испорчен.
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        for (Transaction transaction : transactions) {
            lines.add(toLine(transaction));
        }

        Path parent = path.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent); // создаст все недостающие каталоги; если они есть — ничего не делает
        }
        try (BufferedWriter writer = Files.newBufferedWriter(path, CHARSET)) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        }
    }

    private Transaction parseLine(Path path, String line, int lineNumber) throws CsvFormatException {
        // limit = -1: не отбрасывать пустые значения в конце строки (пустое описание)
        String[] parts = line.split(SEPARATOR, -1);
        if (parts.length != COLUMNS) {
            throw new CsvFormatException(path, lineNumber,
                    "ожидалось " + COLUMNS + " полей, получено " + parts.length);
        }
        try {
            return new Transaction(
                    UUID.fromString(parts[0]),
                    LocalDate.parse(parts[1]),
                    TransactionType.valueOf(parts[2]),
                    new BigDecimal(parts[3]),
                    new Category(parts[4]),
                    parts[5]);
        } catch (IllegalArgumentException | DateTimeParseException | InvalidTransactionException e) {
            // Не теряем причину: исходное исключение сохраняется как cause
            throw new CsvFormatException(path, lineNumber, e.getMessage(), e);
        }
    }

    private String toLine(Transaction transaction) {
        return String.join(SEPARATOR,
                transaction.getId().toString(),
                transaction.getDate().toString(),
                transaction.getType().name(),
                transaction.getAmount().toPlainString(),
                checkValue(transaction.getCategory().getCode(), "категория"),
                checkValue(transaction.getDescription(), "описание"));
    }

    private String checkValue(String value, String fieldName) {
        if (value.contains(SEPARATOR) || value.contains("\n") || value.contains("\r")) {
            throw new IllegalArgumentException(
                    "Поле \"" + fieldName + "\" содержит ';' или перевод строки — в упрощенном CSV это запрещено: " + value);
        }
        return value;
    }
}
