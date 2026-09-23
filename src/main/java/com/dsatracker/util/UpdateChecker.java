package com.dsatracker.util;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Asks GitHub whether a newer release than the running version exists. Purely informational:
 * the app never downloads or installs anything itself. Every failure (offline, rate limit,
 * private repo, unexpected payload) quietly yields "no update".
 */
public final class UpdateChecker {

    /** A newer release the user can download. */
    public record Update(String version, String pageUrl) { }

    private static final String LATEST_API =
            "https://api.github.com/repos/" + AppConstants.GITHUB_REPO + "/releases/latest";
    private static final Pattern TAG = Pattern.compile("\"tag_name\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern PAGE = Pattern.compile("\"html_url\"\\s*:\\s*\"([^\"]+/releases/tag/[^\"]+)\"");

    private UpdateChecker() {
        // Utility class.
    }

    /** Blocking network call - run it off the JavaFX thread. */
    public static Optional<Update> checkForUpdate() {
        try {
            final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
            final HttpRequest request = HttpRequest.newBuilder(URI.create(LATEST_API))
                    .timeout(Duration.ofSeconds(8))
                    .header("Accept", "application/vnd.github+json")
                    .build();
            final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200 ? parse(response.body(), AppConstants.APP_VERSION) : Optional.empty();
        } catch (final IOException | RuntimeException e) {
            return Optional.empty();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    /** Extracts the latest release from GitHub's JSON and returns it only if newer than {@code current}. */
    static Optional<Update> parse(final String json, final String current) {
        final Matcher tag = TAG.matcher(json);
        if (!tag.find() || !isNewer(tag.group(1), current)) {
            return Optional.empty();
        }
        final Matcher page = PAGE.matcher(json);
        final String url = page.find() ? page.group(1)
                : "https://github.com/" + AppConstants.GITHUB_REPO + "/releases/latest";
        return Optional.of(new Update(tag.group(1).replaceFirst("^[vV]", ""), url));
    }

    /** Numeric, dot-separated comparison ("v1.10.0" is newer than "1.9.2"); unparsable input counts as not newer. */
    static boolean isNewer(final String candidate, final String current) {
        try {
            final int[] a = numbers(candidate);
            final int[] b = numbers(current);
            for (int i = 0; i < Math.max(a.length, b.length); i++) {
                final int x = i < a.length ? a[i] : 0;
                final int y = i < b.length ? b[i] : 0;
                if (x != y) {
                    return x > y;
                }
            }
            return false;
        } catch (final NumberFormatException e) {
            return false;
        }
    }

    private static int[] numbers(final String version) {
        final String[] parts = version.trim().replaceFirst("^[vV]", "").split("\\.");
        final int[] result = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Integer.parseInt(parts[i]);
        }
        return result;
    }
}
