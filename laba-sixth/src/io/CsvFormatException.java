package io;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Файл прочитан, но его содержимое не соответствует формату CSV.
 * Наследуется от IOException, потому что для вызывающего кода это тоже "не удалось загрузить файл",
 * но хранит номер строки — чтобы сообщить, где именно проблема.
 */
public class CsvFormatException extends IOException {

    private final int lineNumber;

    public CsvFormatException(Path path, int lineNumber, String message) {
        this(path, lineNumber, message, null);
    }

    public CsvFormatException(Path path, int lineNumber, String message, Throwable cause) {
        super(String.format("%s, строка %d: %s", path, lineNumber, message), cause);
        this.lineNumber = lineNumber;
    }

    public int getLineNumber() {
        return lineNumber;
    }
}
