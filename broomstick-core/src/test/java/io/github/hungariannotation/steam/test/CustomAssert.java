package io.github.hungariannotation.steam.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import io.github.hungariannotation.steam.kv.KvToken.KvStringToken;

public class CustomAssert {

    public static void assertStringToken(String expected, Object token) {
        if (token instanceof KvStringToken stringToken) {
            assertEquals(expected, stringToken.value);
        } else {
            fail(String.format("not an instance of %s, was %s", KvStringToken.class, token.getClass()));
        }
    
    }

}
