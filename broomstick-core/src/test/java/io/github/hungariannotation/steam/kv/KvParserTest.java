package io.github.hungariannotation.steam.kv;

import static io.github.hungariannotation.steam.kv.JsonAssert.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import io.github.hungariannotation.steam.test.TestData;

public class KvParserTest {

    // some basic transformers that convert VDF files into s-expressions

    static String lispString(String value) {
        return String.format("'%s'", value);
    }

    static String lispCons(List<Map.Entry<String, String>> values) {
        return values.stream()
                .map(each -> List.of(String.format("'%s'", each.getKey()), each.getValue())
                        .stream()
                        .collect(Collectors.joining(" ", "(", ")")))
                .collect(Collectors.joining(" "));
    }

    @Test
    void testParseFactory() {
        assertDoesNotThrow(() -> {
            var expected = "('root' ('key1' 'value1') ('key2' 'value2') ('key3' 'value3'))";
            var stream = TestData.getReader(this.getClass(), "basic.vdf");
            var parsed = KvParser.parse(stream, KvParserTest::lispString, KvParserTest::lispCons);
            System.out.println(parsed);
            assertEquals(expected, parsed);
        });
    }

    @Test
    void testParse() {
        assertDoesNotThrow(() -> {
            var expected = TestData.getResourceAsString(this.getClass(), "basic.vdf.json");
            var stream = TestData.getReader(this.getClass(), "basic.vdf");
            var parsed = KvParser.parse(stream).toJson();
            // System.out.println(parsed);
            assertSimilarJson(expected, parsed);
        });
    }

    @Test
    void testParse2() {
        assertDoesNotThrow(() -> {
            var expected = TestData.getResourceAsString(this.getClass(), "structure.vdf.json");
            var stream = TestData.getReader(this.getClass(), "structure.vdf");
            var parsed = KvParser.parse(stream).toJson();
            // System.out.println(parsed);
            assertSimilarJson(expected, parsed);
        });
    }

    @Test
    void testParse3() {
        assertDoesNotThrow(() -> {
            var expected = TestData.getResourceAsString(this.getClass(), "complex.vdf.json");
            var stream = TestData.getReader(this.getClass(), "complex.vdf");
            var parsed = KvParser.parse(stream).toJson();
            // System.out.println(parsed);
            assertSimilarJson(expected, parsed);
        });
    }

    @Test
    void testParseErrors() {
        var exception = assertThrows(RuntimeException.class, () -> {
            KvParser.parse("a {");
        });

        assertTrue(exception.getMessage().contains("unexpected EOF"),
                () -> String.format("message was: %s", exception.getMessage()));
    }

    @Test
    void testParseErrors2() {
        var exception = assertThrows(RuntimeException.class, () -> {
            KvParser.parse("a { b }");
        });

        assertTrue(exception.getMessage().contains("unexpected close brace"),
                () -> String.format("message was: %s", exception.getMessage()));
    }
}
