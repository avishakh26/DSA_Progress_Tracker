package com.dsatracker.model;

import com.dsatracker.model.enums.Difficulty;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class BaseEntityTest {

    @Test
    void entitiesWithSameClassAndIdAreEqual() {
        final Topic a = new Topic("Arrays", "desc", 1, Difficulty.EASY);
        a.setId(5);
        final Topic b = new Topic("Arrays (renamed)", "other desc", 2, Difficulty.HARD);
        b.setId(5);

        assertEquals(a, b, "Same concrete type + same id must be equal regardless of other fields");
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void entitiesWithDifferentIdsAreNotEqual() {
        final Topic a = new Topic();
        a.setId(1);
        final Topic b = new Topic();
        b.setId(2);

        assertNotEquals(a, b);
    }

    @Test
    void transientEntitiesWithNullIdAreNeverEqualToEachOther() {
        final Topic a = new Topic();
        final Topic b = new Topic();

        assertNotEquals(a, b, "Two unsaved entities (null id) must not be considered equal");
        assertEquals(a, a, "An entity must always equal itself");
    }

    @Test
    void entitiesOfDifferentTypesWithSameIdAreNotEqual() {
        final Topic topic = new Topic();
        topic.setId(1);
        final Note note = new Note();
        note.setId(1);

        assertNotEquals(topic, note);
    }
}
