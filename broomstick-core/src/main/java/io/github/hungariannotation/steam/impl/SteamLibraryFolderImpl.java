package io.github.hungariannotation.steam.impl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import io.github.hungariannotation.steam.SteamApp;
import io.github.hungariannotation.steam.SteamLibraryFolder;

public class SteamLibraryFolderImpl implements SteamLibraryFolder {

    private final String pathString;
    private final String label;
    private final List<String> appids;

    public SteamLibraryFolderImpl(String pathString, String label, List<String> apps) {
        this.pathString = pathString;
        this.label = label;
        this.appids = apps;
    }

    @Override
    public Path getPath() {
        return Path.of(Objects.requireNonNull(pathString));
    }

    @Override
    public String getLabel() {
        return label;
    }

    private Path toManifestPath(String appid) {
        return getPath().resolve("steamapps", "appmanifest_" + appid + ".acf");
    }

    private Optional<SteamApp> toApp(Path mpath) {
        try {
            return Optional.of((SteamApp) new SteamAppImpl(getPath(), mpath));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public List<SteamApp> getApps() {
        return appids.stream()
                .map(this::getApp)
                .flatMap(Optional::stream)
                .toList();
    }

    private final HashMap<String, Optional<SteamApp>> appcache = new HashMap<>();

    @Override
    public Optional<SteamApp> getApp(String id) {
        if (appids.contains(id)) {
            return appcache.computeIfAbsent(id, ids -> toApp(toManifestPath(ids)));
        } else {
            return Optional.empty();
        }
    }

}