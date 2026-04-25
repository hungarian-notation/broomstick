package io.github.hungariannotation.steam.kv;

/**
 * The base unit operated on by the parse. Can be a String, an open or close
 * brace, or the special EOF token.
 */
public sealed interface KvToken {

    public static enum TokenType {
        STRING, OPEN, CLOSE, EOF;
    }

    TokenType type();

    public static KvToken of(TokenType type, int line, int column) {
        return switch (type) {
            case STRING -> throw new IllegalArgumentException();
            case OPEN -> new KvOpenToken(line, column);
            case CLOSE -> new KvCloseToken(line, column);
            case EOF -> new KvEOF(line, column);
            default -> throw new NullPointerException();
        };
    }

    public static KvToken of(String content, int line, int column) {
        return new KvStringToken(line, column, content);
    }

    static class BaseToken {

        final int line;
        final int column;

        public BaseToken(int line, int column) {
            this.line = line;
            this.column = column;
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName() + " []";
        }

    }

    /**
     * This includes the closing brace, but also EOF which implicitly closes the
     * root table.
     */
    public static sealed interface KvObjectTerminalToken extends KvToken {

    }

    /**
     * String token. Represents both quoted and unquoted strings. Quoted strings are
     * stored without their surrounding quotes.
     */
    public static final class KvStringToken extends BaseToken implements KvToken {
        public final String value;

        public KvStringToken(int line, int column, String value) {
            super(line, column);
            this.value = value;
        }

        @Override
        public TokenType type() {
            return TokenType.STRING;
        }

        @Override
        public String toString() {
            return "VdfStringToken [value=" + value + "]";
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
            KvStringToken other = (KvStringToken) obj;
            if (value == null) {
                if (other.value != null)
                    return false;
            } else if (!value.equals(other.value))
                return false;
            return true;
        }

    }

    /**
     * Open brace token.
     */
    public static final class KvOpenToken extends BaseToken implements KvToken {

        public KvOpenToken(int line, int column) {
            super(line, column);
        }

        @Override
        public TokenType type() {
            return TokenType.OPEN;
        }
    }

    /**
     * Close brace token.
     */
    public static final class KvCloseToken extends BaseToken implements KvObjectTerminalToken {

        public KvCloseToken(int line, int column) {
            super(line, column);
        }

        @Override
        public TokenType type() {
            return TokenType.CLOSE;
        }
    }

    /**
     * EOF indicator token.
     */
    public static final class KvEOF extends BaseToken implements KvObjectTerminalToken {

        public KvEOF(int line, int column) {
            super(line, column);
        }

        @Override
        public TokenType type() {
            return TokenType.EOF;
        }
    }

}
