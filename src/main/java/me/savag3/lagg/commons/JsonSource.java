package me.savag3.lagg.commons;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @author Savag3life
 * @since 1/31/2023
 * @version 1.0
 */
public class JsonSource {

    private final Gson gson;

    public JsonSource(Gson gson) {
        this.gson = gson;
    }

    public JsonSource() {
        this(
                new GsonBuilder()
                        .setPrettyPrinting()
                        .disableHtmlEscaping()
                        .serializeNulls()
                        .enableComplexMapKeySerialization()
                        .disableInnerClassSerialization()
                        .excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.VOLATILE)
                        .create()
        );
    }

    /**
     * Deserializes a JSON string stored in a file into an object of the specified type.
     * @param type The type of the object to deserialize to.
     * @param file The file to deserialize from.
     * @param <T> The type of the object to deserialize to.
     * @return The deserialized object.
     */
    public <T> T load(Class<T> type, File file) {
        String content = Disk.toUTF8(Objects.requireNonNull(Disk.read(file)));
        try {
            return this.gson.fromJson(content, type);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Check &/or load a given object from disk or save a new default copy to disk.
     * If loaded data is null, broken, or invalid, a new default object is saved to disk &
     * the broken file is renamed to `.broken` to ensure it is not loaded again but not lost.
     * @param object The object to check &/or load.
     * @param file The file to check &/or load from.
     * @param type The type of the object to check &/or load.
     * @param <T> The type of the object to check &/or load.
     * @return The loaded object.
     */
    @SuppressWarnings("all")
    public <T> T loadOrSaveDefault(T object, File file, Class<T> type) {
        try {
            if (!file.exists()) {
                file.createNewFile();
                save(object, file);
                return object;
            }
        } catch (IOException er) {
            er.printStackTrace();
            return object;
        }

        T newObject = this.load(type, file);

        if (newObject == null) {
            File backup = new File(file.getAbsolutePath() + ".broken");
            if (backup.exists()) backup.delete();
            file.renameTo(backup);
            save(object, file);
            return object;
        }

        return newObject;
    }

    /**
     * Serializes an object into a JSON string.
     * @param object The object to serialize.
     * @param file The file to serialize to.
     */
    public void save(Object object, File file) {
        Disk.write(file, this.gson.toJson(object).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Deserializes a JSON string into an object of the specified type.
     * @param type The type of the object to deserialize to.
     * @param file The file to deserialize from.
     * @param <T> The type of the object to deserialize to.
     * @return The deserialized object.
     */
    public <T> T fromJson(TypeToken<T> type, File file) {
        String content = Disk.toUTF8(Objects.requireNonNull(Disk.read(file)));
        return fromJson(type, content);
    }

    /**
     * Deserializes a JSON string into an object of the specified type.
     * @param type The type of the object to deserialize to.
     * @param content The JSON string to deserialize.
     * @param <T> The type of the object to deserialize to.
     * @return The deserialized object.
     */
    public <T> T fromJson(TypeToken<T> type, String content) {
        return this.gson.fromJson(content, type.getType());
    }

}
