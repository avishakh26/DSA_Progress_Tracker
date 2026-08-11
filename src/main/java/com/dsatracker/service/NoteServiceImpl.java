package com.dsatracker.service;

import com.dsatracker.exception.EntityNotFoundException;
import com.dsatracker.model.Note;
import com.dsatracker.repository.NoteRepository;
import com.dsatracker.util.Validator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public final class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;

    public NoteServiceImpl(final NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    @Override
    public List<Note> getAllNotes() {
        return noteRepository.findAll();
    }

    @Override
    public List<Note> getNotesByTopic(final int topicId) {
        return noteRepository.findByTopicId(topicId);
    }

    @Override
    public Optional<Note> findById(final int id) {
        return noteRepository.findById(id);
    }

    @Override
    public Note createNote(final String title, final Integer topicId, final String content) {
        Validator.requireNonBlank(title, "Note title");
        return noteRepository.save(new Note(title, topicId, content));
    }

    @Override
    public Note updateNoteContent(final int id, final String newContent) {
        final Note note = noteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No note found with id " + id));
        note.updateContent(newContent);
        return noteRepository.save(note);
    }

    @Override
    public Note updateNote(final Note note) {
        Validator.requireNonBlank(note.getTitle(), "Note title");
        note.setUpdatedAt(LocalDateTime.now());
        return noteRepository.save(note);
    }

    @Override
    public void deleteNote(final int id) {
        noteRepository.deleteById(id);
    }
}
