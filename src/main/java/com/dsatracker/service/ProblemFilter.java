package com.dsatracker.service;

import com.dsatracker.model.enums.Difficulty;
import com.dsatracker.model.enums.Platform;
import com.dsatracker.model.enums.ProblemStatus;

/** Every field is optional - {@code null} means "no filter on this criterion". */
public record ProblemFilter(
        String titleKeyword,
        Integer topicId,
        Difficulty difficulty,
        Platform platform,
        ProblemStatus status
) {
    public static ProblemFilter none() {
        return new ProblemFilter(null, null, null, null, null);
    }
}
