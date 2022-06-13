package com.easypost.easyvcr.internalutilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class Files {
    /**
     * Creates a file if it doesn't exist.
     *
     * @param filePath The path to the file.
     * @throws IOException If the file cannot be created.
     */
    public static void createFileIfNotExists(String filePath) throws IOException {
        try {
            File file = new File(filePath);
            File parentFolder = file.toPath().getParent().toFile();
            if (!parentFolder.exists()) {
                parentFolder.mkdirs();
            }
            file.createNewFile(); // if file already exists will do nothing
        } catch (Exception ignored) {
            throw new IOException("Could not create file");
        }
    }

    /**
     * Reads a file.
     *
     * @param file The file to read.
     * @return The contents of the file.
     */
    public static String readFile(File file) {
        List<String> data = new ArrayList<>();
        try {
            data = java.nio.file.Files.readAllLines(file.toPath());
        } catch (IOException ignored) {
            return null;
        }
        if (data.isEmpty()) {
            return null;
        }
        StringBuilder contents = new StringBuilder();
        for (String line : data) {
            contents.append(line);
        }
        return contents.toString();
    }

    /**
     * Reads a file.
     *
     * @param filePath The path to the file.
     * @return The contents of the file.
     */
    public static String readFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return null;  // file doesn't exist
        }
        return readFile(file);
    }

    /**
     * Writes a file.
     *
     * @param filePath The path to the file.
     * @param string   The contents to write.
     */
    public static void writeFile(String filePath, String string) throws IOException {
        createFileIfNotExists(filePath);
        FileWriter myWriter = new FileWriter(filePath);
        myWriter.write(string);
        myWriter.close();
    }
}
