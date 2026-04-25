package io.github.hungariannotation.steam.impl.win32;

import java.nio.file.Path;
import java.util.Optional;

import com.sun.jna.platform.win32.WinReg;

import io.github.hungariannotation.steam.impl.BaseSteamLibrary;

/**
 * This implementation directly retrieves the steam installation directory from
 * the Windows registry. The base SteamInstallation can then follow the
 * libraryfolders.vdf and appmanifest_nnnnnn.acf files to any installed game.
 */
public class Win32Steam extends BaseSteamLibrary {

    public Win32Steam() {
        super();
    }

    private final WinRegPath STEAM_INSTALLPATH = new WinRegPath(WinReg.HKEY_LOCAL_MACHINE,
            "SOFTWARE\\WOW6432Node\\Valve\\Steam",
            "InstallPath");

    private boolean gotPath = false;
    private Path steamPath;

    @Override
    public Optional<Path> getSteam() {
        if (!gotPath) {
            steamPath = STEAM_INSTALLPATH.getString().map(Path::of).orElse(null);
            gotPath = true;
            return Optional.of(steamPath);
        }

        return Optional.of(steamPath);
    }

}
