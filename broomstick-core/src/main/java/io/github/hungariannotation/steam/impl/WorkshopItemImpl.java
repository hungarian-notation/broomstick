package io.github.hungariannotation.steam.impl;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import io.github.hungariannotation.steam.PublishedFileDetails;
import io.github.hungariannotation.steam.WorkshopItem;

public class WorkshopItemImpl implements WorkshopItem {

    private final WorkshopImpl workshop;
    private final String id;
    private final String timeTouched;
    private final String timeUpdatedLocal;
    private final String timeUpdatedRemote;
    private final String manifestLocal;
    private final String manifestRemote;

    record ManifestData(String id, String timeTouched, String timeUpdatedLocal,
            String timeUpdatedRemote, String manifestLocal, String manifestRemote) {
    }

    public WorkshopItemImpl(WorkshopImpl workshop, ManifestData data) {
        this.workshop = workshop;
        this.id = data.id;
        this.timeTouched = data.timeTouched;
        this.timeUpdatedLocal = data.timeUpdatedLocal;
        this.timeUpdatedRemote = data.timeUpdatedRemote;
        this.manifestLocal = data.manifestLocal;
        this.manifestRemote = data.manifestRemote;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getTimeTouched() {
        return timeTouched;
    }

    @Override
    public String getTimeUpdatedLocal() {
        return timeUpdatedLocal;
    }

    @Override
    public String getTimeUpdatedRemote() {
        return timeUpdatedRemote;
    }

    @Override
    public String getManifestLocal() {
        return manifestLocal;
    }

    @Override
    public String getManifestRemote() {
        return manifestRemote;
    }

    
    @Override
    public CompletableFuture<Optional<PublishedFileDetails>> getDetailsAsync() {
        return workshop.getDetailsAsync(this);
    }
    
    @Override
    public Optional<PublishedFileDetails> getDetails() {
        return getDetailsAsync().join();
    }

}
