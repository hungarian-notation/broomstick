package io.github.hungariannotation.steam.webapi;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * Steam Web API requests must send arguments as
 * application/x-www-form-urlencoded POST data. This helper class wraps a
 * {@link HttpRequest.Builder} instance, allowing arguments to be specified with
 * the {@link #field(String, String)} method.
 * <p>
 * When {@link #build()} is called, all fields you have specified are encoded
 * and added to the request via
 * {@link HttpRequest.Builder#POST(java.net.http.HttpRequest.BodyPublisher)}
 */
public class SteamApiRequestBuilder {
    private static final Duration requestTimeout = Duration.ofSeconds(10);
    private static final Charset CHARSET = Objects.requireNonNull(StandardCharsets.UTF_8);

    private static final String urlEncode(String content) {
        return URLEncoder.encode(content, CHARSET);
    }

    private final HttpRequest.Builder base;
    private final LinkedHashMap<String, String> fields;

    public SteamApiRequestBuilder(Builder base) {
        this.base = base;
        this.fields = new LinkedHashMap<>();
    }

    public SteamApiRequestBuilder(URI uri) {
        this(HttpRequest
                .newBuilder(uri)
                .timeout(requestTimeout)
                .header("Content-Type", "application/x-www-form-urlencoded"));
    }

    public SteamApiRequestBuilder header(String name, String value) {
        base.header(name, value);
        return this;
    }

    public SteamApiRequestBuilder timeout(Duration duration) {
        base.timeout(duration);
        return this;
    }

    public HttpRequest build() {
        var builder = new StringBuilder();
        var first = true;

        while (!fields.isEmpty()) {
            var entry = fields.pollFirstEntry();

            if (!first) {
                builder.append("&");
            } else {
                first = false;
            }

            builder.append(urlEncode(entry.getKey()));
            builder.append("=");
            builder.append(urlEncode(entry.getValue()));
        }

        base.POST(BodyPublishers.ofString(builder.toString(), CHARSET));
        return base.build();
    }

    public SteamApiRequestBuilder field(String key, Object value) {
        this.fields.put(key, Objects.requireNonNull(value).toString());
        return this;
    }

    public SteamApiRequestBuilder array(String countName, String arrayName, List<? extends Object> values) {
        this.field(countName, Integer.toString(values.size()));
        for (int i = 0; i < values.size(); ++i)
            this.field(String.format("%s[%d]", arrayName, i), values.get(i));
        return this;
    }

}