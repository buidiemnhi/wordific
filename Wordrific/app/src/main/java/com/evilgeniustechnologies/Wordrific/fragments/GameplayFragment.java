package com.evilgeniustechnologies.Wordrific.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.evilgeniustechnologies.Wordrific.R;
import com.evilgeniustechnologies.Wordrific.adapters.ListAnimationAdapter;
import com.evilgeniustechnologies.Wordrific.adapters.NormalAdapter;
import com.evilgeniustechnologies.Wordrific.adapters.RootAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vendetta on 6/20/14.
 */
public class GameplayFragment extends BaseFragment {
    private RootAdapter rootAdapter;

    public GameplayFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.tab_gameplay, container, false);
        List<Integer> contents = new ArrayList<Integer>();
        int totalSets = 5;
        if (database.getCurrentUser() != null) {
            totalSets = database.getCurrentUser().getNextSetToBuy() - 1;
            if (totalSets > database.getHighestSet()) {
                totalSets = database.getHighestSet();
            }
        }
        for (int i = 0; i < totalSets; i++) {
            contents.add(i + 1);
        }
        ListView rootList = (ListView) layout.findViewById(R.id.service_list);
        rootAdapter = new NormalAdapter(getActivity(), database, contents);
        ListAnimationAdapter animationAdapter = new ListAnimationAdapter(rootAdapter);
        animationAdapter.setAbsListView(rootList);
        rootList.setAdapter(animationAdapter);
        rootList.setOnItemClickListener(rootAdapter);
        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        rootAdapter.notifyDataSetChanged();
    }
}