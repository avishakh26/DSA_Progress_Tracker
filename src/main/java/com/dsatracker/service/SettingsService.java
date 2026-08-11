package com.dsatracker.service;

public interface SettingsService {

    /** Resets every problem/topic back to NOT_STARTED and clears activity history, but keeps all rows. */
    void resetProgress();

    /** Wipes every table and reloads the original first-launch sample data. */
    void restoreSampleData();

    /** Wipes every table, leaving a completely empty database. */
    void clearAllData();
}
