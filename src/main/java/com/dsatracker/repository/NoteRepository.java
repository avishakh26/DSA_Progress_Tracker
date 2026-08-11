package com.dsatracker.repository;

import com.dsatracker.model.Note;

import java.util.List;

public interface NoteRepository extends Repository<Note, Integer> {
    List<Note> findByTopicId(Integer topicId);
}
