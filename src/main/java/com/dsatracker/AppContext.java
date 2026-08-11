package com.dsatracker;

import com.dsatracker.controller.AnalyticsController;
import com.dsatracker.controller.DashboardController;
import com.dsatracker.controller.GoalsController;
import com.dsatracker.controller.MainController;
import com.dsatracker.controller.NotesController;
import com.dsatracker.controller.ProblemsController;
import com.dsatracker.controller.RoadmapController;
import com.dsatracker.controller.SettingsController;
import com.dsatracker.database.DatabaseManager;
import com.dsatracker.repository.ActivityRepository;
import com.dsatracker.repository.GoalRepository;
import com.dsatracker.repository.NoteRepository;
import com.dsatracker.repository.ProblemRepository;
import com.dsatracker.repository.SqliteActivityRepository;
import com.dsatracker.repository.SqliteGoalRepository;
import com.dsatracker.repository.SqliteNoteRepository;
import com.dsatracker.repository.SqliteProblemRepository;
import com.dsatracker.repository.SqliteTopicRepository;
import com.dsatracker.repository.TopicRepository;
import com.dsatracker.service.ActivityService;
import com.dsatracker.service.ActivityServiceImpl;
import com.dsatracker.service.DashboardService;
import com.dsatracker.service.DashboardServiceImpl;
import com.dsatracker.service.GoalService;
import com.dsatracker.service.GoalServiceImpl;
import com.dsatracker.service.NoteService;
import com.dsatracker.service.NoteServiceImpl;
import com.dsatracker.service.ProblemService;
import com.dsatracker.service.ProblemServiceImpl;
import com.dsatracker.service.SettingsService;
import com.dsatracker.service.SettingsServiceImpl;
import com.dsatracker.service.TopicService;
import com.dsatracker.service.TopicServiceImpl;

/**
 * Hand-rolled composition root: builds every repository and service exactly
 * once, wired against the already-initialized {@code DatabaseManager}
 * singleton, and hands out the service interfaces. Also serves as the
 * JavaFX {@code FXMLLoader} controller factory ({@link #createController}),
 * so every controller - the root {@link MainController} and every routed
 * view controller added from Step 7 onward - is constructed here with the
 * services it needs, instead of relying on FXMLLoader's default
 * no-arg-constructor reflection.
 */
public final class AppContext {

    private final TopicService topicService;
    private final ProblemService problemService;
    private final NoteService noteService;
    private final GoalService goalService;
    private final ActivityService activityService;
    private final DashboardService dashboardService;
    private final SettingsService settingsService;
    private final ThemeManager themeManager;

    public AppContext(final ThemeManager themeManager) {
        this.themeManager = themeManager;

        final TopicRepository topicRepository = new SqliteTopicRepository();
        final ProblemRepository problemRepository = new SqliteProblemRepository();
        final NoteRepository noteRepository = new SqliteNoteRepository();
        final GoalRepository goalRepository = new SqliteGoalRepository();
        final ActivityRepository activityRepository = new SqliteActivityRepository();

        this.activityService = new ActivityServiceImpl(activityRepository);
        this.topicService = new TopicServiceImpl(topicRepository, problemRepository);
        this.problemService = new ProblemServiceImpl(problemRepository, topicService, activityService);
        this.noteService = new NoteServiceImpl(noteRepository);
        this.goalService = new GoalServiceImpl(goalRepository, activityRepository);
        this.dashboardService = new DashboardServiceImpl(topicRepository, problemRepository, activityService, goalService);
        this.settingsService = new SettingsServiceImpl(topicRepository, problemRepository, activityRepository,
                DatabaseManager.getInstance());
    }

    public TopicService getTopicService() {
        return topicService;
    }

    public ProblemService getProblemService() {
        return problemService;
    }

    public NoteService getNoteService() {
        return noteService;
    }

    public GoalService getGoalService() {
        return goalService;
    }

    public ActivityService getActivityService() {
        return activityService;
    }

    public DashboardService getDashboardService() {
        return dashboardService;
    }

    public SettingsService getSettingsService() {
        return settingsService;
    }

    public ThemeManager getThemeManager() {
        return themeManager;
    }

    /**
     * Given the {@code fx:controller} class an {@code FXMLLoader} needs to
     * instantiate, returns a fully-wired instance. Controllers with no
     * service dependencies fall back to their no-arg constructor.
     */
    public Object createController(final Class<?> controllerClass) {
        try {
            if (controllerClass == MainController.class) {
                return new MainController(this);
            }
            if (controllerClass == DashboardController.class) {
                return new DashboardController(dashboardService);
            }
            if (controllerClass == RoadmapController.class) {
                return new RoadmapController(topicService, problemService, themeManager);
            }
            if (controllerClass == ProblemsController.class) {
                return new ProblemsController(problemService, topicService, themeManager);
            }
            if (controllerClass == AnalyticsController.class) {
                return new AnalyticsController(problemService, topicService, activityService);
            }
            if (controllerClass == NotesController.class) {
                return new NotesController(noteService, topicService, themeManager);
            }
            if (controllerClass == GoalsController.class) {
                return new GoalsController(goalService, themeManager);
            }
            if (controllerClass == SettingsController.class) {
                return new SettingsController(settingsService, themeManager);
            }
            return controllerClass.getDeclaredConstructor().newInstance();
        } catch (final ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot instantiate controller: " + controllerClass.getName(), e);
        }
    }
}
