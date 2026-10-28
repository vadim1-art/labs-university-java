package experiment;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Эксперименты лабораторной: Path/Files, классический File, байты vs символы, кодировки.
 * Все файлы создаются в переданном рабочем каталоге.
 */
public final class IoExperiments {

    private static final Charset WINDOWS_1251 = Charset.forName("windows-1251");

    private IoExperiments() {
    }

    public static void run(Path workDir) throws IOException {
        pathAndFiles(workDir);
        classicFileVsPath(workDir);
        bytesVsChars(workDir);
        encoding(workDir);
    }

    /** Основные операции современного API: существование, каталог, запись, чтение, копирование, размер. */
    private static void pathAndFiles(Path workDir) throws IOException {
        System.out.println("\n=== ЭКСПЕРИМЕНТ: Path и Files ===");
        Path dir = workDir.resolve("path-demo");
        Path file = dir.resolve("note.txt");

        System.out.println("Каталог существует до создания? " + Files.exists(dir));
        Files.createDirectories(dir);
        System.out.println("После Files.createDirectories: " + Files.isDirectory(dir));

        Files.writeString(file, "Заметка: купить кофе", StandardCharsets.UTF_8);
        System.out.println("Записали файл " + file.getFileName() + ", существует: " + Files.exists(file));
        System.out.println("Прочитали: " + Files.readString(file, StandardCharsets.UTF_8));

        Path copy = dir.resolve("note-copy.txt");
        Files.copy(file, copy, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Скопировали в " + copy.getFileName() + ", файлы одинаковые: " + (Files.mismatch(file, copy) == -1));

        System.out.println("Размер: " + Files.size(file) + " байт (20 символов, но кириллица в UTF-8 — по 2 байта)");
        System.out.println("Полный путь: " + file.toAbsolutePath());
    }

    /**
     * Небольшой сравнительный фрагмент классического I/O.
     * java.io.File сообщает об ошибке через boolean или 0, Files — через исключение с причиной.
     */
    private static void classicFileVsPath(Path workDir) throws IOException {
        System.out.println("\n=== ЭКСПЕРИМЕНТ: java.io.File против Path/Files ===");
        File legacyDir = new File(workDir.toFile(), "legacy");
        boolean created = legacyDir.mkdirs(); // false, если не создан — но почему, неизвестно
        System.out.println("File.mkdirs() вернул " + created + " (повторный запуск -> false, каталог уже есть)");

        File legacyFile = new File(legacyDir, "legacy.txt");
        // Классическая цепочка: байтовый поток -> мост с кодировкой -> буфер
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(legacyFile), StandardCharsets.UTF_8))) {
            writer.write("Записано через FileOutputStream + OutputStreamWriter");
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(legacyFile), StandardCharsets.UTF_8))) {
            System.out.println("Классическое чтение: " + reader.readLine());
        }

        File missing = new File(legacyDir, "нет-такого-файла.txt");
        System.out.println("File.length() отсутствующего файла: " + missing.length() + " — ошибка не видна");
        try {
            Files.size(missing.toPath());
        } catch (NoSuchFileException e) {
            System.out.println("Files.size() того же файла: " + e.getClass().getSimpleName() + " — понятно, что случилось");
        }
        System.out.println("Переход между API: file.toPath() и path.toFile()");
    }

    /**
     * Текстовый файл копируем символьными потоками (Reader/Writer),
     * бинарный PNG — байтовыми (InputStream/OutputStream). Затем показываем,
     * что будет, если PNG скопировать как текст.
     */
    private static void bytesVsChars(Path workDir) throws IOException {
        System.out.println("\n=== ЭКСПЕРИМЕНТ: байтовые и символьные потоки ===");
        Path dir = Files.createDirectories(workDir.resolve("streams"));

        Path text = dir.resolve("text.txt");
        Files.writeString(text, "Доход;Расход;Кофе;Счет\nПеревод на карту\n", StandardCharsets.UTF_8);
        Path textCopy = dir.resolve("text-copy.txt");
        copyAsText(text, textCopy);
        System.out.println("Текст через Reader/Writer: копия совпадает с оригиналом — " + (Files.mismatch(text, textCopy) == -1));

        Path png = dir.resolve("image.png");
        createPng(png);
        Path pngCopy = dir.resolve("image-copy.png");
        copyAsBytes(png, pngCopy);
        System.out.println("PNG через InputStream/OutputStream: копия совпадает — " + (Files.mismatch(png, pngCopy) == -1));

        Path brokenPng = dir.resolve("image-broken.png");
        try {
            copyAsText(png, brokenPng);
        } catch (MalformedInputException e) {
            // Files.newBufferedReader строгий: на некорректных байтах сразу бросает исключение
            System.out.println("PNG через Files.newBufferedReader: " + e.getClass().getSimpleName()
                    + " — байты PNG не являются корректным текстом UTF-8");
        }

        // Классический InputStreamReader не бросает исключение, а молча заменяет "плохие" байты
        try (Reader reader = new InputStreamReader(Files.newInputStream(png), StandardCharsets.UTF_8);
             Writer writer = new OutputStreamWriter(Files.newOutputStream(brokenPng), StandardCharsets.UTF_8)) {
            copyChars(reader, writer);
        }
        System.out.printf("PNG через InputStreamReader/OutputStreamWriter: совпадает — %s; размер %d -> %d байт%n",
                Files.mismatch(png, brokenPng) == -1, Files.size(png), Files.size(brokenPng));
        System.out.println("  первые байты оригинала: " + firstBytesHex(png, 6));
        System.out.println("  первые байты 'копии':   " + firstBytesHex(brokenPng, 6));
        System.out.println("  ImageIO открывает оригинал? " + isReadableImage(png) + "; 'копию'? " + isReadableImage(brokenPng));
        System.out.println("  Причина: байт 0x89 и другие не образуют корректных символов UTF-8,");
        System.out.println("  декодер заменяет их на U+FFFD (в UTF-8 это EF BF BD) — исходные байты потеряны.");
    }

    /** Эксперимент с кодировкой: пишем в UTF-8, читаем другой кодировкой, затем исправляем. */
    private static void encoding(Path workDir) throws IOException {
        System.out.println("\n=== ЭКСПЕРИМЕНТ: кодировки ===");
        Path dir = Files.createDirectories(workDir.resolve("encoding"));
        String original = "Доход;Расход;Кофе;Счет";

        Path utf8File = dir.resolve("utf8.txt");
        Files.writeString(utf8File, original, StandardCharsets.UTF_8);
        byte[] bytes = Files.readAllBytes(utf8File);
        System.out.println("Записали в UTF-8. Символов в строке: " + original.length() + ", байт в файле: " + bytes.length);

        // new String(bytes, charset) — это и есть декодирование "байты -> символы".
        // Если charset не тот, которым файл записан, каждая пара байт кириллицы превращается в 2 чужих символа.
        System.out.println("Прочитали как windows-1251: " + new String(bytes, WINDOWS_1251));
        System.out.println("Прочитали как ISO-8859-1:   " + new String(bytes, StandardCharsets.ISO_8859_1));

        Path cp1251File = dir.resolve("cp1251.txt");
        Files.writeString(cp1251File, original, WINDOWS_1251);
        System.out.println("Обратная ошибка — файл в windows-1251, читаем как UTF-8: "
                + new String(Files.readAllBytes(cp1251File), StandardCharsets.UTF_8));

        // Исправление: кодировка указывается явно и совпадает с той, которой файл записан
        System.out.println("Исправлено (UTF-8 указан явно): " + Files.readString(utf8File, StandardCharsets.UTF_8));
        System.out.println("Кодировка по умолчанию в этой JVM: " + Charset.defaultCharset()
                + " — на другой машине она может быть другой, поэтому полагаться на нее нельзя");
    }

    private static void copyAsText(Path source, Path target) throws IOException {
        try (Reader reader = Files.newBufferedReader(source, StandardCharsets.UTF_8);
             Writer writer = Files.newBufferedWriter(target, StandardCharsets.UTF_8)) {
            copyChars(reader, writer);
        }
    }

    private static void copyChars(Reader reader, Writer writer) throws IOException {
        char[] buffer = new char[1024];
        int read;
        while ((read = reader.read(buffer)) != -1) {
            writer.write(buffer, 0, read);
        }
    }

    private static void copyAsBytes(Path source, Path target) throws IOException {
        try (InputStream in = new BufferedInputStream(Files.newInputStream(source));
             OutputStream out = new BufferedOutputStream(Files.newOutputStream(target))) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }
    }

    /** Рисуем маленький градиент 64x64 и сохраняем его как настоящий PNG. */
    private static void createPng(Path target) throws IOException {
        BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < 64; y++) {
                image.setRGB(x, y, new Color(x * 4, y * 4, 128).getRGB());
            }
        }
        ImageIO.write(image, "png", target.toFile());
    }

    private static boolean isReadableImage(Path file) {
        try {
            return ImageIO.read(file.toFile()) != null; // null — ни один декодер не узнал формат
        } catch (IOException e) {
            return false;
        }
    }

    private static String firstBytesHex(Path file, int count) throws IOException {
        byte[] bytes = Files.readAllBytes(file);
        StringBuilder hex = new StringBuilder();
        for (int i = 0; i < Math.min(count, bytes.length); i++) {
            hex.append(String.format("%02X ", bytes[i]));
        }
        return hex.toString().trim();
    }
}
