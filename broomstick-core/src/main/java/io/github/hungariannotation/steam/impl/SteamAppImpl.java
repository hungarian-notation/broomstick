package io.github.hungariannotation.steam.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import io.github.hungariannotation.steam.SteamApp;
import io.github.hungariannotation.steam.kv.KvParser;

public class SteamAppImpl implements SteamApp {

    private final String appId;
    private final String name;
    private final String buildId;
    private final Path installDir;
    private final Path libraryPath;
    private Path manifestPath;
    private final Memoized<Optional<WorkshopImpl>> workshop = Memoized.of(this::computeWorkshop);

    public SteamAppImpl(Path libraryPath, Path manifestPath) throws IllegalArgumentException, IOException {
        var table = KvParser.read(manifestPath)
                .orElseThrow(() -> new IllegalArgumentException("bad manifest path: " + manifestPath.toString()));
        var root = table.getTable("AppState").orElseThrow(() -> new IllegalArgumentException("not an appmanifest"));
        this.manifestPath = manifestPath;
        this.libraryPath = libraryPath;
        this.name = root.getString("name").orElse("");
        this.appId = root.getString("appid").orElse("");
        this.buildId = root.getString("buildid").orElse("");
        this.installDir = root.getString("installdir")
                .map(dir -> libraryPath.resolve("steamapps", "common").resolve(dir)).orElseThrow();
    }

    @Override
    public String getId() {
        return appId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Path getInstallPath() {
        return installDir;
    }

    @Override
    public Path getLibraryPath() {
        return libraryPath;
    }

    @Override
    public String getBuildId() {
        return buildId;
    }

    public Optional<Path> getWorkshopManifestPath() {

        Path value = getLibraryPath()
                .resolve("steamapps", "workshop", String.format("appworkshop_%s.acf", getId()));
        return Optional.of(value)
                .filter(Files::isRegularFile);
    }

    @Override
    public Optional<WorkshopImpl> getWorkshop() {
        return workshop.get();
    }

    @Override
    public Path getManifestPath() {
        return manifestPath;
    }

    private Optional<WorkshopImpl> computeWorkshop() {
        return WorkshopImpl.read(this, getWorkshopManifestPath());
    }

}
