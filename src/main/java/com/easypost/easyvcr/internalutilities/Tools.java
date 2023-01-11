package com.easypost.easyvcr.internalutilities;

import com.easypost.easyvcr.AdvancedSettings;
import com.easypost.easyvcr.requestelements.HttpInteraction;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Internal tools for EasyVCR.
 */
public abstract class Tools {
    /**
     * Get a File object from a path.
     *
     * @param filePath The path to the file.
     * @return The File object.
     */
    public static File getFile(String filePath) {
        if (filePath == null) {
            return null;
        }
        return Paths.get(filePath).toFile();
    }

    /**
     * Get a file path from a folder-file pair.
     *
     * @param folderPath The folder path.
     * @param fileName   The file name.
     * @return The file path.
     */
    public static String getFilePath(String folderPath, String fileName) {
        return Paths.get(folderPath, fileName).toString();
    }

    /**
     * Get the base64 representation of a string.
     *
     * @param input The string to encode.
     * @return The base64 representation of the string.
     */
    public static String toBase64String(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes());
    }

    /**
     * Create an input stream from a string.
     *
     * @param string The string to create the input stream from.
     * @return The input stream.
     */
    public static InputStream createInputStream(String string) {
        if (string == null) {
            return new ByteArrayInputStream(new byte[] { });
        }
        return new ByteArrayInputStream(string.getBytes());
    }

    /**
     * Create an output stream from a string.
     *
     * @param string The string to create the output stream from.
     * @return The output stream.
     */
    public static OutputStream createOutputStream(String string) throws IOException {
        if (string == null) {
            return new ByteArrayOutputStream();
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(string.getBytes());
        return outputStream;
    }

    /**
     * Make a copy of an input stream (resetting the position to 0).
     *
     * @param stream The input stream to copy.
     * @return A copy of the input stream.
     */
    public static InputStream copyInputStream(InputStream stream) {
        if (stream == null) {
            return null;
        }
        try {
            stream.reset();
        } catch (IOException ignored) {
        }
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            // TODO: Stream not resetting (len = -1)
            while ((len = stream.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
            return new ByteArrayInputStream(baos.toByteArray());
        } catch (IOException ignored) {
            return new ByteArrayInputStream(new byte[] { });
        }
    }

    /**
     * Read the contents of an input stream into a string.
     *
     * @param stream The input stream to read.
     * @return The contents of the input stream as a string.
     */
    public static String readFromInputStream(InputStream stream) {
        if (stream == null) {
            return null;
        }
        InputStream copy = copyInputStream(stream);
        String str = null;
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(copy));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            str = content.toString();
        } catch (IOException ignored) {
        }
        return str;
    }

    /**
     * Convert a map to a query parameters string.
     *
     * @param map The map to convert.
     * @return The query parameters string.
     */
    public static List<ApachePatch.NameValuePair> mapToQueryParameters(Map<String, String> map) {
        if (map == null || map.size() == 0) {
            return Collections.emptyList();
        }
        List<ApachePatch.NameValuePair> nvpList = new ArrayList<>(map.size());
        for (Map.Entry<String, String> entry : map.entrySet()) {
            nvpList.add(new ApachePatch.NameValuePair(entry.getKey(), entry.getValue()));
        }
        return nvpList;
    }

    /**
     * Convert a URI's query parameters to a Map.
     *
     * @param uri The URI.
     * @return The Map of query parameters.
     */
    public static Map<String, String> queryParametersToMap(URI uri) {
        List<ApachePatch.NameValuePair> receivedQueryDict =
                ApachePatch.URLEncodedUtils.parse(uri, StandardCharsets.UTF_8);
        if (receivedQueryDict == null || receivedQueryDict.size() == 0) {
            return Collections.emptyMap();
        }
        Map<String, String> queryDict = new java.util.Hashtable<>();
        for (ApachePatch.NameValuePair pair : receivedQueryDict) {
            queryDict.put(pair.getName(), pair.getValue());
        }
        return queryDict;
    }

    /**
     * Sleep the current thread for a specified number of milliseconds.
     *
     * @param interaction      The interaction used to determine the number of milliseconds to sleep.
     * @param advancedSettings The advanced settings used to determine the number of milliseconds to sleep.
     * @throws InterruptedException If the thread is interrupted.
     */
    public static void simulateDelay(HttpInteraction interaction, AdvancedSettings advancedSettings)
            throws InterruptedException {
        if (advancedSettings.simulateDelay) {
            Thread.sleep(interaction.getDuration());
        } else {
            Thread.sleep(advancedSettings.manualDelay);
        }
    }
}
