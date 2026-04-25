package io.github.hungariannotation.steam.impl.win32;

import java.util.Optional;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

record WinRegPath(WinReg.HKEY root, String key, String value) {
    public Optional<Object> getValue() {
        if (Advapi32Util.registryValueExists(root, key, value)) {
            return Optional.of(Advapi32Util.registryGetValue(root, key, value));
        } else {
            return Optional.empty();
        }
    }

    public Optional<String> getString() {
        return getValue().flatMap(v -> v instanceof String s ? Optional.of(s) : Optional.empty());
    }
}