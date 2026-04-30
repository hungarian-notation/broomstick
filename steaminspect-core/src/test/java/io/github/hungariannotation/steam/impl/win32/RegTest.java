package io.github.hungariannotation.steam.impl.win32;

import org.junit.jupiter.api.Test;

import io.github.hungariannotation.steam.win32.Registry;
import io.github.hungariannotation.steam.win32.RegistryHiveKey;

public class RegTest {

    @Test
    void testParseFactory() {
        Registry.getRegistryValue(RegistryHiveKey.HKEY_LOCAL_MACHINE, "SOFTWARE\\WOW6432Node\\Valve\\Steam", "InstallPath");
    }
}
