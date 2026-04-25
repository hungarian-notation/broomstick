package io.github.hungariannotation.steam;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface WorkshopItem {

    String getId();

    String getTimeTouched();

    String getTimeUpdatedLocal();

    String getTimeUpdatedRemote();

    String getManifestLocal();

    String getManifestRemote();

    /**
     * Asynchronously retrieves data about the workshop item from Steam's servers.
     * 
     * @return
     */
    CompletableFuture<Optional<PublishedFileDetails>> getDetailsAsync();

    /**
     * Like {@link #getDetailsAsync()}, but blocks until the request to
     * the API resolves.
     * 
     * @return
     */
    Optional<PublishedFileDetails> getDetails();

}