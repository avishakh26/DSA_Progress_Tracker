-- =====================================================================
-- Optional sample data (problems, notes, goal, activity). Runs AFTER roadmap.sql,
-- from Settings' "Restore Sample Data" and from the tests - never on a real first launch.
-- Topics are referenced by name via subqueries rather than hard-coded ids.
-- =====================================================================

-- Sample data starts the first two topics off as in progress.
UPDATE topics SET status = 'IN_PROGRESS' WHERE name IN ('Arrays', 'Strings');

-- ----- Sample problems (Arrays / Strings) --------------------------------
INSERT INTO problems (title, platform, url, topic_id, difficulty, status, date_added, date_solved) VALUES
    ('Two Sum', 'LEETCODE', 'https://leetcode.com/problems/two-sum/',
        (SELECT id FROM topics WHERE name = 'Arrays'), 'EASY', 'SOLVED', date('now', '-2 day'), date('now', '-1 day')),
    ('Best Time to Buy and Sell Stock', 'LEETCODE', 'https://leetcode.com/problems/best-time-to-buy-and-sell-stock/',
        (SELECT id FROM topics WHERE name = 'Arrays'), 'EASY', 'ATTEMPTED', date('now', '-1 day'), NULL),
    ('Maximum Subarray', 'LEETCODE', 'https://leetcode.com/problems/maximum-subarray/',
        (SELECT id FROM topics WHERE name = 'Arrays'), 'MEDIUM', 'NOT_STARTED', date('now'), NULL),
    ('Valid Anagram', 'LEETCODE', 'https://leetcode.com/problems/valid-anagram/',
        (SELECT id FROM topics WHERE name = 'Strings'), 'EASY', 'SOLVED', date('now', '-1 day'), date('now', '-1 day')),
    ('Longest Substring Without Repeating Characters', 'LEETCODE', 'https://leetcode.com/problems/longest-substring-without-repeating-characters/',
        (SELECT id FROM topics WHERE name = 'Strings'), 'MEDIUM', 'NOT_STARTED', date('now'), NULL);

-- ----- Sample note --------------------------------------------------------
INSERT INTO notes (title, topic_id, content) VALUES
    ('Two-pointer cheat sheet', (SELECT id FROM topics WHERE name = 'Arrays'),
     'Use two pointers moving inward for sorted-array pair problems; O(n) instead of O(n^2) brute force.');

-- ----- Sample goal & activity (so the dashboard is not empty) -------------
INSERT INTO goals (goal_type, target, start_date, end_date) VALUES
    ('DAILY', 3, date('now'), NULL);

INSERT INTO activity (activity_date, problems_solved) VALUES
    (date('now', '-1 day'), 2),
    (date('now'), 0);
