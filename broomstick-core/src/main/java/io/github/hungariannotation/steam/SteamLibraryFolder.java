package io.github.hungariannotation.steam;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public interface SteamLibraryFolder {

    /**
     * The root path of this library folder.
     * 
     * @return
     */
    Path getPath();

    /**
     * This library's label. May be an empty string. On Windows, that causes the UI
     * to use the drive's volume name.
     * 
     * @return
     */
    String getLabel();

    /** The steam apps in this library. */
    List<SteamApp> getApps();

    Optional<SteamApp> getApp(String id);

}