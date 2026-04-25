package io.github.hungariannotation.steam.impl;

import java.time.Instant;
import java.util.Optional;

/**
 * Helpers for converting Steam's timestamps to Instants.
 */
public class Timestamp {
    private Timestamp(){}

    public static Optional<Instant> getInstant(Optional<String> utcStringOpt) {
        return utcStringOpt.flatMap(Timestamp::getInstant);
    }

    public static Optional<Instant> getInstant(String utcString) {
        try {
            long seconds = Long.parseLong(utcString);
            return Optional.of(Instant.ofEpochSecond(seconds));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
