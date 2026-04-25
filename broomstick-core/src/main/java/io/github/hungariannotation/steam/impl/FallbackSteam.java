package io.github.hungariannotation.steam.impl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * A token effort to find Linux Steam installations.
 */
public class FallbackSteam extends BaseSteamLibrary {

    public FallbackSteam() {
        super();
    }

    @Override
    public Optional<Path> getSteam() {
        var home = Optional.ofNullable(System.getProperty("user.home")).map(Path::of);

        return home.flatMap(h -> {
            var search = List.of(
                    ".steam/steam/",
                    ".local/share/Steam",
                    ".var/app/com.valvesoftware.Steam/.local/share/Steam",
                    ".wine/drive_c/Program Files (x86)/Steam",
                    "/mnt/c/Program Files (x86)/Steam");
            return search.stream().map(leaf -> h.resolve(leaf))
                    .filter(Files::isDirectory)
                    .map(Path::toAbsolutePath)
                    .findFirst();
        });
    }

}
