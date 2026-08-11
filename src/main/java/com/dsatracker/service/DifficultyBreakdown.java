package com.dsatracker.service;

import com.dsatracker.model.enums.Difficulty;

/** Total and solved problem counts for one difficulty tier, for the difficulty-distribution chart. */
public record DifficultyBreakdown(Difficulty difficulty, int total, int solved) {

    public int unsolved() {
        return total - solved;
    }
}
