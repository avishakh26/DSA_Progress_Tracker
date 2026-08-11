package com.dsatracker.model.enums;

/** Judge/platform a problem was sourced from; constant names mirror the DB CHECK constraint on problems.platform. */
public enum Platform {
    LEETCODE("LeetCode"),
    CODEFORCES("Codeforces"),
    HACKERRANK("HackerRank"),
    GEEKSFORGEEKS("GeeksforGeeks"),
    CODECHEF("CodeChef"),
    ATCODER("AtCoder"),
    OTHER("Other");

    private final String displayName;

    Platform(final String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
