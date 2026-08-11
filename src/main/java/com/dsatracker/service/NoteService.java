package com.dsatracker.service;

import com.dsatracker.model.Note;

import java.util.List;
import java.util.Optional;

public interface NoteService {

    List<Note> getAllNotes();

    List<Note> getNotesByTopic(int topicId);

    Optional<Note> findById(int id);

    Note createNote(String title, Integer topicId, String content);

    Note updateNoteContent(int id, String newContent);

    /** Persists title/topic/content from a full edit form and restamps updatedAt. */
    Note updateNote(Note note);

    void deleteNote(int id);
}
