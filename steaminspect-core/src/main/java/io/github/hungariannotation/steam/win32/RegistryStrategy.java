package io.github.hungariannotation.steam.win32;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface RegistryStrategy {
    CompletableFuture<List<RegistryValue>> getRegistryKey(RegistryHiveKey hive, final String key);
}
