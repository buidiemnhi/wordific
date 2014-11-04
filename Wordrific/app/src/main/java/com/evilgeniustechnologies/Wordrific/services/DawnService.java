package com.evilgeniustechnologies.Wordrific.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.evilgeniustechnologies.Wordrific.models.DawnDatabase;
import com.evilgeniustechnologies.Wordrific.utilties.L;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by vendetta on 5/30/14.
 */
public class DawnService extends Service {
    public static final String TASK = "task";
    private DawnDatabase database;
    private ThreadPoolExecutor executor;
    private AtomicInteger totalSegments;
    private AtomicInteger segments;
    private List<String> newUsers;
    private List<String> newFriendships;
    private List<String> newConfigurations;
    private List<String> newPuzzles;
    private List<String> newScores;
    private List<String> currentUsers;
    private List<String> currentFriendships;
    private List<String> currentConfigurations;
    private List<String> currentPuzzles;
    private List<String> currentScores;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(6);
        totalSegments = new AtomicInteger();
        segments = new AtomicInteger();
        newUsers = Collections.synchronizedList(new ArrayList<String>());
        newFriendships = Collections.synchronizedList(new ArrayList<String>());
        newConfigurations = Collections.synchronizedList(new ArrayList<String>());
        newPuzzles = Collections.synchronizedList(new ArrayList<String>());
        newScores = Collections.synchronizedList(new ArrayList<String>());
        currentUsers = Collections.synchronizedList(new ArrayList<String>());
        currentFriendships = Collections.synchronizedList(new ArrayList<String>());
        currentConfigurations = Collections.synchronizedList(new ArrayList<String>());
        currentPuzzles = Collections.synchronizedList(new ArrayList<String>());
        currentScores = Collections.synchronizedList(new ArrayList<String>());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onHandleIntent(intent);
        return START_STICKY;
    }

    private void onHandleIntent(Intent intent) {
        if (intent == null) {
            L.e("Intent", "null");
            return;
        }
        Task task = (Task) intent.getSerializableExtra(TASK);
        if (task == null) {
            L.e("Task", "null");
            return;
        }
        switch (task) {
            case FETCH_ALL:
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        L.e("Init", "executed");
                        try {
                            fetchIndependentEntities();
                        } catch (ParseException e) {
                            L.e("Init", e.getMessage(), e);
                        } catch (NullPointerException e) {
                            L.e("Init", e.getMessage(), e);
                        }
                    }
                });
                break;
        }
    }

    private void fetchIndependentEntities() throws ParseException, NullPointerException {
        database = DawnDatabase.getInstance();
        // Get the highest set number
        ParseObject puzzle = ParseQuery.getQuery("Puzzles").addDescendingOrder("Set").getFirst();
        if (puzzle != null) {
            database.setHighestSet(puzzle.getInt("Set"));
        }

        ParseQuery<ParseUser> uQuery = database.getUserQuery();
        ParseQuery<ParseObject> cQuery = database.getConfigurationQuery();
        ParseQuery<ParseObject> fQuery = database.getFriendshipQuery();
        ParseQuery<ParseObject> sQuery = database.getScoreQuery();
        int fCount = fQuery.count();
        int sCount = sQuery.count();
        int uCount = uQuery.count();
        int cCount = cQuery.count();
        if (fCount == 0 && sCount == 0 && uCount == 0 && cCount == 0) {
            L.e("No independent entities found", "Shutting down");
            segments.set(0);
            totalSegments.set(1);
            shutdown();
            return;
        }
        database.setTotalProgress(uCount + cCount + fCount + sCount);
        L.e("fCount", fCount);
        L.e("sCount", sCount);
        L.e("uCount", uCount);
        L.e("cCount", cCount);
        int uSegments = (int) Math.ceil(uCount / 100.0);
        int cSegments = (int) Math.ceil(cCount / 500.0);
        segments.set(0);
        totalSegments.set(uSegments + cSegments);
        L.e("uSegments", uSegments);
        L.e("cSegments", cSegments);
        for (int i = 0; i < uSegments; i++) {
            executor.execute(new DawnRunnable(DawnDatabase.Task.FETCH_USER, i * 100, i * 100 + 100 - 1, 20));
        }
        for (int i = 0; i < cSegments; i++) {
            executor.execute(new DawnRunnable(DawnDatabase.Task.FETCH_CONFIGURATION, i * 500, i * 500 + 500 - 1, 100));
        }
    }

    private void fetchDependentEntities() throws ParseException, NullPointerException {
        if (segments.incrementAndGet() != totalSegments.get()) {
            return;
        }
        L.e("All independent entities", "fetched");
        ParseQuery<ParseObject> fQuery = database.getFriendshipQuery();
        ParseQuery<ParseObject> sQuery = database.getScoreQuery();
        int fCount = fQuery.count();
        int sCount = sQuery.count();
        L.e("fCount", fCount);
        L.e("sCount", sCount);
        if (fCount == 0 && sCount == 0) {
            L.e("No dependent entities found", "Shutting down");
            segments.set(0);
            totalSegments.set(1);
            shutdown();
            return;
        }
        int fSegments = (int) Math.ceil(fCount / 500.0);
        int sSegments = (int) Math.ceil(sCount / 500.0);
        segments.set(0);
        totalSegments.set(fSegments + sSegments);
        L.e("fSegments", fSegments);
        L.e("sSegments", sSegments);
        for (int i = 0; i < fSegments; i++) {
            executor.execute(new DawnRunnable(DawnDatabase.Task.FETCH_FRIENDSHIP, i * 500, i * 500 + 500 - 1, 100));
        }
        for (int i = 0; i < sSegments; i++) {
            executor.execute(new DawnRunnable(DawnDatabase.Task.FETCH_SCORE, i * 500, i * 500 + 500 - 1, 100));
        }
    }

    private void deleteNonexistentEntities() throws NullPointerException {
        if (segments.incrementAndGet() != totalSegments.get()) {
            return;
        }
        L.e("All dependent entities", "fetched");
        int uCount = (int) (database.getUserDao().count() - (currentUsers.size() + newUsers.size()));
        int fCount = (int) (database.getFriendshipDao().count() - (currentFriendships.size() + newFriendships.size()));
        int cCount = (int) (database.getConfigurationDao().count() - (currentConfigurations.size() + newConfigurations.size()));
        int sCount = (int) (database.getScoreDao().count() - (currentScores.size() + newScores.size()));
        L.e("fCount", fCount);
        L.e("sCount", sCount);
        L.e("uCount", uCount);
        L.e("cCount", cCount);
        if (uCount == 0 && fCount == 0 && cCount == 0 && sCount == 0) {
            L.e("No nonexistent entities found", "Shutting down");
            segments.set(0);
            totalSegments.set(1);
            shutdown();
            return;
        }
        database.setTotalProgress(database.getTotalProgress().get() + uCount + fCount + cCount + sCount);
        segments.set(0);
        totalSegments.set(4);
        executor.execute(new DawnRunnable(DawnDatabase.Task.DELETE_USER, 0, 0, 0));
        executor.execute(new DawnRunnable(DawnDatabase.Task.DELETE_FRIENDSHIP, 0, 0, 0));
        executor.execute(new DawnRunnable(DawnDatabase.Task.DELETE_CONFIGURATION, 0, 0, 0));
        executor.execute(new DawnRunnable(DawnDatabase.Task.DELETE_SCORE, 0, 0, 0));
    }

    private void shutdown() {
        if (segments.incrementAndGet() != totalSegments.get()) {
            return;
        }
        L.e("Nonexistent entities", "deleted");
        terminate();
    }

    private void terminate() {
        executor.shutdownNow();
        this.stopSelf();
        L.e("Executor", "terminated");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        terminate();
        L.e(this.getClass().getName(), "destroyed");
    }

    public enum Task {
        FETCH_ALL,
    }

    private class DawnRunnable implements Runnable {
        private DawnDatabase.Task task;
        private int skip;
        private int end;
        private int limit;

        private DawnRunnable(DawnDatabase.Task task, int skip, int end, int limit) {
            this.task = task;
            this.skip = skip;
            this.end = end;
            this.limit = limit;
        }

        @Override
        public void run() {
            ParseQuery<ParseUser> uQuery = database.getUserQuery();
            ParseQuery<ParseObject> cQuery = database.getConfigurationQuery();
            ParseQuery<ParseObject> pQuery = database.getPuzzleQuery();
            ParseQuery<ParseObject> fQuery = database.getFriendshipQuery();
            ParseQuery<ParseObject> sQuery = database.getScoreQuery();
            switch (task) {
                case FETCH_USER:
                    try {
                        L.e(task.name(), skip);
                        database.fetchRecursive(uQuery, skip, end, limit, task, currentUsers, newUsers);
                        fetchDependentEntities();
                    } catch (ParseException e) {
                        L.e(task.name(), e.getMessage(), e);
                    } catch (NullPointerException e) {
                        L.e(task.name(), e.getMessage(), e);
                    }
                    break;
                case FETCH_FRIENDSHIP:
                    try {
                        L.e(task.name(), skip);
                        database.fetchRecursive(fQuery, skip, end, limit, task, currentFriendships, newFriendships);
                        deleteNonexistentEntities();
                    } catch (ParseException e) {
                        L.e(task.name(), e.getMessage(), e);
                    } catch (NullPointerException e) {
                        L.e(task.name(), e.getMessage(), e);
                    }
                    break;
                case FETCH_CONFIGURATION:
                    try {
                        L.e(task.name(), skip);
                        database.fetchRecursive(cQuery, skip, end, limit, task, currentConfigurations, newConfigurations);
                        fetchDependentEntities();
                    } catch (ParseException e) {
                        L.e(task.name(), e.getMessage(), e);
                    } catch (NullPointerException e) {
                        L.e(task.name(), e.getMessage(), e);
                    }
                    break;
                case FETCH_PUZZLE:
                    try {
                        L.e(task.name(), skip);
                        database.fetchRecursive(pQuery, skip, end, limit, task, currentPuzzles, newPuzzles);
                        fetchDependentEntities();
                    } catch (ParseException e) {
                        L.e(task.name(), e.getMessage(), e);
                    } catch (NullPointerException e) {
                        L.e(task.name(), e.getMessage(), e);
                    }
                    break;
                case FETCH_SCORE:
                    try {
                        L.e(task.name(), skip);
                        database.fetchRecursive(sQuery, skip, end, limit, task, currentScores, newScores);
                        deleteNonexistentEntities();
                    } catch (ParseException e) {
                        L.e(task.name(), e.getMessage(), e);
                    } catch (NullPointerException e) {
                        L.e(task.name(), e.getMessage(), e);
                    }
                    break;
                case DELETE_USER:
                    try {
                        L.e(task.name(), "started");
                        database.deleteRecursive(currentUsers, newUsers, task);
                        shutdown();
                    } catch (NullPointerException e) {
                        L.e(task.name(), e.getMessage(), e);
                    }
                    break;
                case DELETE_FRIENDSHIP:
                    try {
                        L.e(task.name(), "started");
                        database.deleteRecursive(currentFriendships, newFriendships, task);
                        shutdown();
                    } catch (NullPointerException e) {
                        L.e(task.name(), e.getMessage(), e);
                    }
                    break;
                case DELETE_CONFIGURATION:
                    try {
                        L.e(task.name(), "started");
                        database.deleteRecursive(currentConfigurations, newConfigurations, task);
                        shutdown();
                    } catch (NullPointerException e) {
                        L.e(task.name(), e.getMessage(), e);
                    }
                    break;
                case DELETE_PUZZLE:
                    try {
                        L.e(task.name(), "started");
                        database.deleteRecursive(currentPuzzles, newPuzzles, task);
                        shutdown();
                    } catch (NullPointerException e) {
                        L.e(task.name(), e.getMessage(), e);
                    }
                    break;
                case DELETE_SCORE:
                    try {
                        L.e(task.name(), "started");
                        database.deleteRecursive(currentScores, newScores, task);
                        shutdown();
                    } catch (NullPointerException e) {
                        L.e(task.name(), e.getMessage(), e);
                    }
                    break;
            }
        }
    }
}
