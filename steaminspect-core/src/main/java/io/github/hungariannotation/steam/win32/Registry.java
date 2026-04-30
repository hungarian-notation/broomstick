package io.github.hungariannotation.steam.win32;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * <p>
 * Static methods for interrogating the Windows platform. This class will
 * attempt to dynamically load and use the native registry interface provided by
 * JNA (com.sun.jna) if:
 * <ul>
 * <li>com.sun.jna.platform.win32.Advapi32Util and
 * com.sun.jna.platform.win32.WinReg are in the classpath
 * <li>{@link Module#isNativeAccessEnabled()} returns true for this class
 * </ul>
 * <p>
 * If either of those conditions are not met, the methods in this class
 * will fall back to an implementation that invokes {@code REG QUERY} as a shell
 * command.
 * <p>
 * The methods in this class are asynchronous for the benefit of the
 * {@code REG QUERY} implementation. When using JNA, the returned
 * {@link CompletableFuture}s will already be completed.
 */
public class Registry {

    private static final String JNA_STATIC_FACTORY = "getRegistryStrategy";
    private static final String JNA_STATIC_FACTORY_CLASS = "io.github.hungariannotation"
            + ".steam.win32.jna.NativeRegistryStrategy";

    private static final MethodHandle jnaHandle = initJnaHandler();

    private static final MethodHandle initJnaHandler() {
        if (!Registry.class.getModule().isNativeAccessEnabled())
            return null;
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        var type = MethodType.methodType(RegistryStrategy.class);
        MethodHandle handle;
        try {
            handle = lookup.findStatic(
                    Class.forName(JNA_STATIC_FACTORY_CLASS),
                    JNA_STATIC_FACTORY, type);
        } catch (ReflectiveOperationException e) {
            handle = null;
        }
        return handle;
    }

    /**
     * Check if the platform is Windows.
     * 
     * @return
     */
    public static boolean isAvailable() {
        return System.getProperty("os.name").toLowerCase().contains("w");
    }

    public static final RegistryStrategy getFallbackStrategy() {
        return new FallbackRegistryStrategy();
    }

    public static final RegistryStrategy getStrategy() {
        if (jnaHandle != null) {
            try {
                return (RegistryStrategy) jnaHandle.invokeExact();
            } catch (RuntimeException e) {
                throw e;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        } else {
            return Registry.getFallbackStrategy();
        }
    }

    public static CompletableFuture<List<RegistryValue>> getRegistryKey(RegistryHiveKey hive, final String key) {
        return getStrategy().getRegistryKey(hive, key);
    }

    public static CompletableFuture<Optional<RegistryValue>> getRegistryValue(RegistryHiveKey hive, final String key,
            final String name) {
        return getRegistryKey(hive, key).thenApply(values -> values
                .stream()
                .filter(each -> each.name().equalsIgnoreCase(name))
                .findAny());
    }

}
