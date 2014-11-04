package com.evilgeniustechnologies.Wordrific.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.evilgeniustechnologies.Wordrific.R;
import com.evilgeniustechnologies.Wordrific.adapters.ConnectAdapter;

/**
 * Created by vendetta on 6/20/14.
 */
public class FriendFragment extends BaseFragment {
    private ConnectAdapter adapter;

    public FriendFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.connect, container, false);

        ExpandableListView listView = (ExpandableListView) layout.findViewById(R.id.connect_expand_list);
        adapter = new ConnectAdapter(getActivity(), database);
        listView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return true;
            }
        });
        listView.setOnChildClickListener(adapter);
        listView.setAdapter(adapter);

        listView.expandGroup(0);
        listView.expandGroup(1);
        return layout;
    }
}