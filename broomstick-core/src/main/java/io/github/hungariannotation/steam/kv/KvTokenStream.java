package io.github.hungariannotation.steam.kv;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;

import io.github.hungariannotation.steam.kv.KvToken.*;

/**
 * A stream of tokens for consumption by the parser.
 */
public class KvTokenStream implements Closeable {

    // These are all the non-whitespace codepoints that control tokenization.

    private static final char OPEN_BRACE = '{';
    private static final char CLOSE_BRACE = '}';
    private static final char QUOTE = '"';
    private static final char SLASH = '/';
    private static final char BACKSLASH = '\\';
    private static final char NEWLINE = '\n';

    /**
     * Returned by the input
     */
    static final int SIGNAL_EOF = -1;

    /**
     * Parses the characters in the buffer from 0 to {@link CharBuffer#position()}
     * as an integer with the given radix, then returns as String of that value
     * interpreted as a unicode codepoint.
     * 
     * @param buffer
     * @param radix
     * @return
     */
    private static String parseInt(CharBuffer buffer, int radix) {
        int length = buffer.position();
        buffer.rewind();
        int codepoint = Integer.parseInt(buffer, 0, length, radix);
        return Character.toString(codepoint);
    }

    /**
     * render symbols as strings for error messages
     * 
     * @param codepoint
     * @return
     */
    private static String format(int codepoint) {
        if (codepoint == SIGNAL_EOF) {
            return "EOF";
        } else if (codepoint < 32) {
            return String.format("0x%x", codepoint);
        } else {
            return "'" + Character.valueOf((char) codepoint) + "'";
        }
    }

    /**
     * Whitespace check optimized for ASCII, as it is unlikely that computer
     * generated metadata files will contain non-ascii whitespace outside of
     * strings.
     * <p>
     * I do not believe there is a published specification for VDF files, so we are
     * unaware as to whether or not all unicode whitespace is valid. I error here on
     * the side of caution and resilience, as any weird space characters we'd find
     * outside of strings would otherwise be errors.
     * 
     * @param codepoint
     * @return
     */
    private static boolean isWhitespace(int codepoint) {
        if (codepoint < 0x80) {
            if (0x09 <= codepoint && codepoint <= 0x0D)
                // 9 <= codepoint <= 13
                // \t, \n, {vertical tab}, {form feed}, \r
                return true;
            return codepoint == ' ';
        } else {
            return Character.isWhitespace(codepoint);
        }
    }

    private static boolean isOctal(int c) {
        return (c >= '0') && (c <= '8');
    }

    private static boolean isHex(int c) {
        return (c >= '0') && (c <= '9') ||
                (c >= 'a') && (c <= 'f') ||
                (c >= 'A') && (c <= 'F');
    }

    private LineNumberReader rd;
    private int nextCodePoint = -2;
    private int column = -1;

    /**
     * used in escape sequence processing to hold the contents of multi-character
     * escapes before they are converted to codepoints.
     */
    private final CharBuffer escapeBuffer = CharBuffer.allocate(8);

    public KvTokenStream(LineNumberReader rd) {
        this.rd = rd;
    }

    public KvTokenStream(Reader rd) {
        this(new LineNumberReader(rd));
    }

    public KvTokenStream(InputStream in) {
        this(new InputStreamReader(in, StandardCharsets.UTF_8));
    }

    public KvTokenStream(String source) {
        this(new StringReader(source));
    }

    // /**
    // * Create an exception describing an unexpected codepoint at the reader's
    // * current line.
    // *
    // * @param codepoints
    // * @return
    // * @throws IOException
    // */
    // private RuntimeException expected(int... codepoints) throws IOException {
    // var formatted = Arrays.stream(codepoints)
    // .mapToObj(i -> format(i))
    // .collect(Collectors.joining(", "));
    // return expected(formatted);
    // }

    // private RuntimeException expected(String what) throws IOException {
    // return new RuntimeException(
    // String.format("expected %s at line %d column %d (was %s)", what,
    // rd.getLineNumber(), column,
    // format(nextCodePoint)));
    // }

    public KvToken read() throws IOException {
        if (column < 0)
            advance(); // read first codepoint

        while (true) {
            if (nextCodePoint == SLASH) {
                consumeComment();
                continue;
            }

            if (isWhitespace(nextCodePoint)) {
                consume(nextCodePoint);
                continue;
            }

            break;
        }

        if (nextCodePoint == SIGNAL_EOF) {
            return KvToken.of(TokenType.EOF, rd.getLineNumber(), column);
        } else if (nextCodePoint == QUOTE) {
            return KvToken.of(consumeString(), rd.getLineNumber(), column);
        } else if (nextCodePoint == CLOSE_BRACE) {
            consume(CLOSE_BRACE);
            return KvToken.of(TokenType.CLOSE, rd.getLineNumber(), column);
        } else if (nextCodePoint == OPEN_BRACE) {
            consume(OPEN_BRACE);
            return KvToken.of(TokenType.OPEN, rd.getLineNumber(), column);
        } else {
            return KvToken.of(consumeIdentifier(), rd.getLineNumber(), column);
        }

    }

