package io.github.hungariannotation.steam.win32.jna;

import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import io.github.hungariannotation.steam.win32.RegistryHiveKey;
import io.github.hungariannotation.steam.win32.RegistryStrategy;
import io.github.hungariannotation.steam.win32.RegistryValue;

public class NativeRegistryStrategy implements RegistryStrategy {

    public static RegistryStrategy getRegistryStrategy() {
        return new NativeRegistryStrategy();
    }

    @Override
    public CompletableFuture<List<RegistryValue>> getRegistryKey(RegistryHiveKey hive, String key) {
        System.out.println("using %s".formatted(NativeRegistryStrategy.class));
        return getRegistryKeyUsingJNA(hive, key);
    }

    private static final WinReg.HKEY getHandle(RegistryHiveKey hive) {
        return switch (hive) {
            case HKEY_CLASSES_ROOT -> WinReg.HKEY_CLASSES_ROOT;
            case HKEY_CURRENT_CONFIG -> WinReg.HKEY_CURRENT_CONFIG;
            case HKEY_CURRENT_USER -> WinReg.HKEY_CURRENT_USER;
            case HKEY_LOCAL_MACHINE -> WinReg.HKEY_LOCAL_MACHINE;
            case HKEY_USERS -> WinReg.HKEY_USERS;
        };
    }

    private static CompletableFuture<List<RegistryValue>> getRegistryKeyUsingJNA(
            RegistryHiveKey hive,
            final String key) {
        TreeMap<String, Object> invocationResult = Advapi32Util.registryGetValues(getHandle(hive), key);
        List<RegistryValue> records = jnaResultToRecords(hive, key, invocationResult);
        return CompletableFuture.completedFuture(records);
    }

    private static List<RegistryValue> jnaResultToRecords(
            RegistryHiveKey hive,
            final String key,
            TreeMap<String, Object> map) {
        return map.keySet().stream().map(resultKey -> {
            var value = map.get(resultKey);
            return new RegistryValue(hive.getFullName() + "\\" + key, resultKey, value);
        }).toList();
    }

}
