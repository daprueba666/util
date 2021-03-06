package de.unitrier.st.util;

import org.apache.commons.io.FileUtils;
import org.hibernate.StatelessSession;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Collectors;

public class Util {
    private static Path defaultLogDir = Paths.get(System.getProperty("user.dir"), "logs");
    private static final double EPSILON = 0.00001;  // for comparison of doubles

    public static void insertList(StatelessSession session, List list) {
        for (Object element : list) {
            session.insert(element);
        }
    }

    public static void updateList(StatelessSession session, List list) {
        for (Object element : list) {
            session.update(element);
        }
    }

    public static Logger getClassLogger(Class c) throws IOException {
        return getClassLogger(c, true, defaultLogDir);
    }

    public static Logger getClassLogger(Class c, Path logFileDir) throws IOException {
        return getClassLogger(c, true, logFileDir);
    }

    public static Logger getClassLogger(Class c, boolean consoleOutput) throws IOException {
        return getClassLogger(c, consoleOutput, defaultLogDir);
    }

    public static Logger getClassLogger(Class c, boolean consoleOutput, Path logFileDir) throws IOException {
        // ensure that log directory exists
        try {
            if (!Files.exists(logFileDir)) {
                Files.createDirectory(logFileDir);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String logFile = Paths.get(logFileDir.toString(), c.getSimpleName() + ".log").toString();

        // configure logger
        Logger logger = Logger.getLogger(c.getName());
        if (!consoleOutput) {
            logger.setUseParentHandlers(false); // disable handlers inherited from root logger
        }
        Handler fileHandler = new FileHandler(logFile);
        fileHandler.setFormatter(new SimpleFormatter());
        logger.addHandler(fileHandler);

        return logger;
    }

    public static <T> List<T> processFiles(Path dir, Predicate<Path> filter, Function<Path, T> map) {
        // ensure that input directory exists
        if (!Files.exists(dir) || !Files.isDirectory(dir)) {
            throw new IllegalArgumentException("Directory not found: " + dir);
        }

        try {
            return Files.list(dir)
                    .filter(filter)
                    .map(map)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public static String replaceStringAt(String str, int beginIndex, int endIndex, String replacement) {
        // beginIndex inclusive, endIndex exclusive
        return str.substring(0, beginIndex) + replacement + str.substring(endIndex, str.length());
    }

    public static void ensureFileExists(Path file) {
        // ensure that file exists
        if (!Files.exists(file) || Files.isDirectory(file)) {
            throw new IllegalArgumentException("File not found: " + file);
        }
    }

    public static void ensureDirectoryExists(Path dir) {
        if (!Files.exists(dir) || !Files.isDirectory(dir)) {
            throw new IllegalArgumentException("Directory does not exist: " + dir);
        }
    }

    public static void ensureEmptyDirectoryExists(Path dir) throws IOException {
        // ensure that output dir exists, but is empty
        if (Files.exists(dir)) {
            if (Files.isDirectory(dir)) {
                FileUtils.deleteDirectory(dir.toFile());
            } else {
                throw new IllegalArgumentException("Not a directory: " + dir);
            }
        }
        Files.createDirectories(dir);
    }

    public static void deleteFileIfExists(Path file) throws IOException {
        if (Files.exists(file)) {
            if (Files.isDirectory(file)) {
                throw new IllegalArgumentException("File is a directory: " + file);
            }

            Files.delete(file);
        }
    }

    public static void createDirectory(Path dir) throws IOException {
        Files.createDirectories(dir);
    }

    public static boolean equals(double value1, double value2) {
        // see http://www.cygnus-software.com/papers/comparingfloats/Comparing%20floating%20point%20numbers.htm
        return equals(value1, value2, EPSILON);
    }

    private static boolean equals(double value1, double value2, double epsilon) {
        return Math.abs(value1 - value2) < epsilon;
    }

    public static boolean lessThan(double value1, double value2) {
        return lessThan(value1, value2, EPSILON);
    }

    private static boolean lessThan(double value1, double value2, double epsilon) {
        return (value2 - value1) - epsilon > 0;
    }

    public static boolean greaterThan(double value1, double value2) {
        return greaterThan(value1, value2, EPSILON);
    }

    private static boolean greaterThan(double value1, double value2, double epsilon) {
        return (value2 - value1) + epsilon < 0;
    }

    public static void redirectConsoleMessagesToFile(File outputFile) throws FileNotFoundException {
        PrintStream out = new PrintStream(new FileOutputStream(outputFile));
        System.setOut(out);
        System.setErr(out);
    }

    public static String exceptionStackTraceToString(Exception e) {
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }

}
