package com.dsatracker.util;

import com.dsatracker.exception.ValidationException;

import java.net.URI;
import java.net.URISyntaxException;

/** Fail-fast input checks shared by every service, so validation rules live in one place. */
public final class Validator {

    private Validator() {
        throw new AssertionError("Validator is a static utility class and cannot be instantiated.");
    }

    public static void requireNonBlank(final String value, final String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " cannot be empty.");
        }
    }

    public static void requireInRange(final int value, final int min, final int max, final String fieldName) {
        if (value < min || value > max) {
            throw new ValidationException(fieldName + " must be between " + min + " and " + max + ".");
        }
    }

    public static void requirePositive(final int value, final String fieldName) {
        if (value <= 0) {
            throw new ValidationException(fieldName + " must be greater than zero.");
        }
    }

    /** URL is an optional field: null/blank is fine, but a present value must be a well-formed http(s) URL. */
    public static void requireValidUrlIfPresent(final String url, final String fieldName) {
        if (url == null || url.isBlank()) {
            return;
        }
        try {
            final URI uri = new URI(url);
            final String scheme = uri.getScheme();
            final boolean validScheme = scheme != null
                    && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"));
            if (!validScheme || uri.getHost() == null) {
                throw new ValidationException(fieldName + " must be a valid http(s) URL.");
            }
        } catch (final URISyntaxException e) {
            throw new ValidationException(fieldName + " must be a valid URL.");
        }
    }
}
