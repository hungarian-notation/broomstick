package io.github.hungariannotation.steam.win32;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class FallbackRegistryStrategy implements RegistryStrategy {

    private static final Pattern REGISTRY_VALUE = Pattern.compile("^\s*(.*?)\s+([^\s]+)\s+(.*)$");

    private static final Duration SHELL_TIMEOUT = Duration.ofSeconds(2);

    @Override
    public CompletableFuture<List<RegistryValue>> getRegistryKey(RegistryHiveKey hive, String key) {
        System.out.println("using %s".formatted(FallbackRegistryStrategy.class));
        if (!Registry.isAvailable())
            return CompletableFuture.failedFuture(
                    new RuntimeException("platform is not windows: %s".formatted(System.getProperty("os.name"))));

        String path = "%s\\%s".formatted(hive.name(), key);

        final ProcessBuilder p = new ProcessBuilder(
                "REG", "QUERY", path);
        try {
            final var process = p.start();
            final var future = process.onExit().thenApply(result -> {
                return result;
            });

            return future.orTimeout(SHELL_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS).exceptionally((e) -> {
                process.destroyForcibly();
                throw new RuntimeException(e);
            }).thenApply(exited -> {
                if (exited.exitValue() != 0) {
                    String message;

                    try {
                        message = exited.errorReader().readAllAsString();
                    } catch (final IOException e) {
                        message = "unspecified error";
                    }

                    throw new RuntimeException(message);
                } else {
                    try {
                        return exited.inputReader()
                                .readAllLines()
                                .stream()
                                .filter(line -> !line.isBlank())
                                .toList();
                    } catch (final IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
            }).thenApply(lines -> {
                return lines.subList(1, lines.size())
                        .stream()
                        .map(line -> parseRegistryValue(lines.get(0), line))
                        .filter(each -> each != null).toList();
            });
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Parse a registry value as returned by REG QUERY
     * 
     * @param key
     * @param line
     * @return
     */
    private static RegistryValue parseRegistryValue(String key, String line) {
        final var matched = REGISTRY_VALUE.matcher(line);
        if (matched.find()) {
            var type = RegistryType.valueOf(matched.group(2));
            var value = matched.group(3);
            return new RegistryValue(key,
                    matched.group(1),
                    matched.group(2),
                    type.convertValue(value));
        }
        return null;
    }

}
