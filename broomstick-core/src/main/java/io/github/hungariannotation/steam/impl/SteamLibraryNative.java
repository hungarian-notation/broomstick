package io.github.hungariannotation.steam.impl;

import com.sun.jna.Platform;

import io.github.hungariannotation.steam.Steam;
import io.github.hungariannotation.steam.impl.win32.Win32Steam;

public class SteamLibraryNative {
    private SteamLibraryNative() {
    }

    private static Steam instance;

    /**
     * Gets an appropriate plaform-specific implemenation of SteamInstallation.
     * 
     * @return
     */
    public static Steam getInstance() {
        if (instance == null) {
            if (Platform.isWindows()) {
                instance = new Win32Steam();
            } else {
                instance = new FallbackSteam();
            }
        }

        return instance;
    }
}
