package io.github.hungariannotation.steam.webapi;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import io.github.hungariannotation.steam.PublishedFileDetails;
import jakarta.json.Json;
import jakarta.json.JsonValue;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

/**
 * 
 */
public class SteamApiClient implements AutoCloseable {

    private static final String REMOTESTORAGE_GETDETAILS = 
    "https://api.steampowered.com/ISteamRemoteStorage/GetPublishedFileDetails/v1/";

    private boolean closed = false;
    private final HttpClient client;

    public SteamApiClient(HttpClient client) {
        this.client = client;
    }

    public SteamApiClient() {
        this.client = HttpClient.newBuilder().version(Version.HTTP_1_1)
                .followRedirects(Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                .build();
    }

    @Override
    public void close() {
        if (!closed) {
            closed = true;
            client.close();
        }
    }

    /**
     * Request details about workshop items from the Steam API.
     * 
     * @param ids
     * @return
     */
    public CompletableFuture<List<PublishedFileDetails>> getPublishedFileDetails(List<String> ids) {
        var future = client.sendAsync(getPublishedFileDetailsRequest(ids), BodyHandlers.ofInputStream());
        return future.thenApply(this::toJson).thenApply(SteamApiClient::parsePublishedFileDetails);
    }

    public CompletableFuture<Optional<PublishedFileDetails>> getPublishedFileDetails(String id) {
        var future = client.sendAsync(getPublishedFileDetailsRequest(List.of(id)), BodyHandlers.ofInputStream());
        return future.thenApply(this::toJson).thenApply(SteamApiClient::parsePublishedFileDetails)
                .thenApply(list -> list.stream().findAny());
    }

    protected SteamApiRequestBuilder request(URI uri) {
        return new SteamApiRequestBuilder(uri);
    }

    private HttpRequest getPublishedFileDetailsRequest(List<String> publishedFileIds) {
        return request(URI.create(REMOTESTORAGE_GETDETAILS))
                .array("itemcount", "publishedfileids", publishedFileIds)
                .build();
    }

    private static List<PublishedFileDetails> parsePublishedFileDetails(JsonValue jsonValue) {
        Jsonb jsonb = JsonbBuilder.create();
        var resultArray = jsonValue.asJsonObject()
                .getJsonObject("response") // we want to throw an exception here instead of some optional stunt
                .getJsonArray("publishedfiledetails");
        var results = resultArray.stream().map(resultValue -> {
            return jsonb.fromJson(resultValue.toString(), PublishedFileDetails.class);
        }).toList();
        return results;
    }

    private JsonValue toJson(HttpResponse<InputStream> response) {
        int statusCode = response.statusCode();
        return switch (statusCode) {
            case 200 -> {
                yield Json.createReader(response.body()).readValue();
            }
            default -> {
                try {
                    String result = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
                    throw new RuntimeException(String.format("Status: %d: %s", statusCode, result));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        };
    }
}
