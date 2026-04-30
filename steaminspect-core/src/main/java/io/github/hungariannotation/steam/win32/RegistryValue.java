package io.github.hungariannotation.steam.win32;

import java.util.Optional;

/**
 * Holds information about a value in the Windows registry.
 * 
 * @param type The presence and accuracy of this value depends on the
 *             implementation used to retrieve it. It should not be relied on.
 */
public record RegistryValue(String key, String name, Optional<RegistryType> type, Object value) {

    public RegistryValue(String key, String name, Object value) {
        Optional<RegistryType> type = Optional.empty();

        if (value instanceof Integer)
            type = Optional.of(RegistryType.REG_DWORD);
        if (value instanceof Long)
            type = Optional.of(RegistryType.REG_QWORD);
        if (value instanceof byte[])
            type = Optional.of(RegistryType.REG_BINARY);
        if (value instanceof String)
            type = Optional.of(RegistryType.REG_SZ);

        this(key, name, type, value);
    }

    public RegistryValue(String key, String name, String type, Object value) {
        this(key, name, Optional.of(RegistryType.valueOf(type)), value);
    }

    @Override
    public String toString() {
        return "RegistryValue [%s %s%s]".formatted(name,
                type.map(t -> t.name() + " ").orElse(""),
                getDisplayString());
    }

    /**
     * Returns a view of this object as a potential String value.
     * 
     * @return
     */
    public Optional<String> asString() {
        if (value instanceof String typedValue) {
            return Optional.of(typedValue);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Returns a view of this object as a potential Integer value.
     * 
     * @return
     */
    public Optional<Integer> asInteger() {
        if (value instanceof Integer typedValue) {
            return Optional.of(typedValue);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Returns a view of this object as a potential Long value.
     * 
     * @return
     */
    public Optional<Long> asLong() {
        if (value instanceof Long typedValue) {
            return Optional.of(typedValue);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Returns a view of this object as a potential Number.
     * 
     * @return
     */
    public Optional<Number> asNumber() {
        if (value instanceof Number typedValue) {
            return Optional.of(typedValue);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Returns a view of this object as a potential array of bytes. This corresponds
     * to the {@link RegistryType#REG_BINARY} type.
     * 
     * @return
     */
    public Optional<byte[]> asBytes() {
        if (value instanceof byte[] typedValue) {
            return Optional.of(typedValue);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Attempts to format the contents of this value as they would appear in the
     * output of the {@code REG QUERY} command.
     * 
     * @return
     */
    public String getDisplayString() {
        if (value instanceof byte[] bytes) {
            StringBuilder builder = new StringBuilder();
            for (var i = 0; i < bytes.length; ++i) {
                int intValue = Byte.toUnsignedInt(bytes[i]);
                builder.append("%02X".formatted(intValue));
            }
            return builder.toString();
        } else if (value instanceof Integer dword) {
            return "0x%x".formatted(dword);
        } else if (value instanceof Long qword) {
            return "0x%x".formatted(qword);
        }
        return String.valueOf(value);
    }

}