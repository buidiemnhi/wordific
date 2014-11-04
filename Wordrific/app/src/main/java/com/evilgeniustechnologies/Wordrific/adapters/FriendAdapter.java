package com.evilgeniustechnologies.Wordrific.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.evilgeniustechnologies.Wordrific.R;
import com.evilgeniustechnologies.Wordrific.activities.AccountActivity;
import com.evilgeniustechnologies.Wordrific.daomodel.User;
import com.evilgeniustechnologies.Wordrific.models.DawnDatabase;
import com.evilgeniustechnologies.Wordrific.services.DawnIntentService;
import com.evilgeniustechnologies.Wordrific.utilties.L;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vendetta on 5/27/14.
 */
public class FriendAdapter extends RootAdapter {
    private List<User> contents;
    private List<User> current;
    private List<User> friends;
    private List<User> pending;
    private List<User> waiting;
    private UserFilter userFilter;

    public FriendAdapter(Context context, DawnDatabase database) {
        super(context, R.layout.friend_row_item, database, database.getUserDao().loadAll());
        update();
    }

    @Override
    public void refresh() {
        update();
    }

    private void update() {
        this.contents = new ArrayList<User>(database.getUserDao().loadAll());
        this.current = new ArrayList<User>(contents);
        current.remove(database.getCurrentUser());
        friends = database.getFriends();
        pending = database.getPending();
        waiting = database.getWaiting();
        populateData();
    }

    @Override
    protected List<String> getAllTitles() {
        List<String> titles = new ArrayList<String>();
        for (User user : current) {
            titles.add(user.getFirstName() + " " + user.getLastName());
        }
        return titles;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if (rowView == null) {
            rowView = LayoutInflater.from(getContext()).inflate(R.layout.friend_row_item, null);
            ViewHolder holder = new ViewHolder();
            holder.icon = (ImageView) rowView.findViewById(R.id.friend_row_icon);
            holder.title = (TextView) rowView.findViewById(R.id.friend_row_title);
            holder.status = (TextView) rowView.findViewById(R.id.friend_row_status);
            holder.connect = (ImageView) rowView.findViewById(R.id.friend_row_connect);
            holder.accept = (ImageView) rowView.findViewById(R.id.friend_row_accept);
            holder.deny = (ImageView) rowView.findViewById(R.id.friend_row_deny);
            holder.fListener = new FriendListener();
            holder.connect.setOnClickListener(holder.fListener);
            holder.aListener = new AcceptListener();
            holder.accept.setOnClickListener(holder.aListener);
            holder.dListener = new DenyListener();
            holder.deny.setOnClickListener(holder.dListener);
            rowView.setTag(holder);
        }
        ViewHolder holder = (ViewHolder) rowView.getTag();
        User user = current.get(position);
        holder.title.setText(user.getFirstName() + " " + user.getLastName());
        database.getLoader().displayImage(user.getAvatarUrl(), holder.icon);
        if (friends.contains(user)) {
            holder.status.setText(getContext().getString(R.string.connect_friend));
            holder.status.setVisibility(View.VISIBLE);
            holder.connect.setVisibility(View.GONE);
            holder.accept.setVisibility(View.GONE);
            holder.deny.setVisibility(View.GONE);
        } else if (pending.contains(user)) {
            holder.status.setText(getContext().getString(R.string.request_pending));
            holder.status.setVisibility(View.VISIBLE);
            holder.connect.setVisibility(View.GONE);
            holder.accept.setVisibility(View.GONE);
            holder.deny.setVisibility(View.GONE);
        } else if (waiting.contains(user)) {
            holder.status.setVisibility(View.GONE);
            holder.connect.setVisibility(View.GONE);
            holder.accept.setVisibility(View.VISIBLE);
            holder.deny.setVisibility(View.VISIBLE);
            holder.aListener.setUser(current.get(position));
            holder.dListener.setUser(current.get(position));
        } else {
            holder.status.setVisibility(View.GONE);
            holder.connect.setVisibility(View.VISIBLE);
            holder.accept.setVisibility(View.GONE);
            holder.deny.setVisibility(View.GONE);
            holder.fListener.setUser(current.get(position));
        }
        return rowView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        User user = current.get(position);
        Intent intent = new Intent(getContext(), AccountActivity.class);
        intent.putExtra(AccountActivity.USER_ID, user.getId());
        getContext().startActivity(intent);
    }

    @Override
    public Filter getFilter() {
        if (userFilter == null) {
            userFilter = new UserFilter();
        }
        return userFilter;
    }

    private static class ViewHolder {
        public ImageView icon;
        public TextView title;
        public TextView status;
        public ImageView connect;
        public ImageView accept;
        public ImageView deny;
        public FriendListener fListener;
        public AcceptListener aListener;
        public DenyListener dListener;
    }

    private class FriendListener implements View.OnClickListener {
        private User user;

        public void setUser(User user) {
            this.user = user;
        }

        @Override
        public void onClick(View v) {
            DawnIntentService.startActionSendRequest(getContext(), user.getObjectId());
        }
    }

    private class AcceptListener implements View.OnClickListener {
        private User user;

        public void setUser(User user) {
            this.user = user;
        }

        @Override
        public void onClick(View v) {
            DawnIntentService.startActionAcceptRequest(getContext(), user.getObjectId());
        }
    }

    private class DenyListener implements View.OnClickListener {
        private User user;

        public void setUser(User user) {
            this.user = user;
        }

        @Override
        public void onClick(View v) {
            DawnIntentService.startActionRejectRequest(getContext(), user.getObjectId());
        }
    }

    private class UserFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            // We implement here the filter logic
            if (constraint == null || constraint.length() == 0) {
                // No filter implemented we return all the list
                results.values = contents;
                results.count = contents.size();
            } else {
                // We perform filtering operation
                List<User> filtered = new ArrayList<User>();

                for (Object content : contents) {
                    User user = (User) content;
                    if ((user.getFirstName() + " " + user.getLastName()).toLowerCase().startsWith(constraint.toString().toLowerCase())) {
                        filtered.add(user);
                    } else {
                        final String[] words = (user.getFirstName() + " " + user.getLastName()).toLowerCase().split(" ");
                        for (String word : words) {
                            if (word.startsWith(constraint.toString().toLowerCase())) {
                                filtered.add(user);
                                break;
                            }
                        }
                    }
                }
                results.values = filtered;
                results.count = filtered.size();
                L.e("search count", String.valueOf(results.count));
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            // Now we have to inform the adapter about the new list filtered
            animationAdapter.reset();
            current = (List<User>) results.values;
            populateData();
            L.e("count", String.valueOf(results.count));
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}
