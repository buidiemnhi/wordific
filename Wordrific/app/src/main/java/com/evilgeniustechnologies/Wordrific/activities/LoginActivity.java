package com.evilgeniustechnologies.Wordrific.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.evilgeniustechnologies.Wordrific.R;
import com.evilgeniustechnologies.Wordrific.services.DawnIntentService;
import com.evilgeniustechnologies.Wordrific.utilties.DialogManager;

/**
 * Created by vendetta on 6/16/14.
 */
public class LoginActivity extends ServiceActivity {
    private static int REGISTER = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void onLogin(View v) {
        EditText emailEdt = (EditText) findViewById(R.id.login_email);
        EditText passwordEdt = (EditText) findViewById(R.id.login_password);
        String email = emailEdt.getText().toString();
        String password = passwordEdt.getText().toString();
        if (TextUtils.isEmpty(email)) {
            DialogManager.show(this, DialogManager.Alert.NO_EMAIL);
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            DialogManager.show(this, DialogManager.Alert.INVALID_EMAIL_ADDRESS);
        } else if (TextUtils.isEmpty(password)) {
            DialogManager.show(this, DialogManager.Alert.NO_PASSWORD);
        } else {
            DialogManager.show(this, DialogManager.Alert.PROGRESS);
            DawnIntentService.startActionLogin(this, email, password);
        }
    }

    public void onRegister(View v) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivityForResult(intent, REGISTER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REGISTER) {
            if (resultCode == RESULT_OK) {
                if (database.isLoggedIn()) {
                    setResult(RESULT_OK);
                    finish();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }
}