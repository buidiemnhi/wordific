package com.evilgeniustechnologies.Wordrific.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import com.evilgeniustechnologies.Wordrific.daomodel.Score;
import com.evilgeniustechnologies.Wordrific.daomodel.User;
import com.evilgeniustechnologies.Wordrific.models.DawnDatabase;
import com.evilgeniustechnologies.Wordrific.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vendetta on 6/23/14.
 */
public class ScoreAdapter extends RootAdapter {
    private List<Score> contents;

    public ScoreAdapter(Context context, DawnDatabase database, User user) {
        super(context, R.layout.me_score, database, user.getScores());
        contents = new ArrayList<Score>(user.getScores());
    }

    @Override
    protected List<String> getAllTitles() {
        List<String> titles = new ArrayList<String>();
        for (Score score : contents) {
            titles.add(score.getSet() + "");
        }
        return titles;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Score score = contents.get(position);
        View rowView = convertView;
        if (rowView == null) {
            rowView = View.inflate(getContext(), R.layout.me_score, null);
            ViewHolder holder = new ViewHolder();
            holder.setTxt = (TextView) rowView.findViewById(R.id.me_set);
            holder.scoreTxt = (TextView) rowView.findViewById(R.id.me_score);
            holder.scoreAveTxt = (TextView) rowView.findViewById(R.id.me_ave_score);
            holder.timeTxt = (TextView) rowView.findViewById(R.id.me_time);
            holder.timeAveTxt = (TextView) rowView.findViewById(R.id.me_ave_time);
            rowView.setTag(holder);
        }
        ViewHolder holder = (ViewHolder) rowView.getTag();
        holder.setTxt.setText(score.getSet() + "");
        holder.scoreTxt.setText(score.getScore() + "");
        holder.scoreAveTxt.setText((Math.round(score.getScore() / 20.0 * 10.0) / 10.0) + "");
        holder.timeTxt.setText(score.getElapsedTime() + "");
        holder.timeAveTxt.setText((Math.round(score.getElapsedTime() / 20.0 * 10.0) / 10.0) + "");
        return rowView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Do nothing
    }

    private static class ViewHolder {
        public TextView setTxt;
        public TextView scoreTxt;
        public TextView scoreAveTxt;
        public TextView timeTxt;
        public TextView timeAveTxt;
    }
}
