package com.dsatracker.service;

import com.dsatracker.exception.EntityNotFoundException;
import com.dsatracker.model.Problem;
import com.dsatracker.model.enums.Difficulty;
import com.dsatracker.model.enums.Platform;
import com.dsatracker.model.enums.ProblemStatus;
import com.dsatracker.repository.ProblemRepository;
import com.dsatracker.util.Validator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class ProblemServiceImpl implements ProblemService {

    private final ProblemRepository problemRepository;
    private final TopicService topicService;
    private final ActivityService activityService;

    public ProblemServiceImpl(final ProblemRepository problemRepository, final TopicService topicService,
                               final ActivityService activityService) {
        this.problemRepository = problemRepository;
        this.topicService = topicService;
        this.activityService = activityService;
    }

    @Override
    public List<Problem> getAllProblems() {
        return problemRepository.findAll();
    }

    @Override
    public Optional<Problem> findById(final int id) {
        return problemRepository.findById(id);
    }

    @Override
    public Problem addProblem(final String title, final Platform platform, final String url, final int topicId,
                               final Difficulty difficulty, final String notes) {
        Validator.requireNonBlank(title, "Problem title");
        Validator.requireValidUrlIfPresent(url, "Problem URL");
        final Problem problem = new Problem(title, platform, url, topicId, difficulty);
        problem.setNotes(notes);
        final Problem saved = problemRepository.save(problem);
        topicService.recalculateStatus(topicId);
        return saved;
    }

    @Override
    public Problem updateProblem(final Problem problem) {
        Validator.requireNonBlank(problem.getTitle(), "Problem title");
        Validator.requireValidUrlIfPresent(problem.getUrl(), "Problem URL");
        final Problem saved = problemRepository.save(problem);
        topicService.recalculateStatus(problem.getTopicId());
        return saved;
    }

    @Override
    public void deleteProblem(final int id) {
        final Problem problem = requireExisting(id);
        problemRepository.deleteById(id);
        topicService.recalculateStatus(problem.getTopicId());
    }

    @Override
    public Problem markSolved(final int id) {
        final Problem problem = requireExisting(id);
        problem.markSolved();
        final Problem saved = problemRepository.save(problem);
        activityService.recordProblemSolved();
        topicService.recalculateStatus(problem.getTopicId());
        return saved;
    }

    @Override
    public Problem markAttempted(final int id) {
        final Problem problem = requireExisting(id);
        problem.markAttempted();
        final Problem saved = problemRepository.save(problem);
        topicService.recalculateStatus(problem.getTopicId());
        return saved;
    }

    @Override
    public List<Problem> search(final ProblemFilter filter, final ProblemSortBy sortBy) {
        final List<Problem> results = problemRepository.findAll().stream()
                .filter(p -> filter.titleKeyword() == null
                        || p.getTitle().toLowerCase().contains(filter.titleKeyword().toLowerCase()))
                .filter(p -> filter.topicId() == null || filter.topicId().equals(p.getTopicId()))
                .filter(p -> filter.difficulty() == null || filter.difficulty() == p.getDifficulty())
                .filter(p -> filter.platform() == null || filter.platform() == p.getPlatform())
                .filter(p -> filter.status() == null || filter.status() == p.getStatus())
                .collect(Collectors.toCollection(ArrayList::new));

        results.sort(comparatorFor(sortBy));
        return results;
    }

    @Override
    public List<DifficultyBreakdown> getDifficultyBreakdown() {
        final List<Problem> all = problemRepository.findAll();
        final List<DifficultyBreakdown> breakdown = new ArrayList<>();
        for (final Difficulty difficulty : Difficulty.values()) {
            final long total = all.stream().filter(p -> p.getDifficulty() == difficulty).count();
            final long solved = all.stream()
                    .filter(p -> p.getDifficulty() == difficulty && p.getStatus() == ProblemStatus.SOLVED)
                    .count();
            breakdown.add(new DifficultyBreakdown(difficulty, (int) total, (int) solved));
        }
        return breakdown;
    }

    private Problem requireExisting(final int id) {
        return problemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No problem found with id " + id));
    }

    private Comparator<Problem> comparatorFor(final ProblemSortBy sortBy) {
        return switch (sortBy) {
            case DIFFICULTY -> Comparator.comparing(Problem::getDifficulty);
            case TITLE_ALPHABETICAL -> Comparator.comparing(Problem::getTitle, String.CASE_INSENSITIVE_ORDER);
            case DATE_ADDED_DESC -> Comparator.comparing(Problem::getDateAdded).reversed();
        };
    }
}
