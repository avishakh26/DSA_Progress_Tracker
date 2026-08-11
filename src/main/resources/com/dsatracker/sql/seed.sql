-- =====================================================================
-- First-launch sample data.
-- DatabaseManager only runs this script when the `topics` table is empty,
-- so it never duplicates rows on subsequent launches.
-- Topics are referenced by name via subqueries rather than hard-coded ids,
-- so insertion order here is free to change without breaking anything.
-- =====================================================================

-- ----- Phase 1: Fundamentals -----------------------------------------
INSERT INTO topics (name, description, phase, difficulty, status) VALUES
    ('Arrays', 'Contiguous storage, traversal, two-pointer and sliding-window techniques.', 1, 'EASY', 'IN_PROGRESS'),
    ('Strings', 'Pattern matching, parsing and manipulation of character sequences.', 1, 'EASY', 'IN_PROGRESS'),
    ('Math & Bit Manipulation', 'Number theory basics, bitwise tricks, modular arithmetic.', 1, 'EASY', 'NOT_STARTED');

-- ----- Phase 2: Linear data structures --------------------------------
INSERT INTO topics (name, description, phase, difficulty, status) VALUES
    ('Linked Lists', 'Singly/doubly linked lists, fast-slow pointers, in-place reversal.', 2, 'MEDIUM', 'NOT_STARTED'),
    ('Stacks & Queues', 'LIFO/FIFO structures, monotonic stacks, sliding-window maximum.', 2, 'MEDIUM', 'NOT_STARTED'),
    ('Hashing', 'Hash maps and sets for O(1) lookup, collision handling.', 2, 'MEDIUM', 'NOT_STARTED');

-- ----- Phase 3: Recursion, searching & sorting ------------------------
INSERT INTO topics (name, description, phase, difficulty, status) VALUES
    ('Recursion & Backtracking', 'Divide and conquer, combinatorial search, pruning.', 3, 'MEDIUM', 'NOT_STARTED'),
    ('Sorting Algorithms', 'Comparison and non-comparison sorts, stability, complexity trade-offs.', 3, 'MEDIUM', 'NOT_STARTED'),
    ('Binary Search', 'Search space reduction over sorted and monotonic domains.', 3, 'MEDIUM', 'NOT_STARTED');

-- ----- Phase 4: Trees & graphs -----------------------------------------
INSERT INTO topics (name, description, phase, difficulty, status) VALUES
    ('Trees', 'Traversals, recursion on tree structures, height/diameter problems.', 4, 'MEDIUM', 'NOT_STARTED'),
    ('Binary Search Trees', 'BST invariants, balancing, order-statistics queries.', 4, 'MEDIUM', 'NOT_STARTED'),
    ('Graphs', 'BFS/DFS, connectivity, shortest paths, topological sort.', 4, 'HARD', 'NOT_STARTED');

-- ----- Phase 5: Dynamic programming & greedy ----------------------------
INSERT INTO topics (name, description, phase, difficulty, status) VALUES
    ('Dynamic Programming', 'Memoization, tabulation, classic DP families.', 5, 'HARD', 'NOT_STARTED'),
    ('Greedy Algorithms', 'Local-optimal choice strategies and their correctness proofs.', 5, 'MEDIUM', 'NOT_STARTED');

-- ----- Phase 6: Advanced topics ------------------------------------------
INSERT INTO topics (name, description, phase, difficulty, status) VALUES
    ('Advanced Graphs', 'MST, union-find, network flow, articulation points.', 6, 'HARD', 'NOT_STARTED'),
    ('Tries', 'Prefix trees for string sets, autocomplete-style queries.', 6, 'HARD', 'NOT_STARTED'),
    ('Segment & Fenwick Trees', 'Range query/update structures.', 6, 'HARD', 'NOT_STARTED');

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
