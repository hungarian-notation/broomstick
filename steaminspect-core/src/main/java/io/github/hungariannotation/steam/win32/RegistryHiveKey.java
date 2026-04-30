package io.github.hungariannotation.steam.win32;

import java.util.List;
import java.util.Optional;

/**
 * Represents a top level key (i.e. hive) of the Windows Registry. This enum
 * only contains the hives accessible via the {@code REG QUERY} command.
 */
public enum RegistryHiveKey {
    HKEY_LOCAL_MACHINE("HKEY_LOCAL_MACHINE", "HKLM"),
    HKEY_CURRENT_USER("HKEY_CURRENT_USER", "HKCU"),
    HKEY_CLASSES_ROOT("HKEY_CLASSES_ROOT", "HKCR"),
    HKEY_USERS("HKEY_USERS", "HKU"),
    HKEY_CURRENT_CONFIG("HKEY_CURRENT_CONFIG", "HKCC"),

    /*
     * These are keys that have some meaning to the registry, but are not supported
     * by REG QUERY:
     */
    // HKEY_PERFORMANCE_DATA("HKEY_PERFORMANCE_DATA"),
    // HKEY_PERFORMANCE_TEXT("HKEY_PERFORMANCE_TEXT"),
    // HKEY_PERFORMANCE_NLSTEXT("HKEY_PERFORMANCE_NLSTEXT"),
    // HKEY_DYN_DATA("HKEY_DYN_DATA"),
    // HKEY_CURRENT_USER_LOCAL_SETTINGS("HKEY_CURRENT_USER_LOCAL_SETTINGS"),

    ;

    public static final RegistryHiveKey HKCR = RegistryHiveKey.HKEY_CLASSES_ROOT;
    public static final RegistryHiveKey HKCU = RegistryHiveKey.HKEY_CURRENT_USER;
    public static final RegistryHiveKey HKLM = RegistryHiveKey.HKEY_LOCAL_MACHINE;
    public static final RegistryHiveKey HKU = RegistryHiveKey.HKEY_USERS;
    public static final RegistryHiveKey HKCC = RegistryHiveKey.HKEY_CURRENT_CONFIG;

    private final String name;
    private final List<String> aliases;

    public List<String> getAliases() {
        return aliases;
    }

    public String getFullName() {
        return name;
    }

    RegistryHiveKey(String string, String... alias) {
        this.name = string;
        this.aliases = List.of(alias);
    }

    /**
     * Return the key for a given name, or an empty optional if the name can not be
     * resolved.
     * 
     * This method also accepts the same abbreviated names that Windows supports,
     * such as {@code "HKLM"} for {@link #HKEY_LOCAL_MACHINE}.
     * 
     * @param name
     * @return
     */
    public static Optional<RegistryHiveKey> parse(String name) {
        for (var value : RegistryHiveKey.values()) {
            if (value.name.equalsIgnoreCase(name)) {
                return Optional.of(value);
            } else if (value.aliases.contains(name.toUpperCase())) {
                return Optional.of(value);
            }
        }

        return Optional.empty();
    }

}