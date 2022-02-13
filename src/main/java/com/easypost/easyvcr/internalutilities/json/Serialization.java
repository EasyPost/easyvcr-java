package com.easypost.easyvcr.internalutilities.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

/**
 * JSON de/serialization utilities.
 */
public final class Serialization {
    /**
     * Convert a JSON string to an object.
     *
     * @param json  JSON string
     * @param clazz Class of the object to convert to
     * @param <T>   Type of the object to convert to
     * @return Object of type clazz
     */
    public static <T> T convertJsonToObject(String json, Class<T> clazz) {
        Gson gson = new Gson();
        return gson.fromJson(json, clazz);
    }

    /**
     * Convert a JSON element to an object.
     *
     * @param json  JSON element
     * @param clazz Class of the object to convert to
     * @param <T>   Type of the object to convert to
     * @return Object of type clazz
     */
    public static <T> T convertJsonToObject(JsonElement json, Class<T> clazz) {
        Gson gson = new Gson();
        return gson.fromJson(json, clazz);
    }

    /**
     * Convert an object to a JSON string.
     *
     * @param object Object to convert
     * @return JSON string
     */
    public static String convertObjectToJson(Object object) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(object);
    }
}
