package com.evilgeniustechnologies.Wordrific.activities;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ListView;
import android.widget.SearchView;
import com.evilgeniustechnologies.Wordrific.adapters.FriendAdapter;
import com.evilgeniustechnologies.Wordrific.adapters.ListAnimationAdapter;
import com.evilgeniustechnologies.Wordrific.R;
import com.evilgeniustechnologies.Wordrific.models.DawnDatabase;

public class FriendActivity extends ListServiceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onExecute() {
        setContentView(R.layout.friend);

        rootList = (ListView) findViewById(R.id.friend_list_view);
        rootAdapter = new FriendAdapter(this, database);

        ListAnimationAdapter animationAdapter = new ListAnimationAdapter(rootAdapter);
        rootAdapter.setAnimationAdapter(animationAdapter);
        animationAdapter.setAbsListView(rootList);
        rootList.setAdapter(animationAdapter);
        rootList.setOnItemClickListener(rootAdapter);
    }

    @Override
    protected void onRefresh(DawnDatabase.Status status, int progress) {
        if (status == DawnDatabase.Status.FRIENDSHIP_REQUEST_HANDLED ||
                status == DawnDatabase.Status.FRIENDSHIP_REQUEST_SENT) {
            if (rootAdapter != null) {
                rootAdapter.refresh();
                rootAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                // this is your adapter that will be filtered
                if (rootAdapter != null) {
                    rootAdapter.getFilter().filter(newText);
                }
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                // this is your adapter that will be filtered
                if (rootAdapter != null) {
                    rootAdapter.getFilter().filter(query);
                }
                return true;
            }
        };
        searchView.setOnQueryTextListener(queryTextListener);
        return super.onCreateOptionsMenu(menu);
    }
}
