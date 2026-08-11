package com.dsatracker.util;

import java.time.format.DateTimeFormatter;

/**
 * Shared date/time formatting. SQLite's own {@code datetime('now')} writes
 * space-separated timestamps ({@code "2026-08-11 19:47:03"}), not Java's
 * default ISO {@code T}-separated format, so any column written by both
 * raw SQL (schema defaults, seed data) and repository code must use this
 * formatter to parse/format consistently.
 */
public final class DateTimeUtil {

    public static final DateTimeFormatter SQLITE_DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DateTimeUtil() {
        throw new AssertionError("DateTimeUtil is a static utility class and cannot be instantiated.");
    }
}
