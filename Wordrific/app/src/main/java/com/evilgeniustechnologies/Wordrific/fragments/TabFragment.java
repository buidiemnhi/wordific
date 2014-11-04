package com.evilgeniustechnologies.Wordrific.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.evilgeniustechnologies.Wordrific.R;

/**
 * Created by vendetta on 6/20/14.
 */
public class TabFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tab_layout, container, false);
    }
}