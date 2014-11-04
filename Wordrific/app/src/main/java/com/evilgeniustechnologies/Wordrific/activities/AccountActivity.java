package com.evilgeniustechnologies.Wordrific.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import com.evilgeniustechnologies.Wordrific.adapters.ListAnimationAdapter;
import com.evilgeniustechnologies.Wordrific.adapters.RootAdapter;
import com.evilgeniustechnologies.Wordrific.adapters.ScoreAdapter;
import com.evilgeniustechnologies.Wordrific.daomodel.User;
import com.evilgeniustechnologies.Wordrific.R;

/**
 * Created by vendetta on 6/25/14.
 */
public class AccountActivity extends ListServiceActivity {
    public static final String USER_ID = "userId";
    private User user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.me);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent() != null) {
            user = database.getUserDao().load(getIntent().getLongExtra(USER_ID, 0));
        }
    }

    @Override
    protected void onExecute() {
        ImageView logoutBtn = (ImageView) findViewById(R.id.logout_button);
        EditText firstNameEdt = (EditText) findViewById(R.id.me_firstName);
        EditText lastNameEdt = (EditText) findViewById(R.id.me_lastName);
        ImageView avatarImg = (ImageView) findViewById(R.id.me_avatar);
        logoutBtn.setVisibility(View.GONE);
        firstNameEdt.setText(user.getFirstName());
        lastNameEdt.setText(user.getLastName());
        firstNameEdt.setTextColor(Color.BLACK);
        lastNameEdt.setTextColor(Color.BLACK);
        firstNameEdt.setEnabled(false);
        lastNameEdt.setEnabled(false);
        firstNameEdt.setFocusable(false);
        lastNameEdt.setFocusable(false);
        if (!TextUtils.isEmpty(user.getAvatarUrl())) {
            database.getLoader().displayImage(user.getAvatarUrl(), avatarImg);
        }

        ListView rootList = (ListView) findViewById(R.id.me_list_scores);
        RootAdapter rootAdapter = new ScoreAdapter(this, database, user);
        ListAnimationAdapter animationAdapter = new ListAnimationAdapter(rootAdapter);
        animationAdapter.setAbsListView(rootList);
        rootList.setAdapter(animationAdapter);
        rootList.setOnItemClickListener(rootAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public void onAvatar(View v) {
        // Do nothing
    }
}