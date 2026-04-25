package io.github.hungariannotation.steam;

import java.net.URL;
import java.time.Instant;
import java.util.List;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.bind.annotation.JsonbProperty;

/**
 * Binding to a response from Steam's
 * ISteamRemoteStorage::GetPublishedFileDetails API
 */
public class PublishedFileDetails {

    public PublishedFileDetails() {

    }

    @JsonbProperty("publishedfileid")
    private String id;

    @JsonbProperty("creator")
    private String creator;

    @JsonbProperty("consumer_app_id")
    private String appid;

    @JsonbProperty("title")
    private String title;

    @JsonbProperty("preview_url")
    private URL previewUrl;

    @JsonbProperty("description")
    private String description;

    @JsonbProperty("time_created")
    private String timeCreatedString;

    @JsonbProperty("time_updated")
    private String timeUpdatedString;

    @JsonbProperty("tags")
    private JsonArray tagsJson;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public URL getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(URL previewUrl) {
        this.previewUrl = previewUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getTimeCreated() {
        return Instant.ofEpochSecond(Long.parseLong(timeCreatedString));
    }

    public void setTimeCreatedString(String timeCreatedString) {
        this.timeCreatedString = timeCreatedString;
    }

    public String getTimeUpdatedString() {
        return timeUpdatedString;
    }

    public Instant getTimeUpdated() {
        return Instant.ofEpochSecond(Long.parseLong(timeUpdatedString));
    }

    public void setTimeUpdatedString(String timeUpdatedString) {
        this.timeUpdatedString = timeUpdatedString;
    }

    JsonArray getTagsJson() {
        return tagsJson;
    }

    void setTagsJson(JsonArray tags) {
        this.tagsJson = tags;
    }

    public String getTimeCreatedString() {
        return timeCreatedString;
    }

    public List<String> getTags() {
        return getTagsJson().getValuesAs((JsonObject obj) -> obj.getString("tag"));
    }

    @Override
    public String toString() {
        return "WorkshopDetails [getId()=" + getId() + ", getCreator()=" + getCreator() + ", getAppid()="
                + getAppid() + ", getTitle()=" + getTitle() + ", getPreviewUrl()=" + getPreviewUrl()
                + ", getTimeCreated()=" + getTimeCreated() + ", getTimeUpdated()=" + getTimeUpdated()
                + ", getTags()=" + getTagsJson() + "]";
    }

}