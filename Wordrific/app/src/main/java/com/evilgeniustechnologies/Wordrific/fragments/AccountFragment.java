package com.evilgeniustechnologies.Wordrific.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;

import com.evilgeniustechnologies.Wordrific.R;
import com.evilgeniustechnologies.Wordrific.adapters.ListAnimationAdapter;
import com.evilgeniustechnologies.Wordrific.adapters.RootAdapter;
import com.evilgeniustechnologies.Wordrific.adapters.ScoreAdapter;
import com.evilgeniustechnologies.Wordrific.daomodel.User;
import com.evilgeniustechnologies.Wordrific.services.DawnIntentService;
import com.makeramen.RoundedImageView;

/**
 * Created by vendetta on 6/20/14.
 */
public class AccountFragment extends BaseFragment {
    private EditListener listener;
    private EditText firstNameEdt;
    private EditText lastNameEdt;

    public AccountFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.me, container, false);
        firstNameEdt = (EditText) layout.findViewById(R.id.me_firstName);
        lastNameEdt = (EditText) layout.findViewById(R.id.me_lastName);
        RoundedImageView avatarImg = (RoundedImageView) layout.findViewById(R.id.me_avatar);
        User user = database.getCurrentUser();
        firstNameEdt.setText(user.getFirstName());
        lastNameEdt.setText(user.getLastName());
        if (!TextUtils.isEmpty(user.getAvatarUrl())) {
            database.getLoader().displayImage(user.getAvatarUrl(), avatarImg);
        }
        listener = new EditListener();
        firstNameEdt.setOnFocusChangeListener(listener);
        lastNameEdt.setOnFocusChangeListener(listener);

        ListView rootList = (ListView) layout.findViewById(R.id.me_list_scores);
        RootAdapter rootAdapter = new ScoreAdapter(getActivity(), database, database.getCurrentUser());
        ListAnimationAdapter animationAdapter = new ListAnimationAdapter(rootAdapter);
        animationAdapter.setAbsListView(rootList);
        rootList.setAdapter(animationAdapter);
        rootList.setOnItemClickListener(rootAdapter);
        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if (listener != null) {
            User user = database.getCurrentUser();
            firstNameEdt.setText(user.getFirstName());
            lastNameEdt.setText(user.getLastName());
            firstNameEdt.setOnFocusChangeListener(listener);
            lastNameEdt.setOnFocusChangeListener(listener);
        }
    }

    @Override
    public void onPause() {
        firstNameEdt.setOnFocusChangeListener(null);
        lastNameEdt.setOnFocusChangeListener(null);
        super.onPause();
    }

    private class EditListener implements View.OnFocusChangeListener {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            switch (v.getId()) {
                case R.id.me_firstName:
                    if (!hasFocus) {
                        String firstName = ((EditText) v).getText().toString();
                        DawnIntentService.startActionChangeFirstName(getActivity(), firstName);
                    }
                    break;
                case R.id.me_lastName:
                    if (!hasFocus) {
                        String lastName = ((EditText) v).getText().toString();
                        DawnIntentService.startActionChangeLastName(getActivity(), lastName);
                    }
                    break;
            }
        }
    }
}