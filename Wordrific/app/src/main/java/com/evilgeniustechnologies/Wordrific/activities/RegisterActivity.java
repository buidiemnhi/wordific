package com.evilgeniustechnologies.Wordrific.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;

import com.evilgeniustechnologies.Wordrific.R;
import com.evilgeniustechnologies.Wordrific.services.DawnIntentService;
import com.evilgeniustechnologies.Wordrific.utilties.DialogManager;

/**
 * Created by vendetta on 6/16/14.
 */
public class RegisterActivity extends ServiceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
    }

    public void onSignUp(View v) {
        EditText firstNameEdt = (EditText) findViewById(R.id.register_first_name);
        EditText lastNameEdt = (EditText) findViewById(R.id.register_last_name);
        EditText emailEdt = (EditText) findViewById(R.id.register_email);
        EditText passwordEdt = (EditText) findViewById(R.id.register_password);
        EditText retryPasswordEdt = (EditText) findViewById(R.id.register_password_retry);
        String firstName = firstNameEdt.getText().toString();
        String lastName = lastNameEdt.getText().toString();
        String email = emailEdt.getText().toString();
        String password = passwordEdt.getText().toString();
        String retryPassword = retryPasswordEdt.getText().toString();
        if (TextUtils.isEmpty(firstName)) {
            DialogManager.show(this, DialogManager.Alert.NO_FIRST_NAME);
        } else if (TextUtils.isEmpty(lastName)) {
            DialogManager.show(this, DialogManager.Alert.NO_LAST_NAME);
        } else if (TextUtils.isEmpty(email)) {
            DialogManager.show(this, DialogManager.Alert.NO_EMAIL);
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            DialogManager.show(this, DialogManager.Alert.INVALID_EMAIL_ADDRESS);
        } else if (TextUtils.isEmpty(password)) {
            DialogManager.show(this, DialogManager.Alert.NO_PASSWORD);
        } else if (TextUtils.isEmpty(retryPassword)) {
            DialogManager.show(this, DialogManager.Alert.NO_RETRY_PASSWORD);
        } else if (!password.equals(retryPassword)) {
            DialogManager.show(this, DialogManager.Alert.PASSWORD_MISMATCH);
        } else {
            DawnIntentService.startActionRegister(this, email, password, firstName, lastName);
        }
    }
}