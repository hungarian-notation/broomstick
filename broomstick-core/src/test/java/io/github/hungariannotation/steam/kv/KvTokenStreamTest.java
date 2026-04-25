package io.github.hungariannotation.steam.kv;

import org.junit.jupiter.api.Test;

import io.github.hungariannotation.steam.kv.KvToken.KvCloseToken;
import io.github.hungariannotation.steam.kv.KvToken.KvEOF;
import io.github.hungariannotation.steam.kv.KvToken.KvOpenToken;

import static io.github.hungariannotation.steam.test.CustomAssert.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class KvTokenStreamTest {

    private KvTokenStream createStream(String string) {
        return new KvTokenStream(this.getClass().getResourceAsStream(string));
    }

    @Test
    void testRead() {
        assertDoesNotThrow(() -> {
            try (var tokenizer = createStream("basic.vdf")) {
                assertStringToken("root", tokenizer.read());
                assertInstanceOf(KvOpenToken.class, tokenizer.read());
                assertStringToken("key1", tokenizer.read());
                assertStringToken("value1", tokenizer.read());
                assertStringToken("key2", tokenizer.read());
                assertStringToken("value2", tokenizer.read());
                assertStringToken("key3", tokenizer.read());
                assertStringToken("value3", tokenizer.read());
                assertInstanceOf(KvCloseToken.class, tokenizer.read());
                assertInstanceOf(KvEOF.class, tokenizer.read());
            }
        });
    }

    @Test
    void testRead2() {
        assertDoesNotThrow(() -> {
            try (var tokenizer = createStream("complex.vdf")) {
                assertStringToken("things", tokenizer.read());
                assertInstanceOf(KvOpenToken.class, tokenizer.read());
                assertStringToken("0", tokenizer.read());
                assertInstanceOf(KvOpenToken.class, tokenizer.read());
                assertStringToken("name", tokenizer.read());
                assertStringToken("thing0", tokenizer.read());
                assertInstanceOf(KvCloseToken.class, tokenizer.read());
                assertStringToken("1", tokenizer.read());
                assertInstanceOf(KvOpenToken.class, tokenizer.read());
                assertStringToken("name", tokenizer.read());
                assertStringToken("thing1", tokenizer.read());
                assertInstanceOf(KvCloseToken.class, tokenizer.read());
                assertStringToken("2", tokenizer.read());
                assertInstanceOf(KvOpenToken.class, tokenizer.read());
                assertInstanceOf(KvCloseToken.class, tokenizer.read());
                assertInstanceOf(KvCloseToken.class, tokenizer.read());
                assertStringToken("string", tokenizer.read());
                assertStringToken("\n\nmultiline", tokenizer.read());
                assertStringToken("🤖", tokenizer.read());
                assertStringToken("🤖", tokenizer.read());
                assertStringToken("more things", tokenizer.read());
                assertStringToken("no", tokenizer.read());
                assertInstanceOf(KvEOF.class, tokenizer.read());

            }
        });
    }

    @Test
    void testRead3() {
        assertDoesNotThrow(() -> {
            try (var tokenizer = createStream("structure.vdf")) {
                assertStringToken("a", tokenizer.read());
                assertInstanceOf(KvOpenToken.class, tokenizer.read());
                assertStringToken("b", tokenizer.read());
                assertStringToken("c", tokenizer.read());
                assertStringToken("d", tokenizer.read());
                assertStringToken("e", tokenizer.read());
                assertStringToken("f", tokenizer.read());
                assertInstanceOf(KvOpenToken.class, tokenizer.read());
                assertStringToken("g", tokenizer.read());
                assertStringToken("h", tokenizer.read());
                assertInstanceOf(KvCloseToken.class, tokenizer.read());
                assertStringToken("i", tokenizer.read());
                assertStringToken("j", tokenizer.read());
                assertStringToken("k", tokenizer.read());
                assertInstanceOf(KvOpenToken.class, tokenizer.read());
                assertStringToken("l", tokenizer.read());
                assertStringToken("m", tokenizer.read());
                assertInstanceOf(KvCloseToken.class, tokenizer.read());
                assertStringToken("n", tokenizer.read());
                assertInstanceOf(KvOpenToken.class, tokenizer.read());
                assertInstanceOf(KvCloseToken.class, tokenizer.read());
                assertInstanceOf(KvCloseToken.class, tokenizer.read());
                assertInstanceOf(KvEOF.class, tokenizer.read());
            }
        });
    }
}
