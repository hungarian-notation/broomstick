package io.github.hungariannotation.steam.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import io.github.hungariannotation.steam.SteamApp;
import io.github.hungariannotation.steam.SteamLibraryFolder;
import io.github.hungariannotation.steam.Steam;
import io.github.hungariannotation.steam.kv.KvParser;
import io.github.hungariannotation.steam.kv.KvTable;
import io.github.hungariannotation.steam.kv.KvValue;

public abstract class BaseSteamLibrary implements Steam {

    public BaseSteamLibrary() {

    }

    private final Memoized<List<? extends SteamLibraryFolder>> memoizedFolders = Memoized
            .of(this::computeLibraryFolders);

    /**
     * Get the main steam library folder. The Windows implementation retrieves this
     * programatically from the registry, while the fallback implementation searches
     * various paths Steam may be installed on different systems.
     * 
     * @return
     */
    @Override
    public abstract Optional<Path> getSteam();

    /**
     * {@code Steam/steamapps/libraryfolders.vdf} contains a record for every Steam
     * library folder and a list of the appids of the games installed there.
     * 
     * The Steam directory we're under is one of them, but there may be others.
     * 
     * @return
     */
    @Override
    public List<? extends SteamLibraryFolder> getLibraryFolders() {
        return memoizedFolders.get();
    }

    @Override
    public List<SteamApp> getApps() {
        return getLibraryFolders().stream().flatMap(lib -> lib.getApps().stream()).toList();
    }

    @Override
    public Optional<SteamApp> getApp(String id) {
        return getLibraryFolders()
                .stream()
                .flatMap(folder -> folder.getApp(id).stream())
                .findAny();
    }

    private List<? extends SteamLibraryFolder> computeLibraryFolders() {
        var libraryFolderTable = getSteam().map(p -> p.resolve("steamapps/libraryfolders.vdf"))
                .filter(Files::isRegularFile)
                .map(this::parse)
                .flatMap(lib -> lib.getTable("libraryfolders"));

        return libraryFolderTable
                .stream()
                .flatMap(KvTable::streamValues)
                .map(each -> {
                    var pathOptional = each.getString("path");
                    var labelOptional = each.getString("label");
                    var apps = each.streamKeysOf("apps").toList();
                    var rec = pathOptional
                            .flatMap(path -> labelOptional.map(label -> new SteamLibraryFolderImpl(path, label, apps)));
                    return rec;
                }).flatMap(Optional::stream).toList();
    }

    private KvValue parse(Path path) {
        try (var reader = Files.newBufferedReader(path)) {
            return KvParser.parse(reader);
        } catch (IOException e) {
            throw new RuntimeException("error parsing kv file", e);
        }
    }

}
