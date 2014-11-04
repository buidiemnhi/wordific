package com.evilgeniustechnologies.Wordrific.activities;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.evilgeniustechnologies.Wordrific.models.DawnDatabase;
import com.evilgeniustechnologies.Wordrific.utilties.DialogManager;
import com.evilgeniustechnologies.Wordrific.utilties.L;

public abstract class ServiceActivity extends Activity implements DawnDatabase.DawnReceiver {
    protected DawnDatabase database;
    protected boolean displayed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set to portrait mode only for all activities
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // If database is null, either construct a new instance or get an already constructed instance
        if (database == null) {
            L.e(getLocalClassName(), "Database is null");
            database = DawnDatabase.getInstance(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        L.e(getLocalClassName(), "onResume");

        // Set observer for database
        database.setReceiver(this);

        // Display any unresolved dialog
        DialogManager.show(this);
    }

    @Override
    protected void onPause() {
        L.e(getLocalClassName(), "onPause");

        // Close all visible dialogs before rotate (if any)
        DialogManager.close();

        // Unset observer for database, to avoid memory leak
        database.setReceiver(null);
        super.onPause();
    }

    @Override
    public void onNotified(final DawnDatabase.Status status, final int progress) {
        // This method is originally fired in background thread, needs to run it on ui thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (status) {
                    case NONE:
                        L.e("notify", status.name());
                        // Continue display progress while database is fetching data
                        DialogManager.show(ServiceActivity.this, DialogManager.Alert.PROGRESS_BAR);
                        DialogManager.updateProgress(database.getProgressStatus());
                        break;
                    case DATABASE_READY:
                        L.e("notify", status.name());
                        if (database.getProgressStatus() != 100) {
                            return;
                        }

                        // Show any currently issued dialog
                        DialogManager.show(ServiceActivity.this);

                        // Execute any pending request
                        database.executeRequests();

                        // If @onExecute already run, don't run it again
                        if (!displayed) {
                            onExecute();
                            displayed = true;
                        }
                        break;
                    case SCORE_PUBLISHED:
                    case PREPARING_PUZZLES:
                    case PUZZLES_READY:
                    case FRIENDSHIP_REQUEST_HANDLED:
                    case FRIENDSHIP_REQUEST_SENT:
                    case AVATAR_UPLOADED:
                    case LOGOUT:
                        // Refresh UI components
                        onRefresh(status, progress);
                        break;
                }
            }
        });
    }

    /**
     * Called when the database done fetching data, or a new observer is set.
     * Put all UI construction code here.
     */
    protected void onExecute() {
        // Construct UI here
        L.e("User records", database.getUserDao().count());
        L.e("Friendship records", database.getFriendshipDao().count());
        L.e("Configuration records", database.getConfigurationDao().count());
        L.e("Puzzle records", database.getPuzzleDao().count());
        L.e("Score records", database.getScoreDao().count());
    }

    protected void onRefresh(DawnDatabase.Status status, int progress) {
        // Refresh UI components
    }
}
