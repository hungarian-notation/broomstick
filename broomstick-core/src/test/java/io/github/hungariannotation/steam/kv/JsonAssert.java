package io.github.hungariannotation.steam.kv;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;
import java.io.StringWriter;

import jakarta.json.Json;

public class JsonAssert {
    public static String normalizeJson(String json) {
        try (var reader = Json.createReader(new StringReader(json))) {
            var value = reader.readValue();
            var stringWriter = new StringWriter();
            var writer = Json.createWriter(stringWriter);
            writer.write(value);
            return stringWriter.toString();
        }
    }

    public static void assertSimilarJson(String expected, String actual) {
        assertEquals(normalizeJson(expected), normalizeJson(actual));
    }
}