    /**
     * Consumes the given input character, throwing an exception if the next
     * character is not the expected character.
     * 
     * @param expected
     * @return
     * @throws IOException
     */
    private int consume(int expected) throws IOException {
        if (nextCodePoint != expected) {
            throw new RuntimeException(
                    String.format("expected %s found %s", format(expected), format(nextCodePoint)));
        } else {
            return consume();
        }
    }

    /**
     * Updates {@link #nextCodePoint} to the next value.
     * 
     * @throws IOException
     */
    private void advance() throws IOException {
        if (nextCodePoint == SIGNAL_EOF)
            return;

        // Read in the next Unicode codepoint. This requires two reads
        // if the next code unit is a high surrogate.

        int ch = rd.read();

        if (ch >= 0 && Character.isHighSurrogate((char) ch)) {
            int low = rd.read();
            nextCodePoint = Character.toCodePoint((char) ch, (char) low);
        } else {
            nextCodePoint = ch;
        }

        if (nextCodePoint == '\n') {
            column = 0;
        } else {
            column += 1;
        }
    }

    private int consume() throws IOException {
        var value = nextCodePoint;
        advance();
        return value;
    }

    private void consumeComment() throws IOException {
        consume(SLASH);
        consume(SLASH);
        while (nextCodePoint != NEWLINE)
            consume(nextCodePoint);
        consume(NEWLINE);
    }

    private String consumeString() throws IOException {
        consume(QUOTE);
        StringBuilder builder = new StringBuilder();
        while (nextCodePoint != QUOTE) {
            if (nextCodePoint == BACKSLASH) {
                builder.append(consumeEscapeSequence());
            } else {
                builder.appendCodePoint(consume(nextCodePoint));
            }
        }
        consume(QUOTE);
        return builder.toString();
    }

    private String consumeIdentifier() throws IOException {
        StringBuilder builder = new StringBuilder();
        while (!(nextCodePoint == SIGNAL_EOF || isWhitespace(nextCodePoint))) {
            if (nextCodePoint == BACKSLASH) {
                builder.append(consumeEscapeSequence());
            } else {
                builder.appendCodePoint(consume(nextCodePoint));
            }
        }
        return builder.toString();
    }

    /**
     * Without an official specification, I defer to a permissive extension of C's
     * definition of an escape sequence.
     * 
     * @param next
     * @return
     * @throws IOException
     */
    private String consumeEscapeSequence() throws IOException {
        consume(BACKSLASH);
        int next = consume(nextCodePoint);

        if (isOctal(next)) {
            escapeBuffer.clear();
            do {
                escapeBuffer.append((char) consume(nextCodePoint));
            } while (isOctal(nextCodePoint) && escapeBuffer.position() < 3);
            return parseInt(escapeBuffer, 8);
        } else if (next == 'x') {
            escapeBuffer.clear();
            while (isHex(nextCodePoint))
                escapeBuffer.append((char) consume(nextCodePoint));
            return parseInt(escapeBuffer, 16);
        } else if (next == 'u') {
            escapeBuffer.clear();
            while (escapeBuffer.position() < 4 && isHex(nextCodePoint))
                escapeBuffer.append((char) consume(nextCodePoint));
            return parseInt(escapeBuffer, 16);
        } else if (next == 'U') {
            escapeBuffer.clear();
            while (escapeBuffer.position() < 8 && isHex(nextCodePoint)) {
                escapeBuffer.append((char) consume(nextCodePoint));
            }
            return parseInt(escapeBuffer, 16);
        }

        return switch (next) {
            case 'a' -> "\u0007"; // Alert (Bell)
            case 'b' -> "\b"; // Backspace
            case 'e' -> "\u001B"; // Escape
            case 'f' -> "\f"; // Formfeed
            case 'n' -> "\n"; // Newline
            case 'r' -> "\r"; // Carriage Return
            case 't' -> "\t"; // Horizontal Tab
            case 'v' -> "\u000b"; // Vertical Tab
            default -> Character.toString(next);
        };
    }

    public boolean eof() {
        return nextCodePoint == SIGNAL_EOF;
    }

    @Override
    public void close() throws IOException {
        rd.close();
    }
}