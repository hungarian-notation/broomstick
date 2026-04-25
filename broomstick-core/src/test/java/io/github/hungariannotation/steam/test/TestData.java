package io.github.hungariannotation.steam.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class TestData {

    public static String getResourceAsString(Class<?> context, String name) {
        var reader = new InputStreamReader(context.getResourceAsStream(name));
        try {
            return reader.readAllAsString();
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    @Deprecated
    public static InputStream getStream(Class<?> context, String name) {
        return context.getResourceAsStream(name);
    }

    public static Reader getReader(Class<?> context, String name) {
        return new InputStreamReader(getStream(context, name));
    }

}
