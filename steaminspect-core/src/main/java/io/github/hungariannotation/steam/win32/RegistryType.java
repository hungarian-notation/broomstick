package io.github.hungariannotation.steam.win32;

import java.util.function.Function;

/**
 * A Windows Registry value type.
 */
public enum RegistryType {
    REG_BINARY(RegistryType::toBytes),
    REG_DWORD(RegistryType::toInt),
    REG_DWORD_LITTLE_ENDIAN(RegistryType::toInt),
    REG_DWORD_BIG_ENDIAN(RegistryType::toInt),
    REG_EXPAND_SZ,
    REG_LINK,
    REG_MULTI_SZ,
    REG_NONE,
    REG_QWORD(RegistryType::toLong),
    REG_QWORD_LITTLE_ENDIAN(RegistryType::toLong),
    REG_SZ;

    private static final String identity(String string) {
        return string;
    }

    private static final Long toLong(String string) {
        if (string.startsWith("0x")) {
            string = string.substring(2);
            return Long.parseLong(string, 16);
        }

        return Long.parseLong(string);
    }

    private static final Integer toInt(String string) {
        if (string.startsWith("0x")) {
            string = string.substring(2);
            return Integer.parseInt(string, 16);
        }
        return Integer.parseInt(string);
    }

    private static final byte[] toBytes(String string) {
        byte[] bytes = new byte[string.length() / 2];

        for (int i = 0; i < bytes.length; ++i) {
            int pos = i * 2;
            bytes[i] = (byte) Integer.parseInt(string.substring(pos, pos + 2), 16);
        }

        return bytes;
    }

    private final Function<String, Object> defaultConversion;

    RegistryType(Function<String, Object> defaultConversion) {
        this.defaultConversion = defaultConversion;
    }

    RegistryType() {
        this(RegistryType::identity);
    }

    /**
     * Convert a string representation as returned by {@code REG QUERY} to an
     * appropriate Java type for this registry type.
     * 
     * @param string
     * @return
     */
    public Object convertValue(String string) {
        return defaultConversion.apply(string);
    }
}