package com.evilgeniustechnologies.Wordrific.models;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;

import com.evilgeniustechnologies.Wordrific.daomodel.Configuration;
import com.evilgeniustechnologies.Wordrific.daomodel.ConfigurationDao;
import com.evilgeniustechnologies.Wordrific.daomodel.DaoMaster;
import com.evilgeniustechnologies.Wordrific.daomodel.DaoSession;
import com.evilgeniustechnologies.Wordrific.daomodel.Friendship;
import com.evilgeniustechnologies.Wordrific.daomodel.FriendshipDao;
import com.evilgeniustechnologies.Wordrific.daomodel.Puzzle;
import com.evilgeniustechnologies.Wordrific.daomodel.PuzzleDao;
import com.evilgeniustechnologies.Wordrific.daomodel.Score;
import com.evilgeniustechnologies.Wordrific.daomodel.ScoreDao;
import com.evilgeniustechnologies.Wordrific.daomodel.User;
import com.evilgeniustechnologies.Wordrific.daomodel.UserDao;
import com.evilgeniustechnologies.Wordrific.services.DawnIntentService;
import com.evilgeniustechnologies.Wordrific.services.DawnService;
import com.evilgeniustechnologies.Wordrific.utilties.DawnUtilities;
import com.evilgeniustechnologies.Wordrific.utilties.DialogManager;
import com.evilgeniustechnologies.Wordrific.utilties.L;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by vendetta on 5/29/14.
 */
public class DawnDatabase {
    public static final int ACCEPT = 1;
    public static final int PENDING = 2;

    private static final int CORRECT_EASY_ANSWER_INDEX = 0;
    private static final int CORRECT_MEDIUM_ANSWER_INDEX = 1;
    private static final int CORRECT_HARD_ANSWER_INDEX = 2;
    private static final int QUICK_EASY_ANSWER_INDEX = 3;
    private static final int QUICK_MEDIUM_ANSWER_INDEX = 4;
    private static final int QUICK_HARD_ANSWER_INDEX = 5;
    private static final int TIME_BONUS_EASY_INDEX = 6;
    private static final int TIME_BONUS_MEDIUM_INDEX = 7;
    private static final int TIME_BONUS_HARD_INDEX = 8;
    private static final int PENALTY_WRONG_EASY_INDEX = 9;
    private static final int PENALTY_WRONG_MEDIUM_INDEX = 10;
    private static final int PENALTY_WRONG_HARD_INDEX = 11;
    private static final int PENALTY_CLUE_EASY_INDEX = 12;
    private static final int PENALTY_CLUE_MEDIUM_INDEX = 13;
    private static final int PENALTY_CLUE_HARD_INDEX = 14;
    private static final int PENALTY_SKIP_PUZZLE_INDEX = 15;
    private static final int TOTAL_START_TIME_INDEX = 16;
    private static final int NUMBER_OF_PUZZLES_INDEX = 17;

    private static DawnDatabase instance;
    private SQLiteDatabase database;
    private DaoMaster master;
    private DaoSession session;
    private UserDao userDao;
    private FriendshipDao friendshipDao;
    private ConfigurationDao configurationDao;
    private PuzzleDao puzzleDao;
    private ScoreDao scoreDao;

    private DawnReceiver receiver;
    private Context context;
    private boolean loggedIn = false;
    private boolean populated = false;
    private int progressStatus;
    private AtomicInteger totalProgress;
    private AtomicInteger progress;
    private int highestSet = 130;

    private ImageLoader loader;
    private ImageSize screenSize;
    private User user;
    private List<DawnUtilities.Triple> requests;

    private DawnDatabase() {
    }

    private DawnDatabase(Context context) {
        construct(context);
    }

    public static DawnDatabase getInstance() {
        if (instance == null) {
            instance = new DawnDatabase();
        }
        return instance;
    }

