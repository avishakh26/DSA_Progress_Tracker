-- =====================================================================
-- Default learning roadmap: the phased topic list every new user starts with.
-- Run once, when the database file is first created (and again by Restore Sample Data).
-- =====================================================================

-- ----- Phase 1: Fundamentals -----------------------------------------
INSERT INTO topics (name, description, phase, difficulty, status) VALUES
    ('Arrays', 'Contiguous storage, traversal, two-pointer and sliding-window techniques.', 1, 'EASY', 'NOT_STARTED'),
    ('Strings', 'Pattern matching, parsing and manipulation of character sequences.', 1, 'EASY', 'NOT_STARTED'),
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
