package com.evilgeniustechnologies.Wordrific.activities;

import android.os.Bundle;
import android.widget.ListView;
import com.evilgeniustechnologies.Wordrific.adapters.RootAdapter;

public abstract class ListServiceActivity extends ServiceActivity {
    protected ListView rootList;
    protected RootAdapter rootAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

}
