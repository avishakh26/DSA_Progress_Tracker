package com.dsatracker.service;

import com.dsatracker.exception.EntityNotFoundException;
import com.dsatracker.model.DiaryEntry;
import com.dsatracker.repository.DiaryEntryRepository;
import com.dsatracker.util.Validator;

import java.util.List;

public final class DiaryServiceImpl implements DiaryService {

    private final DiaryEntryRepository diaryEntryRepository;

    public DiaryServiceImpl(final DiaryEntryRepository diaryEntryRepository) {
        this.diaryEntryRepository = diaryEntryRepository;
    }

    @Override
    public List<DiaryEntry> getAllEntries() {
        return diaryEntryRepository.findAll();
    }

    @Override
    public DiaryEntry createEntry(final String title, final String content) {
        Validator.requireNonBlank(title, "Entry title");
        return diaryEntryRepository.save(new DiaryEntry(title, content));
    }

    @Override
    public DiaryEntry updateEntry(final int id, final String title, final String content) {
        Validator.requireNonBlank(title, "Entry title");
        final DiaryEntry entry = diaryEntryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No diary entry found with id " + id));
        entry.updateEntry(title, content);
        return diaryEntryRepository.save(entry);
    }

    @Override
    public void deleteEntry(final int id) {
        diaryEntryRepository.deleteById(id);
    }
}
