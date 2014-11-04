package com.evilgeniustechnologies.Wordrific.adapters;

import android.content.Context;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import com.evilgeniustechnologies.Wordrific.models.DawnDatabase;

import java.util.List;

/**
 * Created by benjamin on 5/23/14.
 */
public abstract class RootAdapter extends ArrayAdapter
        implements AdapterView.OnItemClickListener {
    protected ListAnimationAdapter animationAdapter;
    protected DawnDatabase database;

    public RootAdapter(Context context, int resource, DawnDatabase database, List contents) {
        super(context, resource, contents);
        this.database = database;
    }

    public void setAnimationAdapter(ListAnimationAdapter animationAdapter) {
        this.animationAdapter = animationAdapter;
    }

    protected void populateData() {
        clear();
        addAll(getAllTitles());
    }

    public void refresh() {
        // Refresh
    }

    protected abstract List<String> getAllTitles();
}
