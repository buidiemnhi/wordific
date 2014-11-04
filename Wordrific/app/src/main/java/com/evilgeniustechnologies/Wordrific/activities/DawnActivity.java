package com.evilgeniustechnologies.Wordrific.activities;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.evilgeniustechnologies.Wordrific.R;
import com.evilgeniustechnologies.Wordrific.fragments.AboutFragment;
import com.evilgeniustechnologies.Wordrific.fragments.AccountFragment;
import com.evilgeniustechnologies.Wordrific.fragments.BaseFragment;
import com.evilgeniustechnologies.Wordrific.fragments.FriendFragment;
import com.evilgeniustechnologies.Wordrific.fragments.GameplayFragment;
import com.evilgeniustechnologies.Wordrific.models.DawnDatabase;
import com.evilgeniustechnologies.Wordrific.services.DawnIntentService;
import com.evilgeniustechnologies.Wordrific.utilties.DawnUtilities;
import com.evilgeniustechnologies.Wordrific.utilties.DialogManager;
import com.evilgeniustechnologies.Wordrific.utilties.L;
import com.parse.ParseAnalytics;
import com.parse.ParseUser;


public class DawnActivity extends ListServiceActivity {
    public static final int LOGIN = 0;
    public static final int IMAGE = 1;
    private Intent uploadData;
    private Fragment tabFragment;
    private ImageView tabPlay;
    private ImageView tabFriend;
    private ImageView tabAbout;
    private ImageView tabAccount;
    private BaseFragment gameplayFragment;
    private BaseFragment friendFragment;
    private BaseFragment aboutFragment;
    private BaseFragment accountFragment;
    private Tab tab;
    private int totalRequests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Track app opens.
        ParseAnalytics.trackAppOpened(getIntent());
    }

    @Override
    protected void onExecute() {
        setContentView(R.layout.dawn_activity);
        tabFragment = getFragmentManager().findFragmentById(R.id.tab_fragment);
        tabPlay = (ImageView) tabFragment.getView().findViewById(R.id.tab_play);
        tabFriend = (ImageView) tabFragment.getView().findViewById(R.id.tab_friend);
        tabAbout = (ImageView) tabFragment.getView().findViewById(R.id.tab_about);
        tabAccount = (ImageView) tabFragment.getView().findViewById(R.id.tab_account);
        totalRequests = database.getNumOfRequests();
        displayRequestNotification();
        onSelectTabAbout(null);
    }

    @Override
    protected void onRefresh(DawnDatabase.Status status, int progress) {
        L.e("status", status.name());
        if (status == DawnDatabase.Status.FRIENDSHIP_REQUEST_HANDLED ||
                status == DawnDatabase.Status.FRIENDSHIP_REQUEST_SENT) {
            if (tab == Tab.FRIEND) {
                if (findViewById(R.id.fragment_container) != null && friendFragment != null) {
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.detach(friendFragment);
                    transaction.attach(friendFragment);
                    transaction.commit();
                }
            }
        } else if (status == DawnDatabase.Status.AVATAR_UPLOADED) {
            if (tab == Tab.ACCOUNT) {
                if (findViewById(R.id.fragment_container) != null && accountFragment != null) {
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.detach(accountFragment);
                    transaction.attach(accountFragment);
                    transaction.commit();
                }
            }
        } else if (status == DawnDatabase.Status.LOGOUT) {
            onSelectTabAbout(null);
        }
    }

    private void displayRequestNotification() {
        if (tabFragment == null) {
            return;
        }
        L.e("totalRequests", totalRequests);
        RelativeLayout notification = (RelativeLayout) tabFragment.getView().findViewById(R.id.tab_notification_container);
        TextView requestMonitor = (TextView) tabFragment.getView().findViewById(R.id.tab_notification);
        if (totalRequests != 0) {
            notification.setVisibility(View.VISIBLE);
            requestMonitor.setText(totalRequests + "");
        } else {
            notification.setVisibility(View.INVISIBLE);
        }
    }

    private void hideRequestNotification() {
        if (database.getNumOfRequests() != 0) {
            database.clearAllRequests();
            totalRequests = 0;
        }
    }

    private boolean needLogin() {
        if (ParseUser.getCurrentUser() == null || !ParseUser.getCurrentUser().isAuthenticated()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, LOGIN);
            return true;
        }
        return false;
    }

    private void selectTab(BaseFragment fragment) {
        if (findViewById(R.id.fragment_container) != null) {
            if (getCurrentFocus() != null) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    private void displayTab() {
        displayRequestNotification();
        tabPlay.setImageResource(R.drawable.tab_indicator_play);
        tabFriend.setImageResource(R.drawable.tab_indicator_friend);
        tabAbout.setImageResource(R.drawable.tab_indicator_rule);
        tabAccount.setImageResource(R.drawable.tab_indicator_me);
        switch (tab) {
            case PLAY:
                tabPlay.setImageResource(R.drawable.icon_play_selected);
                break;
            case FRIEND:
                tabFriend.setImageResource(R.drawable.icon_friend_selected);
                break;
            case ABOUT:
                tabAbout.setImageResource(R.drawable.icon_rule_selected);
                break;
            case ACCOUNT:
                tabAccount.setImageResource(R.drawable.icon_me_selected);
                break;
        }
    }

    public void onSelectTabGameplay(View v) {
        if (tab != Tab.PLAY) {
            tab = Tab.PLAY;
            if (gameplayFragment == null) {
                gameplayFragment = new GameplayFragment();
            }
            selectTab(gameplayFragment);
            displayTab();
        }
    }

    public void onSelectTabFriend(View v) {
        if (!needLogin()) {
            if (tab != Tab.FRIEND) {
                tab = Tab.FRIEND;
                if (friendFragment == null) {
                    friendFragment = new FriendFragment();
                }
                selectTab(friendFragment);
                hideRequestNotification();
                displayTab();
            }
        }
    }

    public void onSelectTabAbout(View v) {
        if (tab != Tab.ABOUT) {
            tab = Tab.ABOUT;
            if (aboutFragment == null) {
                aboutFragment = new AboutFragment();
            }
            selectTab(aboutFragment);
            displayTab();
        }
    }

    public void onSelectTabAccount(View v) {
        if (!needLogin()) {
            if (tab != Tab.ACCOUNT) {
                tab = Tab.ACCOUNT;
                if (accountFragment == null) {
                    accountFragment = new AccountFragment();
                }
                selectTab(accountFragment);
                displayTab();
            }
        }
    }

    /**
     * Start new activity for searching friend
     *
     * @param v the View that was clicked
     */
    public void onSearch(View v) {
        Intent intent = new Intent(this, FriendActivity.class);
        startActivity(intent);
    }

    /**
     * Start new activity for buying new set
     *
     * @param v the View that was clicked
     */
    public void onBuy(View v) {
        // If no Internet connection, return
        if (!DawnUtilities.isConnected(this)) {
            DialogManager.show(this, DialogManager.Alert.NO_INTERNET);
            return;
        }
        // If no one is currently logged in
        if (database.getCurrentUser() == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return;
        }
        // Prepare in-app billing
        Intent intent = new Intent(this, BuyActivity.class);
        startActivity(intent);
    }

    public void onLogout(View v) {
        L.e("logout", "executed");
        DawnIntentService.startActionLogout(this);
    }

    public void onAvatar(View v) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(intent, IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOGIN) {
            if (resultCode == RESULT_OK) {
                onSelectTabAccount(null);
            }
        } else if (requestCode == IMAGE) {
            if (resultCode == RESULT_OK) {
                uploadData = data;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } else {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        displayRequestNotification();
        if (uploadData != null && uploadData.getData() != null) {
            DawnIntentService.startActionUploadAvatar(this, uploadData.getData());
            uploadData = null;
        }
    }

    public enum Tab {
        PLAY,
        FRIEND,
        ABOUT,
        ACCOUNT
    }
}
