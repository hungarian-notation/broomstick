package io.github.hungariannotation.steam.kv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import io.github.hungariannotation.steam.kv.KvToken.*;

import static java.util.Map.Entry;

/**
 * Contains various static methods that transform inputs in different forms to
 * KeyValue Syntax Trees.
 * <p>
 * By default, this class uses {@link KvTable} and {@link KvString} instances to
 * encode the AST, but you may specify alternative factory functions to
 * {@link #parse(Reader, Function, Function)} or
 * {@link #parse(KvTokenStream, Function, Function)} to define your own AST
 * representation.
 * <p>
 * For example, I use these factory functions in the tests to encode the AST in
 * a LISP-like string format:
 * 
 * <pre>
 * {@code
 * static String lispString(String value) {
 *     return String.format("'%s'", value);
 * }
 *
 * static String lispCons(List<Map.Entry<String, String>> values) {
 *     return values.stream()
 *             .map(each -> List.of(String.format("'%s'", each.getKey()), each.getValue())
 *                     .stream()
 *                     .collect(Collectors.joining(" ", "(", ")")))
 *             .collect(Collectors.joining(" "));
 * }
 * }
 * </pre>
 * 
 */
public class KvParser {

    private KvParser() {
    }

    private record Frame<E>(String key, LinkedList<Entry<String, E>> entries) {
        private Frame(String key) {
            this(key, new LinkedList<>());
        }
    }

    /**
     * Read a kv/vdf document from the given path, using the default AST types.
     * 
     * @param path
     * @return
     */
    public static Optional<KvValue> read(Path path) {
        return read(Optional.of(path));
    }

    /**
     * 
     * 
     * @param path
     * @return
     */
    public static Optional<KvValue> read(Optional<Path> path) {
        return path.filter(Files::isRegularFile)
                .flatMap(validpath -> {
                    try {
                        return Optional.of(
                                parse(Files.newBufferedReader(validpath),
                                        KvString::new,
                                        KvTable::new));
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
    }

    /**
     * Generic parser that reads from a path via the {@link java.nio.file} API.
     * Acepts user-defined AST factory functions,
     * 
     * @param <V>
     * @param <T>
     * @param path
     * @param constructString
     * @param constructTable
     * @return
     */
    public static <V, T extends V> Optional<T> read(Optional<Path> path,
            Function<String, ? extends V> constructString,
            Function<List<Map.Entry<String, V>>, ? extends T> constructTable) {
        return path
                .filter(Files::isRegularFile)
                .flatMap(validPath -> {
                    BufferedReader reader;
                    try {
                        reader = Files.newBufferedReader(validPath);
                        T result = parse(reader, constructString, constructTable);
                        return Optional.of(result);
                    } catch (IOException e) {
                        return Optional.empty();
                    }
                });

    }

    /**
     * Parses the string using the built-in AST types.
     * 
     * @see #parse(Reader)
     * @param string
     * @return
     * @throws IOException
     */
    public static KvValue parse(String string) throws IOException {
        return parse(string, KvString::new, KvTable::new);
    }

    /**
     * Parses from the given reader using the built-in AST types.
     * 
     * @param reader
     * @return
     * @throws IOException
     */
    public static KvValue parse(Reader reader) throws IOException {
        return parse(reader, KvString::new, KvTable::new);
    }

    /**
     * Generic parser accepting user-defined AST factory functions.
     * <p>
     * A default AST mapping is provided via {@link #parse(Reader)}
     * 
     * @param <V>             The type that can hold either string values or tables.
     * @param <T>             The subclass of V that holds tables.
     * @param string          The input as a String.
     * @param constructString A function that maps Strings to {@link V}
     * @param constructTable  A function that maps sequences of key-value pairs to
     *                        {@link T}
     * @return The root table of the VDF document.
     * @throws IOException
     */
    public static <E, V extends E> V parse(String string, Function<String, ? extends E> scalarMapper,
            Function<List<Map.Entry<String, E>>, ? extends V> vectorMapper) throws IOException {
        return parse(new StringReader(string), scalarMapper, vectorMapper);
    }

    /**
     * Generic parser accepting user-defined AST factory functions.
     * <p>
     * A default AST mapping is provided via {@link #parse(Reader)}
     * 
     * @param <V>             The type that can hold either string values or tables.
     * @param <T>             The subclass of V that holds tables.
     * @param reader
     * @param constructString A function that maps Strings to {@link V}
     * @param constructTable  A function that maps sequences of key-value pairs to
     *                        {@link T}
     * @return The root table of the VDF document.
     * @throws IOException
     */
    public static <V, T extends V> T parse(
            Reader reader,
            Function<String, ? extends V> constructString,
            Function<List<Map.Entry<String, V>>, ? extends T> constructTable) throws IOException {
        try (var tokenizer = new KvTokenStream(reader)) {
            return parse(tokenizer, constructString, constructTable);
        }
    }

    /**
     * Generic parser accepting user-defined AST factory functions.
     * <p>
     * A default AST mapping is provided via {@link #parse(Reader)}
     * 
     * @param <V>             The type that can hold either string values or tables.
     * @param <T>             The subclass of V that holds tables.
     * @param tokenizer       The token stream.
     * @param constructString A function that maps Strings to {@link V}
     * @param constructTable  A function that maps sequences of key-value pairs to
     *                        {@link T}
     * @return The root table of the VDF document.
     * @throws IOException
     */
    public static <V, T extends V> T parse(
            KvTokenStream tokenizer,
            Function<String, ? extends V> constructString,
            Function<List<Map.Entry<String, V>>, ? extends T> constructTable) throws IOException {

        LinkedList<Frame<V>> stack = new LinkedList<>();
        stack.add(new Frame<>(null));

        while (true) {
            var token = tokenizer.read();
            switch (token) {
                case KvOpenToken _ -> throw new RuntimeException(
                        "open token in a location where only key or close tokens are valid");

                case KvStringToken key -> {
                    var value = tokenizer.read();

                    switch (value) {
                        case KvStringToken stringValue -> {
                            stack.peek().entries()
                                    .add(Map.entry(key.value, constructString.apply(stringValue.value)));
                        }
                        case KvOpenToken _ -> stack.push(new Frame<>(key.value));
                        case KvCloseToken _ -> throw new RuntimeException(
                                "unexpected close brace after key; expected string or open brace");
                        case KvObjectTerminalToken _ -> throw new RuntimeException(
                                "unexpected EOF");
                    }
                }

                case KvObjectTerminalToken terminal -> {
                    var frame = stack.pop();
                    T frameValue = constructTable.apply(frame.entries);

                    switch (terminal) {
                        case KvCloseToken _ -> {
                            if (stack.isEmpty())
                                throw new RuntimeException("unbalanced braces");
                            var frameKey = Objects.requireNonNull(frame.key);
                            stack.peek().entries.add(Map.entry(frameKey, frameValue));
                        }

                        case KvEOF _ -> {
                            if (!stack.isEmpty())
                                throw new RuntimeException("unbalanced braces; unexpected EOF");
                            assert frame.key == null;
                            return frameValue;
                        }

                    }
                }
            }
            assert token.type() != TokenType.EOF; // visions of infinite looping
        }
    }
}
