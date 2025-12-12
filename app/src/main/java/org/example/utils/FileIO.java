package org.example.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Utility class for file input/output operations
 */
public class FileIO {
    /**
     * Load file content as string
     * @param path file path
     * @return file content as string
     * 
     * @throws FileNotFoundException if file not found
     * @throws IOException if file cannot be read
     */
    public static String load(String path) throws FileNotFoundException, IOException {
        var fs = new FileInputStream(path);
        byte[] data = fs.readAllBytes();
        fs.close();

        return new String(data);
    }

    /**
     * Load resource content as string
     * @param name resource name
     * @return resource content as string
     * 
     * @throws FileNotFoundException if resource not found
     * @throws IOException if resource cannot be read
     */
    public static String loadResource(String name) throws FileNotFoundException, IOException {
        var inputStream = FileIO.class.getClassLoader().getResourceAsStream(name);
        if (inputStream == null) {
            throw new FileNotFoundException("Resource not found: " + name);
        }

        byte[] data = inputStream.readAllBytes();
        inputStream.close();

        return new String(data, "UTF-8");
    }

    /**
     * Save string content to file
     * @param path file path
     * @param content string content
     * @return file path
     * 
     * @throws FileNotFoundException if file cannot be created
     * @throws IOException if file cannot be written
     */
    public static String save(String path, String content) throws FileNotFoundException, IOException {
        var fs = new FileOutputStream(path);
        fs.write(content.getBytes());
        fs.close();

        return path;
    }
}
