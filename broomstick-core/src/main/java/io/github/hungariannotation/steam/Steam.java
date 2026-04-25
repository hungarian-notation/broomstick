package io.github.hungariannotation.steam;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import io.github.hungariannotation.steam.impl.BaseSteamLibrary;
import io.github.hungariannotation.steam.impl.SteamLibraryNative;

public interface Steam {

    /**
     * Gets an appropriate plaform-specific implemenation of SteamInstallation.
     * 
     * @return
     */
    public static Steam getInstance() {
        return SteamLibraryNative.getInstance();
    }

    /**
     * Returns a SteamLibrary interface for the steam installation at a given path.
     * 
     * @param path
     * @return
     */
    public static Steam getInstance(Path path) {
        return new BaseSteamLibrary() {
            @Override
            public Optional<Path> getSteam() {
                return Optional.ofNullable(path);
            }
        };
    }

    /**
     * Get the main steam library folder. The Windows implementation retrieves this
     * programatically from the registry, while the fallback implementation searches
     * various paths Steam may be installed on different systems.
     * 
     * @return
     */
    Optional<Path> getSteam();

    /**
     * {@code Steam/steamapps/libraryfolders.vdf} contains a record for every Steam
     * library folder and a list of the appids of the games installed there.
     * 
     * The Steam directory we're under is one of them, but there may be others.
     * 
     * @return
     */
    List<? extends SteamLibraryFolder> getLibraryFolders();

    /**
     * Get a list of all installed Steam apps.
     * 
     * @return
     */
    List<SteamApp> getApps();

    /**
     * Get a specific Steam app, or an empty optional if it is not installed.
     * 
     * @param id
     * @return
     */
    Optional<SteamApp> getApp(String id);

}