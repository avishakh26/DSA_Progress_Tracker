package com.dsatracker.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ActivityTest {

    @Test
    void incrementSolvedAddsOneToTheCount() {
        final Activity activity = new Activity(LocalDate.now(), 2);

        activity.incrementSolved();
        activity.incrementSolved();

        assertEquals(4, activity.getProblemsSolved());
    }
}