    public static DawnDatabase getInstance(Context context) {
        if (instance == null || !instance.isPopulated()) {
            L.e("Init", "Database");
            instance = new DawnDatabase(context);
            if (DawnUtilities.isConnected(context)) {
                DialogManager.show(context, DialogManager.Alert.PROGRESS_BAR);
                fetchingData(context);
            } else {
                instance.progressStatus = 100;
                instance.notifyReceiver(Status.DATABASE_READY);
            }
        }
        return instance;
    }

    private static void fetchingData(Context context) {
        Intent intent = new Intent(context, DawnService.class);
        intent.putExtra(DawnService.TASK, DawnService.Task.FETCH_ALL);
        context.startService(intent);
    }

    private void construct(Context context) {
        // Get Image Loader
        loader = ImageLoader.getInstance();

        // Get screen size
        Point size = DawnUtilities.getScreenSize(context);
        screenSize = new ImageSize(size.x, size.y);

        // Loading session
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null && currentUser.isAuthenticated()) {
            loggedIn = true;
        }

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, "parse-db", null);
        database = helper.getWritableDatabase();
        master = new DaoMaster(database);
        session = master.newSession();
        totalProgress = new AtomicInteger();
        progress = new AtomicInteger();

        userDao = session.getUserDao();
        friendshipDao = session.getFriendshipDao();
        configurationDao = session.getConfigurationDao();
        puzzleDao = session.getPuzzleDao();
        scoreDao = session.getScoreDao();

        populated = checkPopulated();
        L.e("Construction", "complete");
    }

    public boolean checkPopulated() {
        return !(userDao == null ||
                friendshipDao == null ||
                configurationDao == null ||
                puzzleDao == null ||
                scoreDao == null);
    }

    public boolean isDatabaseExist() {
        if (!checkPopulated()) {
            userDao.deleteAll();
            friendshipDao.deleteAll();
            configurationDao.deleteAll();
            puzzleDao.deleteAll();
            scoreDao.deleteAll();
            L.e("Database", "cleared");
            return false;
        }
        return true;
    }

    public boolean isPopulated() {
        return populated;
    }

    public void refresh() {
        session.clear();
        refreshCurrentUser();
    }

    public Task fetchRecursive(ParseQuery query, int skip, int end, int limit, Task task, List<String> current, List<String> news) throws ParseException {
        query.setSkip(skip);
        query.setLimit(limit);
        List parseObjects = query.find();
        ParseObject pObject;
        for (Object object : parseObjects) {
            switch (task) {
                case FETCH_USER:
                    ParseUser pUser = (ParseUser) object;
                    fetchUser(pUser, current, news);
                    break;
                case FETCH_FRIENDSHIP:
                    pObject = (ParseObject) object;
                    fetchFriendship(pObject, current, news);
                    break;
                case FETCH_CONFIGURATION:
                    pObject = (ParseObject) object;
                    fetchConfiguration(pObject, current, news);
                    break;
                case FETCH_PUZZLE:
                    pObject = (ParseObject) object;
                    fetchPuzzle(pObject, current, news);
                    break;
                case FETCH_SCORE:
                    pObject = (ParseObject) object;
                    fetchScore(pObject, current, news);
                    break;
            }
        }
        setProgressStatus(parseObjects.size());
        if (skip + limit >= end) {
            return task;
        }
        if (parseObjects.size() == limit) {
            fetchRecursive(query, skip + limit, end, limit, task, current, news);
        }
        return task;
    }

    public void fetchUser(ParseObject pObject) throws ParseException {
        fetchUser(pObject, new ArrayList<String>(), new ArrayList<String>());
    }

    private boolean fetchUser(ParseObject pObject, List<String> current, List<String> news) throws ParseException {
        ParseUser pUser = (ParseUser) pObject;
        User user = userDao.queryBuilder()
                .where(UserDao.Properties.ObjectId.eq(pUser.getObjectId()))
                .unique();
        if (user == null) {
            user = new User();

            user.setObjectId(pUser.getObjectId());
            user.setUpdatedAt(pUser.getUpdatedAt());
            user.setUsername(pUser.getUsername());
            user.setEmail(pUser.getEmail());
            user.setFirstName(pUser.getString("firstName"));
            user.setLastName(pUser.getString("lastName"));
            user.setBestScore(pUser.getInt("bestScore"));
            user.setNextSetToBuy(pUser.getInt("nextSetToBuy"));
            ParseFile pFile = pUser.getParseFile("avatar");
            if (pFile != null) {
                user.setAvatarUrl(pFile.getUrl());
            }

            userDao.insert(user);
            news.add(user.getObjectId());
            return true;
        } else if (!user.getUpdatedAt().equals(pUser.getUpdatedAt())) {
            user.setUpdatedAt(pUser.getUpdatedAt());
            user.setUsername(pUser.getUsername());
            user.setEmail(pUser.getEmail());
            user.setFirstName(pUser.getString("firstName"));
            user.setLastName(pUser.getString("lastName"));
            user.setBestScore(pUser.getInt("bestScore"));
            user.setNextSetToBuy(pUser.getInt("nextSetToBuy"));
            ParseFile pFile = pUser.getParseFile("avatar");
            if (pFile != null) {
                user.setAvatarUrl(pFile.getUrl());
            }

            userDao.update(user);
            news.add(user.getObjectId());
            return true;
        }
        current.add(user.getObjectId());
        return false;
    }

    public void fetchFriendship(ParseObject pObject) {
        fetchFriendship(pObject, null, null);
    }

    private boolean fetchFriendship(ParseObject pObject, List<String> current, List<String> news) {
        Friendship friendship = friendshipDao.queryBuilder()
                .where(FriendshipDao.Properties.ObjectId.eq(pObject.getObjectId()))
                .unique();
        if (friendship == null) {
            friendship = new Friendship();

            friendship.setObjectId(pObject.getObjectId());
            friendship.setUpdatedAt(pObject.getUpdatedAt());

            friendship.setSeenRequest(pObject.getBoolean("hasSeenRequestNotification"));
            friendship.setStatus(pObject.getInt("status"));

            friendship.setRequestSenderObjectId(pObject.getParseUser("requester").getObjectId());
            friendship.setRequestReceiverObjectId(pObject.getParseUser("requestee").getObjectId());

            User requestSender = userDao.queryBuilder()
                    .where(UserDao.Properties.ObjectId.eq(friendship.getRequestSenderObjectId()))
                    .uniqueOrThrow();
            User requestReceiver = userDao.queryBuilder()
                    .where(UserDao.Properties.ObjectId.eq(friendship.getRequestReceiverObjectId()))
                    .uniqueOrThrow();
            friendship.setRequestSender(requestSender);
            friendship.setRequestReceiver(requestReceiver);

            friendshipDao.insert(friendship);
            if (news != null) {
                news.add(friendship.getObjectId());
            }
            return true;
        } else if (!friendship.getUpdatedAt().equals(pObject.getUpdatedAt())) {
            friendship.setUpdatedAt(pObject.getUpdatedAt());

            friendship.setSeenRequest(pObject.getBoolean("hasSeenRequestNotification"));
            friendship.setStatus(pObject.getInt("status"));

            friendship.setRequestSenderObjectId(pObject.getParseUser("requester").getObjectId());
            friendship.setRequestReceiverObjectId(pObject.getParseUser("requestee").getObjectId());

            User requestSender = userDao.queryBuilder()
                    .where(UserDao.Properties.ObjectId.eq(friendship.getRequestSenderObjectId()))
                    .uniqueOrThrow();
            User requestReceiver = userDao.queryBuilder()
                    .where(UserDao.Properties.ObjectId.eq(friendship.getRequestReceiverObjectId()))
                    .uniqueOrThrow();
            friendship.setRequestSender(requestSender);
            friendship.setRequestReceiver(requestReceiver);

            friendshipDao.update(friendship);
            if (news != null) {
                news.add(friendship.getObjectId());
            }
            return true;
        }
        if (current != null) {
            current.add(friendship.getObjectId());
        }
        return false;
    }

    private boolean fetchConfiguration(ParseObject pObject, List<String> current, List<String> news) {
        Configuration configuration = configurationDao.queryBuilder()
                .where(ConfigurationDao.Properties.ObjectId.eq(pObject.getObjectId()))
                .unique();
        if (configuration == null) {
            configuration = new Configuration();

            configuration.setObjectId(pObject.getObjectId());
            configuration.setUpdatedAt(pObject.getUpdatedAt());

            configuration.setGameRules(pObject.getString("gameRules"));
            configuration.setBase64EncodedPublicKey(pObject.getString("base64EncodedPublicKey"));

            List<Integer> configs = DawnUtilities.getConfigurations(pObject.getString("configVariables"));
            configuration.setCorrectAnswerEasy(configs.get(CORRECT_EASY_ANSWER_INDEX));
            configuration.setCorrectAnswerMedium(configs.get(CORRECT_MEDIUM_ANSWER_INDEX));
            configuration.setCorrectAnswerHard(configs.get(CORRECT_HARD_ANSWER_INDEX));
            configuration.setQuickAnswerEasy(configs.get(QUICK_EASY_ANSWER_INDEX));
            configuration.setQuickAnswerMedium(configs.get(QUICK_MEDIUM_ANSWER_INDEX));
            configuration.setQuickAnswerHard(configs.get(QUICK_HARD_ANSWER_INDEX));
            configuration.setTimeForBonusEasy(configs.get(TIME_BONUS_EASY_INDEX));
            configuration.setTimeForBonusMedium(configs.get(TIME_BONUS_MEDIUM_INDEX));
            configuration.setTimeForBonusHard(configs.get(TIME_BONUS_HARD_INDEX));
            configuration.setWrongAnswerEasy(configs.get(PENALTY_WRONG_EASY_INDEX));
            configuration.setWrongAnswerMedium(configs.get(PENALTY_WRONG_MEDIUM_INDEX));
            configuration.setWrongAnswerHard(configs.get(PENALTY_WRONG_HARD_INDEX));
            configuration.setCluePenaltyEasy(configs.get(PENALTY_CLUE_EASY_INDEX));
            configuration.setCluePenaltyMedium(configs.get(PENALTY_CLUE_MEDIUM_INDEX));
            configuration.setCluePenaltyHard(configs.get(PENALTY_CLUE_HARD_INDEX));
            configuration.setSkipPuzzlePenalty(configs.get(PENALTY_SKIP_PUZZLE_INDEX));
            configuration.setTotalTime(configs.get(TOTAL_START_TIME_INDEX));
            configuration.setTotalPuzzles(configs.get(NUMBER_OF_PUZZLES_INDEX));

            configurationDao.insert(configuration);
            news.add(configuration.getObjectId());
            return true;
        } else if (!configuration.getUpdatedAt().equals(pObject.getUpdatedAt())) {
            configuration.setUpdatedAt(pObject.getUpdatedAt());

            configuration.setGameRules(pObject.getString("gameRules"));
            configuration.setBase64EncodedPublicKey(pObject.getString("base64EncodedPublicKey"));

            List<Integer> configs = DawnUtilities.getConfigurations(pObject.getString("configVariables"));
            configuration.setCorrectAnswerEasy(configs.get(CORRECT_EASY_ANSWER_INDEX));
            configuration.setCorrectAnswerMedium(configs.get(CORRECT_MEDIUM_ANSWER_INDEX));
            configuration.setCorrectAnswerHard(configs.get(CORRECT_HARD_ANSWER_INDEX));
            configuration.setQuickAnswerEasy(configs.get(QUICK_EASY_ANSWER_INDEX));
            configuration.setQuickAnswerMedium(configs.get(QUICK_MEDIUM_ANSWER_INDEX));
            configuration.setQuickAnswerHard(configs.get(QUICK_HARD_ANSWER_INDEX));
            configuration.setTimeForBonusEasy(configs.get(TIME_BONUS_EASY_INDEX));
            configuration.setTimeForBonusMedium(configs.get(TIME_BONUS_MEDIUM_INDEX));
            configuration.setTimeForBonusHard(configs.get(TIME_BONUS_HARD_INDEX));
            configuration.setWrongAnswerEasy(configs.get(PENALTY_WRONG_EASY_INDEX));
            configuration.setWrongAnswerMedium(configs.get(PENALTY_WRONG_MEDIUM_INDEX));
            configuration.setWrongAnswerHard(configs.get(PENALTY_WRONG_HARD_INDEX));
            configuration.setCluePenaltyEasy(configs.get(PENALTY_CLUE_EASY_INDEX));
            configuration.setCluePenaltyMedium(configs.get(PENALTY_CLUE_MEDIUM_INDEX));
            configuration.setCluePenaltyHard(configs.get(PENALTY_CLUE_HARD_INDEX));
            configuration.setSkipPuzzlePenalty(configs.get(PENALTY_SKIP_PUZZLE_INDEX));
            configuration.setTotalTime(configs.get(TOTAL_START_TIME_INDEX));
            configuration.setTotalPuzzles(configs.get(NUMBER_OF_PUZZLES_INDEX));

            configurationDao.update(configuration);
            news.add(configuration.getObjectId());
            return true;
        }
        current.add(configuration.getObjectId());
        return false;
    }

    public void fetchPuzzle(ParseObject pObject) throws ParseException {
        fetchPuzzle(pObject, null, null);
    }

    public Puzzle fetchPuzzle(ParseObject pObject, List<String> current, List<String> news) throws ParseException {
        Puzzle puzzle = puzzleDao.queryBuilder()
                .where(PuzzleDao.Properties.ObjectId.eq(pObject.getObjectId()))
                .unique();
        if (puzzle == null) {
            puzzle = new Puzzle();

            puzzle.setObjectId(pObject.getObjectId());
            puzzle.setUpdatedAt(pObject.getUpdatedAt());

            puzzle.setNumber(pObject.getInt("Number"));
            puzzle.setSet(pObject.getInt("Set"));
            puzzle.setAnswer(pObject.getString("Answer"));
            puzzle.setClues(DawnUtilities.parseClues(pObject.<String>getList("Clues")));
            puzzle.setBonusClue(pObject.getString("BonusClue"));
            puzzle.setDifficulty(pObject.getInt("Difficulty"));
            puzzle.setImage(
                    DawnUtilities.convertToByte(
                            loader.loadImageSync(
                                    pObject.getParseFile("Image").getUrl(),
                                    screenSize
                            )
                    )
            );

            puzzleDao.insert(puzzle);
            if (news != null) {
                news.add(puzzle.getObjectId());
            }
            return puzzle;
        } else if (!puzzle.getUpdatedAt().equals(pObject.getUpdatedAt())) {
            puzzle.setUpdatedAt(pObject.getUpdatedAt());

            puzzle.setNumber(pObject.getInt("Number"));
            puzzle.setSet(pObject.getInt("Set"));
            puzzle.setAnswer(pObject.getString("Answer"));
            puzzle.setClues(DawnUtilities.parseClues(pObject.<String>getList("Clues")));
            puzzle.setBonusClue(pObject.getString("BonusClue"));
            puzzle.setDifficulty(pObject.getInt("Difficulty"));
            puzzle.setImage(
                    DawnUtilities.convertToByte(
                            loader.loadImageSync(
                                    pObject.getParseFile("Image").getUrl(),
                                    screenSize
                            )
                    )
            );

            puzzleDao.update(puzzle);
            if (news != null) {
                news.add(puzzle.getObjectId());
            }
            return puzzle;
        }
        if (current != null) {
            current.add(puzzle.getObjectId());
        }
        return puzzle;
    }

    public void fetchScore(ParseObject pObject) {
        fetchScore(pObject, null, null);
    }

    private boolean fetchScore(ParseObject pObject, List<String> current, List<String> news) {
        Score score = scoreDao.queryBuilder()
                .where(ScoreDao.Properties.ObjectId.eq(pObject.getObjectId()))
                .unique();
        if (score == null) {
            score = new Score();

            score.setObjectId(pObject.getObjectId());
            score.setUpdatedAt(pObject.getUpdatedAt());

            score.setSet(pObject.getInt("set"));
            score.setScore(pObject.getInt("score"));
            score.setElapsedTime(pObject.getInt("secondsElapsed"));
            score.setUserObjectId(pObject.getParseUser("user").getObjectId());

            User user = userDao.queryBuilder()
                    .where(UserDao.Properties.ObjectId.eq(score.getUserObjectId()))
                    .uniqueOrThrow();
            score.setUserId(user.getId());

            scoreDao.insert(score);
            if (news != null) {
                news.add(score.getObjectId());
            }
            return true;
        } else if (!score.getUpdatedAt().equals(pObject.getUpdatedAt())) {
            score.setUpdatedAt(pObject.getUpdatedAt());

            score.setSet(pObject.getInt("set"));
            score.setScore(pObject.getInt("score"));
            score.setElapsedTime(pObject.getInt("secondsElapsed"));
            score.setUserObjectId(pObject.getParseUser("user").getObjectId());

            User user = userDao.queryBuilder()
                    .where(UserDao.Properties.ObjectId.eq(score.getUserObjectId()))
                    .uniqueOrThrow();
            score.setUserId(user.getId());

            scoreDao.update(score);
            if (news != null) {
                news.add(score.getObjectId());
            }
            return true;
        }
        if (current != null) {
            current.add(score.getObjectId());
        }
        return false;
    }

    public void deleteFriendship(String objectId) {
        Friendship friendship = friendshipDao.queryBuilder()
                .where(FriendshipDao.Properties.ObjectId.eq(objectId))
                .uniqueOrThrow();
        friendshipDao.delete(friendship);
    }

    public Task deleteRecursive(List<String> current, List<String> news, Task task) {
        List<String> total = new ArrayList<String>();
        total.addAll(current);
        total.addAll(news);
        int count = 0;
        switch (task) {
            case DELETE_USER:
                List<User> oldUsers = userDao.queryBuilder()
                        .where(UserDao.Properties.ObjectId.notIn(total))
                        .list();
                for (User user : oldUsers) {
                    userDao.delete(user);
                    count++;
                    if (count == 100) {
                        setProgressStatus(count);
                        count = 0;
                    }
                }
                setProgressStatus(count);
                break;
            case DELETE_FRIENDSHIP:
                List<Friendship> oldFriendships = friendshipDao.queryBuilder()
                        .where(FriendshipDao.Properties.ObjectId.notIn(total))
                        .list();
                for (Friendship friendship : oldFriendships) {
                    friendshipDao.delete(friendship);
                    count++;
                    if (count == 100) {
                        setProgressStatus(count);
                        count = 0;
                    }
                }
                setProgressStatus(count);
                break;
            case DELETE_CONFIGURATION:
                List<Configuration> oldConfigurations = configurationDao.queryBuilder()
                        .where(ConfigurationDao.Properties.ObjectId.notIn(total))
                        .list();
                for (Configuration configuration : oldConfigurations) {
                    configurationDao.delete(configuration);
                    count++;
                    if (count == 100) {
                        setProgressStatus(count);
                        count = 0;
                    }
                }
                setProgressStatus(count);
                break;
            case DELETE_SCORE:
                List<Score> oldScores = scoreDao.queryBuilder()
                        .where(ScoreDao.Properties.ObjectId.notIn(total))
                        .list();
                for (Score score : oldScores) {
                    scoreDao.delete(score);
                    count++;
                    if (count == 100) {
                        setProgressStatus(count);
                        count = 0;
                    }
                }
                setProgressStatus(count);
                break;
        }
        return task;
    }

    public void notifyReceiver(Status status) {
        notifyReceiver(status, 0);
    }

    public void notifyReceiver(Status status, int progress) {
        if (receiver != null) {
            receiver.onNotified(status, progress);
        } else {
            L.e(getClass().getName(), "receiver is null");
        }
    }

    public UserDao getUserDao() {
        return userDao;
    }

    public FriendshipDao getFriendshipDao() {
        return friendshipDao;
    }

    public ConfigurationDao getConfigurationDao() {
        return configurationDao;
    }

    public PuzzleDao getPuzzleDao() {
        return puzzleDao;
    }

    public ScoreDao getScoreDao() {
        return scoreDao;
    }

    public void login() {
        this.loggedIn = true;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public int getProgressStatus() {
        return progressStatus;
    }

    public void setProgressStatus(int value) {
        this.progressStatus = progress.addAndGet(value) * 100 / totalProgress.get();
        L.e(String.valueOf(progress.get()), String.valueOf(totalProgress.get()));
        L.e("progress", String.valueOf(progressStatus));
        if (progressStatus != 100) {
            notifyReceiver(Status.NONE);
        } else {
            DialogManager.closeProgress();
            notifyReceiver(Status.DATABASE_READY);
        }
    }

    public AtomicInteger getTotalProgress() {
        return totalProgress;
    }

    public void setTotalProgress(int value) {
        totalProgress.set(value);
    }

    public void setReceiver(DawnReceiver receiver) {
        this.receiver = receiver;
        this.context = (Context) receiver;
        notifyReceiver(Status.DATABASE_READY);
    }

    public int getHighestSet() {
        return highestSet;
    }

    public void setHighestSet(int highestSet) {
        this.highestSet = highestSet;
    }

    public Context getContext() {
        return context;
    }

    public ParseQuery<ParseUser> getUserQuery() {
        return ParseUser.getQuery();
    }

    public ParseQuery<ParseObject> getConfigurationQuery() {
        return ParseQuery.getQuery("Configurations");
    }

    public ParseQuery<ParseObject> getPuzzleQuery() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            return ParseQuery.getQuery("Puzzles").whereContainedIn("Set", Arrays.asList(1, 2, 3, 4, 5));
        } else {
            int nextSetToBuy = currentUser.getInt("nextSetToBuy");
            List<Integer> boughtSets = new ArrayList<Integer>();
            for (int i = 1; i < nextSetToBuy; i++) {
                boughtSets.add(i);
            }
            return ParseQuery.getQuery("Puzzles").whereContainedIn("Set", boughtSets);
        }
    }

    public ParseQuery<ParseObject> getFriendshipQuery() {
        return ParseQuery.getQuery("Friendships");
    }

    public ParseQuery<ParseObject> getScoreQuery() {
        return ParseQuery.getQuery("Scores");
    }

    public ImageLoader getLoader() {
        if (loader == null) {
            loader = ImageLoader.getInstance();
        }
        return loader;
    }

    private void refreshCurrentUser() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        user = userDao.queryBuilder()
                .where(UserDao.Properties.ObjectId.eq(currentUser.getObjectId()))
                .uniqueOrThrow();
    }

    public User getCurrentUser() {
        if (user == null && loggedIn) {
            L.e("here", "get in");
            ParseUser currentUser = ParseUser.getCurrentUser();
            if (currentUser == null) {
                return null;
            }
            user = userDao.queryBuilder()
                    .where(UserDao.Properties.ObjectId.eq(currentUser.getObjectId()))
                    .uniqueOrThrow();
        }
        return user;
    }

    public void logout() {
        loggedIn = false;
        user = null;
    }

    public List<User> getFriends() {
        User user = getCurrentUser();
        List<User> friends = new ArrayList<User>();
        List<Friendship> receiverFriendships = user.getRequestReceiverFriendships();
        List<Friendship> senderFriendships = user.getRequestSenderFriendships();
        for (Friendship friendship : receiverFriendships) {
            if (friendship.getStatus() == ACCEPT) {
                friends.add(friendship.getRequestSender());
            }
        }
        for (Friendship friendship : senderFriendships) {
            if (friendship.getStatus() == ACCEPT) {
                friends.add(friendship.getRequestReceiver());
            }
        }
        return friends;
    }

    public List<User> getWaiting() {
        User user = getCurrentUser();
        List<User> waiting = new ArrayList<User>();
        List<Friendship> receiverFriendships = user.getRequestReceiverFriendships();
        for (Friendship friendship : receiverFriendships) {
            if (friendship.getStatus() == PENDING) {
                waiting.add(friendship.getRequestSender());
            }
        }
        return waiting;
    }

    public List<User> getPending() {
        User user = getCurrentUser();
        List<User> pending = new ArrayList<User>();
        List<Friendship> senderFriendships = user.getRequestSenderFriendships();
        for (Friendship friendship : senderFriendships) {
            if (friendship.getStatus() == PENDING) {
                pending.add(friendship.getRequestReceiver());
            }
        }
        return pending;
    }

    public int getNumOfRequests() {
        if (getCurrentUser() == null) {
            return 0;
        }
        int total = 0;
        List<Friendship> friendships = getCurrentUser().getRequestReceiverFriendships();
        for (Friendship friendship : friendships) {
            if (!friendship.getSeenRequest()) {
                total++;
            }
        }
        return total;
    }

    public void clearAllRequests() {
        if (getCurrentUser() == null) {
            return;
        }
        List<Friendship> friendships = getCurrentUser().getRequestReceiverFriendships();
        for (Friendship friendship : friendships) {
            getFriendshipQuery().getInBackground(friendship.getObjectId(), new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject parseObject, ParseException e) {
                    if (e == null) {
                        parseObject.put("hasSeenRequestNotification", true);
                        parseObject.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e != null) {
                                    L.e("Mark read", e.getMessage(), e);
                                }
                            }
                        });
                    } else {
                        L.e("Mark notifications as read", e.getMessage(), e);
                    }
                }
            });
        }
    }

    public void collectRequests(String task, String objId, String message) {
        if (receiver != null) {
            DialogManager.show((Context) receiver, message);
            DawnIntentService.startActionHandleRequest(((Activity) receiver), task, objId);
        } else {
            if (requests == null) {
                requests = Collections.synchronizedList(new ArrayList<DawnUtilities.Triple>());
            }
            requests.add(new DawnUtilities.Triple(message, task, objId));
        }
    }

    public void executeRequests() {
        if (requests != null && !requests.isEmpty() && receiver != null) {
            String messages = "";
            for (DawnUtilities.Triple request : requests) {
                messages += request.start + "\n";
                DawnIntentService.startActionHandleRequest(((Activity) receiver), request.middle, request.end);
            }
            requests.clear();
            DialogManager.show((Context) receiver, messages.substring(0, messages.length() - 1));
        }
    }

    public enum Task {
        FETCH_USER,
        FETCH_FRIENDSHIP,
        FETCH_CONFIGURATION,
        FETCH_PUZZLE,
        FETCH_SCORE,
        DELETE_USER,
        DELETE_FRIENDSHIP,
        DELETE_CONFIGURATION,
        DELETE_PUZZLE,
        DELETE_SCORE
    }

    public enum Status {
        NONE,
        DATABASE_READY,
        SCORE_PUBLISHED,
        PUZZLES_READY,
        PREPARING_PUZZLES,
        FRIENDSHIP_REQUEST_HANDLED,
        FRIENDSHIP_REQUEST_SENT,
        LOGOUT,
        AVATAR_UPLOADED
    }

    public interface DawnReceiver {
        public void onNotified(Status status, int progress);
    }
}
