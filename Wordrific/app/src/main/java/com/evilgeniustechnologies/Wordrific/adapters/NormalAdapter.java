package com.evilgeniustechnologies.Wordrific.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.evilgeniustechnologies.Wordrific.R;
import com.evilgeniustechnologies.Wordrific.activities.PlayActivity;
import com.evilgeniustechnologies.Wordrific.daomodel.PuzzleDao;
import com.evilgeniustechnologies.Wordrific.models.DawnDatabase;
import com.evilgeniustechnologies.Wordrific.utilties.DawnUtilities;
import com.evilgeniustechnologies.Wordrific.utilties.DialogManager;

import java.util.List;

/**
 * Created by benjamin on 5/23/14.
 */
public class NormalAdapter extends RootAdapter {
    List contents;

    public NormalAdapter(Context context, DawnDatabase database, List contents) {
        super(context, R.layout.set_row_item, database, contents);
        this.contents = contents;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.set_row_item, null);
            ViewHolder holder = new ViewHolder();
            holder.title = (TextView) rowView.findViewById(R.id.title_item);
            holder.background = (ImageView) rowView.findViewById(R.id.set_bg);
            rowView.setTag(holder);
        }
        ViewHolder holder = (ViewHolder) rowView.getTag();
        holder.title.setText("SET " + contents.get(position));
        if (database.getPuzzleDao()
                .queryBuilder()
                .where(PuzzleDao.Properties.Set.eq(contents.get(position)))
                .count() == 0) {
            holder.background.setImageResource(R.drawable.bg_set_new);
        } else {
            holder.background.setImageResource(R.drawable.bg_set_done);
        }
        return rowView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Integer set = (Integer) contents.get(position);
        if (!DawnUtilities.isConnected(getContext()) &&
                database.getPuzzleDao()
                        .queryBuilder()
                        .where(PuzzleDao.Properties.Set.eq(contents.get(position)))
                        .count() == 0){
            DialogManager.show(getContext(), DialogManager.Alert.NO_INTERNET);
            return;
        }
        Intent intent = new Intent(getContext(), PlayActivity.class);
        intent.putExtra(PlayActivity.SET, set.intValue());
        getContext().startActivity(intent);
    }

    @Override
    protected List<String> getAllTitles() {
        return null;
    }

    private static class ViewHolder {
        public TextView title;
        public ImageView background;
    }
}
