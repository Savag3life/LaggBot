package me.savag3.lagg.commons;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author Savag3life
 * @since 1/31/2023
 * @version 1.0
 */
@UtilityClass
public class Disk {

    /**
     * Read All bytes from a given file
     * @param file File to read all bytes from
     * @return All bytes from the given file
     */
    public static byte[] read(@NonNull File file) {
        int length = (int) file.length();
        byte[] bytes = new byte[length];
        try (FileInputStream in = new FileInputStream(file)) {
            int offset = 0;
            while (offset < length) offset += in.read(bytes, offset, length - offset);
            return bytes;
        } catch (IOException ignored) {
            return null;
        }
    }

    /**
     * Write all bytes to a given file
     * @param file File to write all bytes to
     * @param bytes Bytes to write to the given file
     */
    public static void write(@NonNull File file, byte @NonNull [] bytes) {
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(bytes);
        } catch (IOException ignored) { }
    }

    /**
     * Convert bytes to UTF-8 String
     * @param bytes Bytes to convert to UTF-8 String
     * @return UTF-8 String from the given bytes
     */
    public static String toUTF8(byte @NonNull [] bytes) {
        return new String(bytes, 0, bytes.length, StandardCharsets.UTF_8);
    }

    /**
     * Convert UTF-8 String to bytes
     * @param string UTF-8 String to convert to bytes
     * @return Bytes from the given UTF-8 String
     */
    public static byte[] toUTF8(@NonNull String string) {
        return string.getBytes(StandardCharsets.UTF_8);
    }
}
