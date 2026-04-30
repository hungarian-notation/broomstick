package io.github.hungariannotation.steam.impl;

import io.github.hungariannotation.steam.Steam;
import io.github.hungariannotation.steam.win32.Registry;
import io.github.hungariannotation.steam.win32.Win32Steam;

public class SteamLibraryNative {
    private SteamLibraryNative() {
    }

    private static Steam instance;

    /**
     * Gets an appropriate platform-specific implementation of SteamInstallation.
     * 
     * @return
     */
    public static Steam getInstance() {
        if (instance == null) {
            if (Registry.isAvailable()) {
                instance = new Win32Steam();
            } else {
                instance = new FallbackSteam();
            }
        }

        return instance;
    }
}
