package com.dsatracker;

/**
 * Plain (non-JavaFX) launcher class.
 *
 * <p>When a JavaFX {@code Application} subclass is used as the main class of a
 * shaded/fat jar, the JVM refuses to start because the JavaFX runtime components
 * are on the classpath instead of the module path. Delegating from an ordinary
 * class that does <em>not</em> extend {@code Application} is the standard
 * workaround, so {@code java -jar dsa-progress-tracker.jar} works.</p>
 */
public final class Main {

    private Main() {
        // Utility launcher - never instantiated.
    }

    public static void main(final String[] args) {
        DsaTrackerApp.main(args);
    }
}
