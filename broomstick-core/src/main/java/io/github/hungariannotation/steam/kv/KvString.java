package io.github.hungariannotation.steam.kv;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Stream;

/**
 * The subclass of {@link KvValue} that holds string values.
 */
public final class KvString extends KvValue {

    private final String value;

    public KvString(String value) {
        this.value = Objects.requireNonNull(value);
    }

    public String value() {
        return value;
    }

    public boolean equalsString(String other) {
        return value.equals(other);
    }

    @Override
    public boolean isString() {
        return true;
    }

    @Override
    public Optional<KvValue> get(String key) {
        return Optional.empty();
    }

    @Override
    public Optional<String> getString(String key) {
        return Optional.empty();
    }

    @Override
    public Optional<KvTable> getTable(String key) {
        return Optional.empty();
    }

    @Override
    public Optional<String> asString() {
        return Optional.of(value);
    }

    @Override
    public Optional<KvTable> asTable() {
        return Optional.empty();
    }

    @Override
    public Map<String, KvValue> asMap() {
        return Collections.emptyMap();
    }

    @Override
    public Stream<String> streamKeys() {
        return Stream.empty();
    }

    @Override
    public Stream<KvValue> streamValues() {
        return Stream.empty();
    }

    @Override
    public Stream<String> streamKeysOf(String key) {
        return Stream.empty();
    }

    @Override
    public Stream<KvValue> streamValuesOf(String key) {
        return Stream.empty();
    }

    @Override
    public Stream<Entry<String, KvValue>> streamEntries() {
        return Stream.empty();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        KvString other = (KvString) obj;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

    public String toJson() {
        return String.format("\"%s\"", escapeString(value));
    }

    public String toString() {
        return toJson();
    }

}