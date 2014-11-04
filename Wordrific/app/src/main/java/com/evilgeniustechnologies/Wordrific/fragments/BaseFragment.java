package com.evilgeniustechnologies.Wordrific.fragments;

import android.app.Fragment;

import com.evilgeniustechnologies.Wordrific.models.DawnDatabase;

/**
 * Created by vendetta on 6/20/14.
 */
public abstract class BaseFragment extends Fragment {
    protected DawnDatabase database;

    public BaseFragment() {
        database = DawnDatabase.getInstance();
    }
}