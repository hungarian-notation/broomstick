package io.github.hungariannotation.util;

/**
 * Even though the {@link String#hashCode()} javadoc defines the algorithm used
 * to calculate hash codes, there is no positive affirmation that this is a
 * fixed part of the specification. Since I'm using hash codes here in a way
 * that expects them to be fixed for the same input strings, I'm including a
 * fixed implementation of hashCode. As far as I can tell this produces the same
 * result as the Java SE API on any modern JVM, but my version is guaranteed to
 * stay that way.
 */
public class StableHash {

    public static int hashCode(int result, char[] a, int fromIndex, int length) {
        int end = fromIndex + length;
        for (int i = fromIndex; i < end; i++) {
            result = 31 * result + a[i];
        }
        return result;
    }

    public static int hashCode(int result, CharSequence a, int fromIndex, int length) {
        int end = fromIndex + length;
        for (int i = fromIndex; i < end; i++) {
            result = 31 * result + a.charAt(i);
        }
        return result;
    }

    public static int hashCode(int result, CharSequence a) {
        return hashCode(result, a, 0, a.length());
    }

    public static int hashCode(CharSequence a) {
        return hashCode(0, a);
    }

}
