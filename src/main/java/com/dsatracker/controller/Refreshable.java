package com.dsatracker.controller;

/**
 * Implemented by controllers whose displayed data can go stale while their
 * view sits alive in {@link MainController}'s per-{@link NavItem} cache -
 * e.g. Dashboard showing an old solved-count after a problem was solved
 * from the Problems page. {@link MainController} calls {@link #refresh()}
 * on every navigation to a cached view, not just its first load.
 */
public interface Refreshable {
    void refresh();
}
