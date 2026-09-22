package com.dsatracker.service;

import com.dsatracker.model.DiaryEntry;

import java.util.List;

public interface DiaryService {

    List<DiaryEntry> getAllEntries();

    DiaryEntry createEntry(String title, String content);

    /** Persists title/content from a full edit form and restamps updatedAt; entryDate is left untouched. */
    DiaryEntry updateEntry(int id, String title, String content);

    void deleteEntry(int id);
}
