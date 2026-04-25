package io.github.hungariannotation.steam.kv;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SequencedCollection;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The subclass of {@link KvValue} that holds table structures.
 */
public final class KvTable extends KvValue {

    private final Map<String, KvValue> table;

    public KvTable(SequencedCollection<Entry<String, KvValue>> vector) {
        Objects.requireNonNull(vector);
        var map = new LinkedHashMap<String, KvValue>();
        vector.stream().forEachOrdered(each -> map.put(each.getKey(), each.getValue()));
        this.table = Collections.unmodifiableMap(map);
    }

    public Set<String> keySet() {
        return table.keySet();
    }

    public Collection<KvValue> values() {
        return table.values();
    }

    @Override
    public boolean isString() {
        return false;
    }

    /**
     * If this instance is a table that contains the given key, return the value of
     * that key. Otherwise, returns an empty optional.
     * 
     * @param key
     * @return
     */
    @Override
    public Optional<KvValue> get(String key) {
        return Optional.ofNullable(table.get(key));
    }

    /**
     * If this instance is a table that contains a String value associated with the
     * given key, return the value of that key. Otherwise, returns an empty
     * optional.
     * 
     * @param key
     * @return
     */
    @Override
    public Optional<String> getString(String key) {
        return get(key).flatMap(KvValue::asString);
    }

    /**
     * If this instance is a table that contains a Table value associated with the
     * given key, return the value of that key. Otherwise, returns an empty
     * optional.
     * 
     * @param key
     * @return
     */
    @Override
    public Optional<KvTable> getTable(String key) {
        return get(key).flatMap(KvValue::asTable);
    }

    @Override
    public Optional<String> asString() {
        return Optional.empty();
    }

    @Override
    public Optional<KvTable> asTable() {
        return Optional.of(this);
    }

    @Override
    public Map<String, KvValue> asMap() {
        return table;
    }

    @Override
    public Stream<String> streamKeys() {
        return table.keySet().stream();
    }

    @Override
    public Stream<KvValue> streamValues() {
        return table.values().stream();
    }

    @Override
    public Stream<Entry<String, KvValue>> streamEntries() {
        return table.entrySet().stream();
    }

    public Stream<String> streamKeysOf(String key) {
        return getTable(key).stream().flatMap(KvTable::streamKeys);
    }

    public Stream<KvValue> streamValuesOf(String key) {
        return getTable(key).stream().flatMap(KvTable::streamValues);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((table == null) ? 0 : table.hashCode());
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
        KvTable other = (KvTable) obj;
        if (table == null) {
            if (other.table != null)
                return false;
        } else if (!table.equals(other.table))
            return false;
        return true;
    }

    @Override
    public String toJson() {
        return table.entrySet().stream()
                .map(entry -> String.format("\"%s\": %s", escapeString(entry.getKey()), entry.getValue().toJson()))
                .collect(Collectors.joining(", ", "{", "}"));
    }

    private static class Frame {
        List<Entry<String, KvValue>> entries;
        int entry;

        public Frame(KvTable table) {
            this.entries = table.streamEntries().toList();
            this.entry = 0;
        }

        public Entry<String, KvValue> nextEntry() {
            if (entry < entries.size())
                return entries.get(entry++);
            else
                return null;
        }

        public boolean done() {
            return entry >= entries.size();
        }
    }

    public static void doIndent(String indent, int amount, StringBuilder builder) {
        for (int i = 0; i < amount; ++i) {
            builder.append(indent);
        }
    }

    public String toString(String indent, String spacing) {
        StringBuilder builder = new StringBuilder();
        var stack = new LinkedList<Frame>();

        stack.push(new Frame(this));

        outer: while (!stack.isEmpty()) {
            while (!stack.peek().done()) {
                var entry = stack.peek().nextEntry();
                doIndent(indent, stack.size() - 1, builder);
                builder.append("\"");
                builder.append(escapeString(entry.getKey()));
                builder.append("\"");
                var value = entry.getValue();
                if (value instanceof KvString stringValue) {
                    builder.append(spacing);
                    builder.append("\"");
                    builder.append(escapeString(stringValue.value()));
                    builder.append("\"\n");
                } else if (value instanceof KvTable tableValue) {
                    builder.append("\n");
                    doIndent(indent, stack.size() - 1, builder);
                    builder.append("{\n");
                    stack.push(new Frame(tableValue));
                    continue outer;
                }
            }

            if (stack.size() > 1) {
                doIndent(indent, stack.size() - 2, builder);
                builder.append("}\n");
            }

            stack.pop();
        }

        return builder.toString();
    }

    public String toString() {
        return toString("\t", "\t\t");
    }
}