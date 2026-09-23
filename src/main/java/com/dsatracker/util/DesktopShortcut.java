package com.dsatracker.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * Creates a Desktop shortcut to the packaged {@code .exe} the first time it runs, so a user who
 * just extracted the zip gets a double-click launcher without any manual steps.
 *
 * <p>Does nothing when running from an IDE / {@code java -jar} (there is no exe to point at),
 * on non-Windows systems, or once it has already run - a marker file remembers that, so a user
 * who deletes the shortcut on purpose doesn't get it back on the next launch.</p>
 */
public final class DesktopShortcut {

    private static final String EXE_NAME = "DSA Progress Tracker.exe";
    private static final String MARKER = ".desktop-shortcut-done";

    private DesktopShortcut() {
        // Utility class.
    }

    /** Fire-and-forget: runs on a daemon thread and swallows every failure. */
    public static void createOnFirstRun() {
        final Thread thread = new Thread(DesktopShortcut::createIfNeeded, "desktop-shortcut");
        thread.setDaemon(true);
        thread.start();
    }

    private static void createIfNeeded() {
        try {
            if (!System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win")) {
                return;
            }
            final String command = ProcessHandle.current().info().command().orElse("");
            if (!Path.of(command).getFileName().toString().equalsIgnoreCase(EXE_NAME)) {
                return;
            }
            final Path marker = Path.of(AppConstants.DATA_DIRECTORY, MARKER);
            if (Files.exists(marker)) {
                return;
            }
            Files.createDirectories(marker.getParent());
            final String script = "$lnk = Join-Path ([Environment]::GetFolderPath('Desktop')) 'DSA Progress Tracker.lnk';"
                    + " if (-not (Test-Path $lnk)) {"
                    + " $s = (New-Object -ComObject WScript.Shell).CreateShortcut($lnk);"
                    + " $s.TargetPath = $env:DSA_EXE;"
                    + " $s.WorkingDirectory = Split-Path $env:DSA_EXE;"
                    + " $s.Save() }";
            final ProcessBuilder builder = new ProcessBuilder(List.of("powershell.exe", "-NoProfile",
                    "-NonInteractive", "-WindowStyle", "Hidden", "-Command", script));
            builder.environment().put("DSA_EXE", command);
            builder.redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.DISCARD);
            builder.start().waitFor();
            Files.writeString(marker, "done");
        } catch (final IOException | RuntimeException e) {
            // Best effort only - the app works fine without a shortcut.
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
