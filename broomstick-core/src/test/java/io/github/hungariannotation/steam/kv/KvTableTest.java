package io.github.hungariannotation.steam.kv;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map.Entry;

import static java.util.Map.entry;

import org.junit.jupiter.api.Test;

import io.github.hungariannotation.steam.test.TestData;

public class KvTableTest {

    private static Entry<String, KvValue> kv(String key, String value) {
        return entry(key, new KvString(value));
    }

    private static Entry<String, KvValue> kv(String key, List<Entry<String, KvValue>> table) {
        return entry(key, new KvTable(table));
    }

    private static KvTable root(List<Entry<String, KvValue>> table) {
        return new KvTable(table);
    }

    private static KvTable root(Entry<String, KvValue> root) {
        return root(List.of(root));
    }

    @Test
    void testToJson() {
        var json = "{\"test\": {\"0\": \"zero\", \"1\": \"one\"}}";
        var obj = root(kv("test", List.of(kv("0", "zero"), kv("1", "one"))));
        assertEquals(json, obj.toJson());
    }

    @Test
    void testToJson2() {
        var json = "{\"test1\": {}, \"test2\": {}, \"test3\": {}}";
        var obj = root(List.of(kv("test1", List.of()), kv("test2", List.of()), kv("test3", List.of())));
        assertEquals(json, obj.toJson());
    }

    /**
     * Given a copy of my own libraryfolders.vdf (with digits randomly rotated so
     * you can't judge my gaming habits) verify that VdfTable stringifies itself
     * with the same normalization as the Steam client.
     * <p>
     * This isn't a requirement for correctness, and there are some seemingly
     * hand-written vdf files scattered throughout my Steam installation that do not
     * conform to this standard.
     */
    @Test
    void testSameNormalizationAsSteam() {
        assertDoesNotThrow(() -> {
            var input = TestData.getResourceAsString(this.getClass(), "libraryfolders.vdf");
            var denormalizedInput = TestData.getResourceAsString(this.getClass(), "libraryfolders.vdf.denormalized");
            var normalized = KvParser.parse(denormalizedInput).toString();
            assertEquals(input, normalized);
        });
    }
}
