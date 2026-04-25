package io.github.hungariannotation.steam.kv;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.Map.Entry;

/**
 * Represents a value in a KV file.
 */
public abstract sealed class KvValue permits KvString, KvTable {

    /**
     * Provided for completeness, but you should prefer to use either pattern
     * matching or the functional {@link Optional} and {@link Stream} accessors.
     * 
     * <pre>
     * {@code
     * // preferred alternative:
     * if (vdfValue instanceof KvString string) {
     *     string.value();
     * }
     * }
     * </pre>
     * 
     * @return True if this is a {@link KvString}
     */
    public abstract boolean isString();

    /**
     * Provided for completeness, but you should prefer to use either pattern
     * matching or the functional {@link Optional} and {@link Stream} accessors.
     * 
     * <pre>
     * {@code
     * // preferred alternative:
     * if (vdfValue instanceof KvTable table) {
     *     table.keySet();
     *     table.values();
     * }
     * }
     * </pre>
     * 
     * @return True if this is a {@link KvTable}
     */
    public final boolean isTable() {
        return !isString();
    }

    /**
     * If this instance is a table that contains the given key, return the value of
     * that key. Otherwise, returns an empty optional.
     * 
     * @param key
     * @return
     */
    public abstract Optional<KvValue> get(String key);

    /**
     * If this instance is a table that contains a String value associated with the
     * given key, return the value of that key. Otherwise, returns an empty
     * optional.
     * 
     * @param key
     * @return
     */
    public abstract Optional<String> getString(String key);

    /**
     * If this instance is a table that contains a Table value associated with the
     * given key, return the value of that key. Otherwise, returns an empty
     * optional.
     * 
     * @param key
     * @return
     */
    public abstract Optional<KvTable> getTable(String key);

    /**
     * Returns a view of this object as an optional String. For tables, the optional
     * will be empty.
     * 
     * @return
     */
    public abstract Optional<String> asString();

    /**
     * Returns a view of this object as an optional ValveDataTable. For Strings, the
     * optional will be empty.
     * 
     * @return
     */
    public abstract Optional<KvTable> asTable();

    /**
     * Returns a map, treating string values as empty maps.
     * 
     * @return
     */
    public abstract Map<String, KvValue> asMap();

    /**
     * A stream of Entry items if this is a table, or an empty stream if this is a
     * string.
     * 
     * @return
     */
    public abstract Stream<Entry<String, KvValue>> streamEntries();

    /**
     * A stream of String keys if this is a table, or an empty stream if this is a
     * string.
     * 
     * @return
     */
    public abstract Stream<String> streamKeys();

    /**
     * If this is a table returns a stream of this table's values. If this is a
     * string, returns an empty stream.
     * 
     * @return
     */
    public abstract Stream<KvValue> streamValues();

    /**
     * {@code getTable(key)?.streamKeys()}
     * 
     * @param key
     * @return
     */
    public abstract Stream<String> streamKeysOf(String key);

    /**
     * {@code getTable(key)?.streamValues()}
     * 
     * @param key
     * @return
     */
    public abstract Stream<KvValue> streamValuesOf(String key);

    static String escapeString(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }

    public abstract String toJson();
}