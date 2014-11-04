package com.evilgeniustechnologies.Wordrific.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;

import com.evilgeniustechnologies.Wordrific.activities.DawnActivity;
import com.evilgeniustechnologies.Wordrific.daomodel.Friendship;
import com.evilgeniustechnologies.Wordrific.daomodel.FriendshipDao;
import com.evilgeniustechnologies.Wordrific.daomodel.User;
import com.evilgeniustechnologies.Wordrific.models.DawnDatabase;
import com.evilgeniustechnologies.Wordrific.utilties.DawnUtilities;
import com.evilgeniustechnologies.Wordrific.utilties.DialogManager;
import com.evilgeniustechnologies.Wordrific.utilties.L;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.PushService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class DawnIntentService extends IntentService {
    private static final String ACTION_HANDLE_REQUEST = "com.evilgeniustechnologies.Wordrific.services.action.HANDLE_REQUEST";
    private static final String ACTION_SEND_REQUEST = "com.evilgeniustechnologies.Wordrific.services.action.SEND_REQUEST";
    private static final String ACTION_ACCEPT_REQUEST = "com.evilgeniustechnologies.Wordrific.services.action.ACCEPT_REQUEST";
    private static final String ACTION_REJECT_REQUEST = "com.evilgeniustechnologies.Wordrific.services.action.REJECT_REQUEST";
    private static final String ACTION_DELETE_REQUEST = "com.evilgeniustechnologies.Wordrific.services.action.DELETE_REQUEST";
    private static final String ACTION_PUBLISH_SCORE = "com.evilgeniustechnologies.Wordrific.services.action.PUBLISH_SCORE";
    private static final String ACTION_LOAD_PUZZLES = "com.evilgeniustechnologies.Wordrific.services.action.LOAD_PUZZLES";
    private static final String ACTION_CHANGE_FIRST_NAME = "com.evilgeniustechnologies.Wordrific.services.action.CHANGE_FIRST_NAME";
    private static final String ACTION_CHANGE_LAST_NAME = "com.evilgeniustechnologies.Wordrific.services.action.CHANGE_LAST_NAME";
    private static final String ACTION_UPLOAD_AVATAR = "com.evilgeniustechnologies.Wordrific.services.action.UPLOAD_AVATAR";
    private static final String ACTION_LOGIN = "com.evilgeniustechnologies.Wordrific.services.action.LOGIN";
    private static final String ACTION_LOGOUT = "com.evilgeniustechnologies.Wordrific.services.action.LOGOUT";
    private static final String ACTION_REGISTER = "com.evilgeniustechnologies.Wordrific.services.action.REGISTER";

    private static final String EXTRA_TASK = "com.evilgeniustechnologies.Wordrific.services.extra.TASK";
    private static final String EXTRA_FRIENDSHIP_OBJECT_ID = "com.evilgeniustechnologies.Wordrific.services.extra.FRIENDSHIP_OBJECT_ID";
    private static final String EXTRA_USER_OBJECT_ID = "com.evilgeniustechnologies.Wordrific.services.extra.USER_OBJECT_ID";
    private static final String EXTRA_SCORE = "com.evilgeniustechnologies.Wordrific.services.extra.SCORE";
    private static final String EXTRA_TIME = "com.evilgeniustechnologies.Wordrific.services.extra.TIME";
    private static final String EXTRA_SET = "com.evilgeniustechnologies.Wordrific.services.extra.SET";
    private static final String EXTRA_FIRST_NAME = "com.evilgeniustechnologies.Wordrific.services.extra.FIRST_NAME";
    private static final String EXTRA_LAST_NAME = "com.evilgeniustechnologies.Wordrific.services.extra.LAST_NAME";
    private static final String EXTRA_URI = "com.evilgeniustechnologies.Wordrific.services.extra.URI";
    private static final String EXTRA_EMAIL = "com.evilgeniustechnologies.Wordrific.services.extra.EMAIL";
    private static final String EXTRA_PASSWORD = "com.evilgeniustechnologies.Wordrific.services.extra.PASSWORD";

    private DawnDatabase database;

    public DawnIntentService() {
        super("DawnIntentService");
    }

    /**
     * Starts this service to perform action HandleRequest with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionHandleRequest(Context context, String task, String friendshipObjId) {
        if (!DawnUtilities.isConnected(context)) {
            DialogManager.show(context, DialogManager.Alert.NO_INTERNET);
            return;
        }
        Intent intent = new Intent(context, DawnIntentService.class);
        intent.setAction(ACTION_HANDLE_REQUEST);
        intent.putExtra(EXTRA_TASK, task);
        intent.putExtra(EXTRA_FRIENDSHIP_OBJECT_ID, friendshipObjId);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action SendRequest with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionSendRequest(Context context, String userObjId) {
        if (!DawnUtilities.isConnected(context)) {
            DialogManager.show(context, DialogManager.Alert.NO_INTERNET);
            return;
        }
        DialogManager.show(context, DialogManager.Alert.PROGRESS);
        Intent intent = new Intent(context, DawnIntentService.class);
        intent.setAction(ACTION_SEND_REQUEST);
        intent.putExtra(EXTRA_USER_OBJECT_ID, userObjId);
        context.startService(intent);
    }

    public static void startActionAcceptRequest(Context context, String userObjId) {
        if (!DawnUtilities.isConnected(context)) {
            DialogManager.show(context, DialogManager.Alert.NO_INTERNET);
            return;
        }
        DialogManager.show(context, DialogManager.Alert.PROGRESS);
        Intent intent = new Intent(context, DawnIntentService.class);
        intent.setAction(ACTION_ACCEPT_REQUEST);
        intent.putExtra(EXTRA_USER_OBJECT_ID, userObjId);
        context.startService(intent);
    }

    public static void startActionRejectRequest(Context context, String userObjId) {
        if (!DawnUtilities.isConnected(context)) {
            DialogManager.show(context, DialogManager.Alert.NO_INTERNET);
            return;
        }
        DialogManager.show(context, DialogManager.Alert.PROGRESS);
        Intent intent = new Intent(context, DawnIntentService.class);
        intent.setAction(ACTION_REJECT_REQUEST);
        intent.putExtra(EXTRA_USER_OBJECT_ID, userObjId);
        context.startService(intent);
    }

    public static void startActionDeleteRequest(Context context, String userObjId) {
        if (!DawnUtilities.isConnected(context)) {
            DialogManager.show(context, DialogManager.Alert.NO_INTERNET);
            return;
        }
        DialogManager.show(context, DialogManager.Alert.PROGRESS);
        Intent intent = new Intent(context, DawnIntentService.class);
        intent.setAction(ACTION_DELETE_REQUEST);
        intent.putExtra(EXTRA_USER_OBJECT_ID, userObjId);
        context.startService(intent);
    }

    public static void startActionPublishScore(Context context, int score, int time, int set) {
        if (!DawnUtilities.isConnected(context)) {
            DialogManager.show(context, DialogManager.Alert.NO_INTERNET);
            return;
        }
        DialogManager.show(context, DialogManager.Alert.PROGRESS);
        Intent intent = new Intent(context, DawnIntentService.class);
        intent.setAction(ACTION_PUBLISH_SCORE);
        intent.putExtra(EXTRA_SCORE, score);
        intent.putExtra(EXTRA_TIME, time);
        intent.putExtra(EXTRA_SET, set);
        context.startService(intent);
    }

    public static void startActionLoadPuzzles(Context context, int set) {
        if (!DawnUtilities.isConnected(context)) {
            DialogManager.show(context, DialogManager.Alert.NO_INTERNET);
            return;
        }
        DialogManager.show(context, DialogManager.Alert.PROGRESS_BAR, "Preparing puzzles");
        Intent intent = new Intent(context, DawnIntentService.class);
        intent.setAction(ACTION_LOAD_PUZZLES);
        intent.putExtra(EXTRA_SET, set);
        context.startService(intent);
    }

    public static void startActionChangeFirstName(Context context, String firstName) {
        if (!DawnUtilities.isConnected(context)) {
            DialogManager.show(context, DialogManager.Alert.NO_INTERNET);
            return;
        }
        Intent intent = new Intent(context, DawnIntentService.class);
        intent.setAction(ACTION_CHANGE_FIRST_NAME);
        intent.putExtra(EXTRA_FIRST_NAME, firstName);
        context.startService(intent);
    }

    public static void startActionChangeLastName(Context context, String lastName) {
        if (!DawnUtilities.isConnected(context)) {
            DialogManager.show(context, DialogManager.Alert.NO_INTERNET);
            return;
        }
        Intent intent = new Intent(context, DawnIntentService.class);
        intent.setAction(ACTION_CHANGE_LAST_NAME);
        intent.putExtra(EXTRA_LAST_NAME, lastName);
        context.startService(intent);
    }

    public static void startActionUploadAvatar(Context context, Uri uri) {
        if (!DawnUtilities.isConnected(context)) {
            DialogManager.show(context, DialogManager.Alert.NO_INTERNET);
            return;
        }
        DialogManager.show(context, DialogManager.Alert.PROGRESS);
        Intent intent = new Intent(context, DawnIntentService.class);
        intent.setAction(ACTION_UPLOAD_AVATAR);
        intent.putExtra(EXTRA_URI, uri);
        context.startService(intent);
    }

    public static void startActionLogin(Context context, String email, String password) {
        if (!DawnUtilities.isConnected(context)) {
            DialogManager.show(context, DialogManager.Alert.NO_INTERNET);
            return;
        }
        DialogManager.show(context, DialogManager.Alert.PROGRESS);
        Intent intent = new Intent(context, DawnIntentService.class);
        intent.setAction(ACTION_LOGIN);
        intent.putExtra(EXTRA_EMAIL, email);
        intent.putExtra(EXTRA_PASSWORD, password);
        context.startService(intent);
    }

    public static void startActionLogout(Context context) {
        if (!DawnUtilities.isConnected(context)) {
            DialogManager.show(context, DialogManager.Alert.NO_INTERNET);
            return;
        }
        DialogManager.show(context, DialogManager.Alert.PROGRESS);
        Intent intent = new Intent(context, DawnIntentService.class);
        intent.setAction(ACTION_LOGOUT);
        context.startService(intent);
    }

    public static void startActionRegister(Context context, String email, String password, String firstName, String lastName) {
        if (!DawnUtilities.isConnected(context)) {
            DialogManager.show(context, DialogManager.Alert.NO_INTERNET);
            return;
        }
        DialogManager.show(context, DialogManager.Alert.PROGRESS);
        Intent intent = new Intent(context, DawnIntentService.class);
        intent.setAction(ACTION_REGISTER);
        intent.putExtra(EXTRA_EMAIL, email);
        intent.putExtra(EXTRA_PASSWORD, password);
        intent.putExtra(EXTRA_FIRST_NAME, firstName);
        intent.putExtra(EXTRA_LAST_NAME, lastName);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            L.e("intent", intent.getAction());
            database = DawnDatabase.getInstance();
            final String action = intent.getAction();
            try {
                if (ACTION_HANDLE_REQUEST.equals(action)) {
                    final String task = intent.getStringExtra(EXTRA_TASK);
                    final String friendshipObjId = intent.getStringExtra(EXTRA_FRIENDSHIP_OBJECT_ID);
                    handleActionIncomingRequests(task, friendshipObjId);
                } else if (ACTION_SEND_REQUEST.equals(action) ||
                        ACTION_ACCEPT_REQUEST.equals(action) ||
                        ACTION_REJECT_REQUEST.equals(action) ||
                        ACTION_DELETE_REQUEST.equals(action)) {
                    final String userObjId = intent.getStringExtra(EXTRA_USER_OBJECT_ID);
                    handleActionSendingRequests(action, userObjId);
                } else if (ACTION_PUBLISH_SCORE.equals(action)) {
                    final int score = intent.getIntExtra(EXTRA_SCORE, 0);
                    final int time = intent.getIntExtra(EXTRA_TIME, 0);
                    final int set = intent.getIntExtra(EXTRA_SET, 0);
                    handleActionPublishScore(score, time, set);
                } else if (ACTION_LOAD_PUZZLES.equals(action)) {
                    final int set = intent.getIntExtra(EXTRA_SET, 0);
                    handleActionLoadPuzzles(set);
                } else if (ACTION_CHANGE_FIRST_NAME.equals(action)) {
                    final String firstName = intent.getStringExtra(EXTRA_FIRST_NAME);
                    handleActionChangeFirstName(firstName);
                } else if (ACTION_CHANGE_LAST_NAME.equals(action)) {
                    final String lastName = intent.getStringExtra(EXTRA_LAST_NAME);
                    handleActionChangeLastName(lastName);
                } else if (ACTION_UPLOAD_AVATAR.equals(action)) {
                    final Uri uri = intent.getParcelableExtra(EXTRA_URI);
                    handleActionUploadAvatar(uri);
                } else if (ACTION_LOGIN.equals(action)) {
                    final String email = intent.getStringExtra(EXTRA_EMAIL);
                    final String password = intent.getStringExtra(EXTRA_PASSWORD);
                    handleActionLogin(email, password);
                } else if (ACTION_LOGOUT.equals(action)) {
                    handleActionLogout();
                } else if (ACTION_REGISTER.equals(action)) {
                    final String email = intent.getStringExtra(EXTRA_EMAIL);
                    final String password = intent.getStringExtra(EXTRA_PASSWORD);
                    final String firstName = intent.getStringExtra(EXTRA_FIRST_NAME);
                    final String lastName = intent.getStringExtra(EXTRA_LAST_NAME);
                    handleActionRegister(email, password, firstName, lastName);
                }
            } catch (ParseException e) {
                L.e(action, e.getMessage(), e);
                DialogManager.setMessage(e.getMessage());
                database.notifyReceiver(DawnDatabase.Status.DATABASE_READY);
            } catch (JSONException e) {
                L.e(action, e.getMessage(), e);
            } catch (FileNotFoundException e) {
                L.e(action, e.getMessage(), e);
            } finally {
                DialogManager.closeProgress();
            }
        }
    }

    /**
     * Handle action HandleRequest in the provided background thread with the provided
     * parameters.
     */
    private void handleActionIncomingRequests(String task, String friendshipObjId) throws ParseException {
        if (ACTION_SEND_REQUEST.equals(task) ||
                ACTION_ACCEPT_REQUEST.equals(task)) {
            // Get target friendship ParseObject
            ParseQuery<ParseObject> fQuery = database.getFriendshipQuery();
            ParseObject friendship = fQuery.get(friendshipObjId);
            // Reload target friendship from Parse
            database.fetchFriendship(friendship);
            // Refresh data
            database.refresh();
            // Notify main thread activity
            database.notifyReceiver(DawnDatabase.Status.FRIENDSHIP_REQUEST_HANDLED);
        } else if (ACTION_REJECT_REQUEST.equals(task) ||
                ACTION_DELETE_REQUEST.equals(task)) {
            // Delete target friendship
            database.deleteFriendship(friendshipObjId);
            // Refresh data
            database.refresh();
            // Notify main thread activity
            database.notifyReceiver(DawnDatabase.Status.FRIENDSHIP_REQUEST_HANDLED);
        }
    }

    /**
     * Handle action SendRequest in the provided background thread with the provided
     * parameters.
     */
    private void handleActionSendingRequests(String action, String userObjId) throws ParseException, JSONException {
        if (ACTION_SEND_REQUEST.equals(action)) {
            // Get current logged in user
            ParseUser currentUser = ParseUser.getCurrentUser();
            // Get target user
            ParseUser parseUser = database.getUserQuery().get(userObjId);
            // Construct a new friendship ParseObject
            ParseObject friendship = new ParseObject("Friendships");
            friendship.put("status", DawnDatabase.PENDING);
            friendship.put("hasSeenRequestNotification", false);
            friendship.put("requester", currentUser);
            friendship.put("requestee", parseUser);
            // Set relation
            ParseRelation<ParseUser> relation = friendship.getRelation("partners");
            relation.add(currentUser);
            relation.add(parseUser);
            // Save the new friendship to Parse
            friendship.save();
            // Load the new friendship from Parse
            database.fetchFriendship(friendship);
            // Refresh data
            database.refresh();
            // Construct a new push notification
            ParsePush push = new ParsePush();
            push.setChannel("user_" + parseUser.getObjectId());
            JSONObject json = new JSONObject("{" +
                    "\"alert\": \"" + database.getCurrentUser().getFirstName() + " " + database.getCurrentUser().getLastName() + " has sent you a friend request.\"," +
                    "\"action\": \"com.evilgeniustechnologies.Wordrific.UPDATE_STATUS\"," +
                    "\"task\": \"" + action + "\"," +
                    "\"friendshipObjId\": \"" + friendship.getObjectId() +
                    "\"}");
            L.e("json", json.toString());
            push.setData(json);
            // Send notification
            push.send();
            // Notify main thread activity
            database.notifyReceiver(DawnDatabase.Status.FRIENDSHIP_REQUEST_SENT);
        } else if (ACTION_ACCEPT_REQUEST.equals(action)) {
            // Get current logged in user
            User currentUser = database.getCurrentUser();
            // Get the friendship between current user and the target user
            Friendship friendship = database.getFriendshipDao()
                    .queryBuilder()
                    .where(FriendshipDao.Properties.RequestSenderObjectId.eq(userObjId),
                            FriendshipDao.Properties.RequestReceiverObjectId.eq(currentUser.getObjectId()))
                    .uniqueOrThrow();
            // Get and update friendship ParseObject
            ParseObject pFriendship = database.getFriendshipQuery().get(friendship.getObjectId());
            pFriendship.put("status", DawnDatabase.ACCEPT);
            // Save friendship to Parse
            pFriendship.save();
            // Reload friendship from Parse
            database.fetchFriendship(pFriendship);
            // Refresh data
            database.refresh();
            // Construct a new push notification
            ParsePush push = new ParsePush();
            push.setChannel("user_" + userObjId);
            JSONObject json = new JSONObject("{" +
                    "\"alert\": \"" + database.getCurrentUser().getFirstName() + " " + database.getCurrentUser().getLastName() + " has accepted your friend request.\"," +
                    "\"action\": \"com.evilgeniustechnologies.Wordrific.UPDATE_STATUS\"," +
                    "\"task\": \"" + action + "\"," +
                    "\"friendshipObjId\": \"" + friendship.getObjectId() +
                    "\"}");
            L.e("json", json.toString());
            push.setData(json);
            // Send notification
            push.send();
            // Notify main thread activity
            database.notifyReceiver(DawnDatabase.Status.FRIENDSHIP_REQUEST_SENT);
        } else if (ACTION_REJECT_REQUEST.equals(action)) {
            // Get current logged in user
            User currentUser = database.getCurrentUser();
            // Get the friendship between current user and the target user
            final Friendship friendship = database.getFriendshipDao()
                    .queryBuilder()
                    .where(FriendshipDao.Properties.RequestSenderObjectId.eq(userObjId),
                            FriendshipDao.Properties.RequestReceiverObjectId.eq(currentUser.getObjectId()))
                    .uniqueOrThrow();
            // Get the friendship ParseObject
            ParseObject pFriendship = database.getFriendshipQuery().get(friendship.getObjectId());
            // Delete the friendship on Parse
            pFriendship.delete();
            // Delete the friendship on local database
            database.getFriendshipDao().delete(friendship);
            // Refresh data
            database.refresh();
            // Construct a new push notification
            ParsePush push = new ParsePush();
            push.setChannel("user_" + userObjId);
            JSONObject json = new JSONObject("{" +
                    "\"alert\": \"" + database.getCurrentUser().getFirstName() + " " + database.getCurrentUser().getLastName() + " has rejected your friend request.\"," +
                    "\"action\": \"com.evilgeniustechnologies.Wordrific.UPDATE_STATUS\"," +
                    "\"task\": \"" + action + "\"," +
                    "\"friendshipObjId\": \"" + friendship.getObjectId() +
                    "\"}");
            L.e("json", json.toString());
            push.setData(json);
            // Send push notification
            push.send();
            // Notify main thread activity
            database.notifyReceiver(DawnDatabase.Status.FRIENDSHIP_REQUEST_SENT);
        } else if (ACTION_DELETE_REQUEST.equals(action)) {
            // Get current logged in user
            User currentUser = database.getCurrentUser();
            // Get the friendship between current user and the target user
            Friendship friendship = database.getFriendshipDao()
                    .queryBuilder()
                    .where(FriendshipDao.Properties.RequestSenderObjectId.eq(userObjId),
                            FriendshipDao.Properties.RequestReceiverObjectId.eq(currentUser.getObjectId()))
                    .unique();
            if (friendship == null) {
                friendship = database.getFriendshipDao()
                        .queryBuilder()
                        .where(FriendshipDao.Properties.RequestReceiverObjectId.eq(userObjId),
                                FriendshipDao.Properties.RequestSenderObjectId.eq(currentUser.getObjectId()))
                        .uniqueOrThrow();
            }
            // Get the friendship ParseObject
            ParseObject pFriendship = database.getFriendshipQuery().get(friendship.getObjectId());
            // Delete the friendship from Parse
            pFriendship.delete();
            // Delete the friendship from local database
            database.getFriendshipDao().delete(friendship);
            // Refresh data
            database.refresh();
            // Construct a new push notification
            ParsePush push = new ParsePush();
            push.setChannel("user_" + userObjId);
            JSONObject json = new JSONObject("{" +
                    "\"alert\": \"" + database.getCurrentUser().getFirstName() + " " + database.getCurrentUser().getLastName() + " has unfriended with you.\"," +
                    "\"action\": \"com.evilgeniustechnologies.Wordrific.UPDATE_STATUS\"," +
                    "\"task\": \"" + action + "\"," +
                    "\"friendshipObjId\": \"" + friendship.getObjectId() +
                    "\"}");
            L.e("json", json.toString());
            push.setData(json);
            // Send push notification
            push.send();
            // Notify main thread activity
            database.notifyReceiver(DawnDatabase.Status.FRIENDSHIP_REQUEST_SENT);
        }
    }

    private void handleActionPublishScore(int score, int time, int set) throws ParseException {
        // Get current user
        User user = DawnDatabase.getInstance().getCurrentUser();
        // Check and save best score
        if (user.getBestScore() == null || user.getBestScore() < score) {
            ParseUser currentUser = ParseUser.getCurrentUser();
            currentUser.put("bestScore", score);
            currentUser.save();
        }
        // Construct a new score ParseObject
        ParseObject pScore = new ParseObject("Scores");
        pScore.put("score", score);
        pScore.put("secondsElapsed", time);
        pScore.put("set", set);
        pScore.put("user", ParseUser.getCurrentUser());
        // Save the new score to Parse
        pScore.save();
        // Load the the new score from Parse
        database.fetchScore(pScore);
        // Refresh data
        database.refresh();
        // Notify main thread activity
        database.notifyReceiver(DawnDatabase.Status.SCORE_PUBLISHED);
    }

    private void handleActionLoadPuzzles(int set) throws ParseException {
        int count = 0;
        List<ParseObject> pPuzzle = database.getPuzzleQuery().whereEqualTo("Set", set).find();
        for (ParseObject parseObject : pPuzzle) {
            // Load puzzles from Parse
            database.fetchPuzzle(parseObject);
            // Send progress to main thread activity
            database.notifyReceiver(DawnDatabase.Status.PREPARING_PUZZLES, ++count * 100 / 20);
        }
        // Notify main thread activity
        database.notifyReceiver(DawnDatabase.Status.PUZZLES_READY);
    }

    private void handleActionChangeFirstName(String firstName) throws ParseException {
        // Get the current logged in user ParseObject
        ParseUser user = ParseUser.getCurrentUser();
        // Put the new first name
        user.put("firstName", firstName);
        // Save user to Parse
        user.save();
        // Load user from Parse
        database.fetchUser(user);
        // Refresh data
        database.refresh();
    }

    private void handleActionChangeLastName(String lastName) throws ParseException {
        // Get the current logged in user ParseObject
        ParseUser user = ParseUser.getCurrentUser();
        // Put the new last name
        user.put("lastName", lastName);
        // Save user to Parse
        user.save();
        // Load user from Parse
        database.fetchUser(user);
        // Refresh data
        database.refresh();
    }

    private void handleActionUploadAvatar(Uri uri) throws ParseException, FileNotFoundException {
        // Get the path of the selected image
        String imagePath = DawnUtilities.getFileName(this, uri);
        // Construct a new bitmap object from the image path
        Bitmap image = DawnUtilities.decodeUri(this, uri);
        // Construct a new ParseFile to store the bitmap data
        ParseFile pFile = new ParseFile(imagePath, DawnUtilities.convertToByte(image));
        // Get the current logged in user
        ParseUser user = ParseUser.getCurrentUser();
        // Put the new ParseFile
        user.put("avatar", pFile);
        // Save user to Parse
        user.save();
        // Reload current user from Parse
        database.fetchUser(user);
        // Refresh data
        database.refresh();
        // Notify main thread activity
        database.notifyReceiver(DawnDatabase.Status.AVATAR_UPLOADED);
    }

    private void login(String email, String password) throws ParseException {
        // Log in using Parse API
        L.e("parse login");
        ParseUser.logIn(email, password);
        // If user has not logged in, return
        if (ParseUser.getCurrentUser() == null || !ParseUser.getCurrentUser().isAuthenticated()) {
            return;
        }
        L.e("get list of channels");
        List channels = ParseInstallation.getCurrentInstallation().getList("channels");
        if (channels == null || !channels.contains("user_" + ParseUser.getCurrentUser().getObjectId())) {
            // Subscribe channel
            L.e("subscribe channel");
            PushService.subscribe(getApplicationContext(), "user_" + ParseUser.getCurrentUser().getObjectId(), DawnActivity.class);
        }
        L.e("database login");
        // Set status as logged in
        database.login();
    }

    private void handleActionLogin(String email, String password) throws ParseException {
        L.e("start of login");
        // Login in background
        login(email, password);
        // Set alert dialog
        L.e("set alert dialog");
        DialogManager.setAlert(DialogManager.Alert.LOGIN_SUCCESS);
        // Notify main thread activity
        L.e("notify receiver");
        database.notifyReceiver(DawnDatabase.Status.DATABASE_READY);
        L.e("end of login");
    }

    private void handleActionLogout() throws ParseException {
        L.e("start of logout");
        // If user has not logged in, return
        if (ParseUser.getCurrentUser() == null || !ParseUser.getCurrentUser().isAuthenticated()) {
            return;
        }
        L.e("get list channels");
        List channels = ParseInstallation.getCurrentInstallation().getList("channels");
        if (channels != null && channels.contains("user_" + ParseUser.getCurrentUser().getObjectId())) {
            // Unsubscribe channel
            L.e("unsubscribe channel");
            PushService.unsubscribe(getApplicationContext(), "user_" + ParseUser.getCurrentUser().getObjectId());
        }
        // Log out using Parse API
        L.e("parse logout");
        ParseUser.logOut();
        // Set status as logged out
        L.e("database logout");
        database.logout();
        // Notify main thread activity
        L.e("notify receiver");
        database.notifyReceiver(DawnDatabase.Status.LOGOUT);
        L.e("end of logout");
    }

    private void handleActionRegister(String email, String password, String firstName, String lastName) throws ParseException {
        // Construct a new user
        ParseUser newUser = new ParseUser();
        newUser.setEmail(email);
        newUser.setUsername(email);
        newUser.setPassword(password);
        newUser.put("firstName", firstName);
        newUser.put("lastName", lastName);
        newUser.put("nextSetToBuy", 6);
        // Sign up to Parse
        newUser.signUp();
        // Load the new user from Parse
        database.fetchUser(newUser);
        // Login in background
        login(email, password);
        // Set alert dialog
        DialogManager.setAlert(DialogManager.Alert.REGISTER_SUCCESS);
        // Notify main thread activity
        database.notifyReceiver(DawnDatabase.Status.DATABASE_READY);
    }
}
