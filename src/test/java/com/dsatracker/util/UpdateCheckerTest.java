package com.dsatracker.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpdateCheckerTest {

    @Test
    void comparesVersionsNumerically() {
        assertTrue(UpdateChecker.isNewer("v1.0.1", "1.0.0"));
        assertTrue(UpdateChecker.isNewer("1.10.0", "1.9.2"));
        assertTrue(UpdateChecker.isNewer("2.0", "1.9.9"));
        assertFalse(UpdateChecker.isNewer("v1.0.0", "1.0.0"));
        assertFalse(UpdateChecker.isNewer("0.9.0", "1.0.0"));
        assertFalse(UpdateChecker.isNewer("beta", "1.0.0"));
    }

    @Test
    void parsesLatestReleaseJson() {
        final String json = "{\"html_url\":\"https://github.com/x/y/releases/tag/v1.2.0\",\"tag_name\":\"v1.2.0\"}";
        final var update = UpdateChecker.parse(json, "1.0.0").orElseThrow();
        assertEquals("1.2.0", update.version());
        assertEquals("https://github.com/x/y/releases/tag/v1.2.0", update.pageUrl());
        assertTrue(UpdateChecker.parse(json, "1.2.0").isEmpty());
        assertTrue(UpdateChecker.parse("{}", "1.0.0").isEmpty());
    }
}
