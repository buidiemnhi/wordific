package com.evilgeniustechnologies.Wordrific.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import com.evilgeniustechnologies.Wordrific.models.DawnDatabase;
import com.evilgeniustechnologies.Wordrific.R;

import java.util.List;

/**
 * Created by benjamin on 5/23/14.
 */
public class SellAdapter extends RootAdapter {
    private List<String> titleList;
    private List<String> priceList;

    public SellAdapter(Context context, DawnDatabase database, List<String> titleList, List<String> priceList) {
        super(context, R.layout.sell_row_item, database, titleList);
        this.titleList = titleList;
        this.priceList = priceList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.sell_row_item, null);
            ViewHolder holder = new ViewHolder();
            holder.title = (TextView) rowView.findViewById(R.id.sell_title);
            holder.button = (Button) rowView.findViewById(R.id.sell_button);
            holder.listener = new SellListener();
            holder.button.setOnClickListener(holder.listener);
            rowView.setTag(holder);
        }
        ViewHolder holder = (ViewHolder) rowView.getTag();
        holder.title.setText(titleList.get(position));
        holder.button.setText(priceList.get(position));
        holder.listener.setPosition(position);
        return rowView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Do nothing
    }

    @Override
    protected List<String> getAllTitles() {
        return null;
    }

    public void onBuy(int position) {

    }

    private static class ViewHolder {
        public TextView title;
        public Button button;
        public SellListener listener;
    }

    private class SellListener implements View.OnClickListener {
        private int position;

        public void setPosition(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            onBuy(position);
        }
    }
}
