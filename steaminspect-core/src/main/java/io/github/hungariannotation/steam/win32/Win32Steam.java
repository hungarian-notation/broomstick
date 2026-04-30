package io.github.hungariannotation.steam.win32;

import java.nio.file.Path;
import java.util.Optional;

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

    private boolean gotPath = false;
    private Path steamPath;

    @Override
    public Optional<Path> getSteam() {
        if (!gotPath) {
            var value = Registry.getRegistryValue(RegistryHiveKey.HKEY_LOCAL_MACHINE,
                    "SOFTWARE\\WOW6432Node\\Valve\\Steam",
                    "InstallPath").join().flatMap(v -> {
                        if (v.value() instanceof String string)
                            return Optional.of(string);
                        return Optional.empty();
                    });
            steamPath = value.map(Path::of).orElse(null);
            gotPath = true;
            return Optional.of(steamPath);
        }

        return Optional.of(steamPath);
    }

}
