package com.lowdragmc.shimmer;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import org.apache.commons.compress.utils.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * @author KilaBash
 * @date 2022/5/7
 * @implNote FileUtility
 */
public class FileUtility {
    public static final JsonParser jsonParser = new JsonParser();

    private FileUtility() {
    }

    public static String readInputStream(InputStream inputStream) throws IOException {
        byte[] streamData = IOUtils.toByteArray(inputStream);
        return new String(streamData, StandardCharsets.UTF_8);
    }

    public static InputStream writeInputStream(String contents) {
        return new ByteArrayInputStream(contents.getBytes(StandardCharsets.UTF_8));
    }

    public static JsonElement loadJson(File file) {
        try {
            if (!file.isFile()) return null;
            Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
            JsonElement json = jsonParser.parse(new JsonReader(reader));
            reader.close();
            return json;
        } catch (Exception ignored) {
        }
        return null;
    }

    public static void extractJarFiles(String resource, File targetPath, boolean replace) {
        try {
            if (!targetPath.exists() || replace) {
                InputStream inputstream = FileUtility.class.getResourceAsStream(resource);
                if (inputstream != null) {
                    String content = readInputStream(inputstream);
                    Writer fileWriter = new OutputStreamWriter(new FileOutputStream(targetPath), StandardCharsets.UTF_8);
                    fileWriter.write(content);
                    fileWriter.close();
                }
            }
        } catch (Exception ignored) {
        }
    }
}
