package com.evilgeniustechnologies.Wordrific.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.evilgeniustechnologies.Wordrific.R;
import com.evilgeniustechnologies.Wordrific.activities.AccountActivity;
import com.evilgeniustechnologies.Wordrific.daomodel.User;
import com.evilgeniustechnologies.Wordrific.models.DawnDatabase;
import com.evilgeniustechnologies.Wordrific.services.DawnIntentService;
import com.evilgeniustechnologies.Wordrific.utilties.L;
import com.makeramen.RoundedImageView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by vendetta on 5/26/14.
 */
public class ConnectAdapter extends BaseExpandableListAdapter
        implements ExpandableListView.OnChildClickListener {
    private Map<String, List<User>> data;
    private Context context;
    private DawnDatabase database;

    public ConnectAdapter(Context context, DawnDatabase database) {
        this.context = context;
        this.database = database;
        update();
    }

    public void update() {
        data = new LinkedHashMap<String, List<User>>();
        data.put("", database.getFriends());
        data.put(context.getString(R.string.connect_request), database.getWaiting());
        notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return data.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return data.get(new ArrayList<String>(data.keySet()).get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return new ArrayList<String>(data.keySet()).get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return data.get(new ArrayList<String>(data.keySet()).get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View parentView = convertView;
        if (parentView == null) {
            parentView = LayoutInflater.from(context).inflate(R.layout.parent_row_item, null);
            ParentHolder holder = new ParentHolder();
            holder.title = (TextView) parentView.findViewById(R.id.parent_title);
            parentView.setTag(holder);
        }
        ParentHolder holder = (ParentHolder) parentView.getTag();
        String title = ((String) getGroup(groupPosition));
        L.e("parent " + groupPosition, getChildrenCount(groupPosition));
        if (TextUtils.isEmpty(title) || getChildrenCount(groupPosition) == 0) {
            holder.title.setVisibility(View.GONE);
        } else {
            holder.title.setVisibility(View.VISIBLE);
            holder.title.setText(title.toUpperCase());
        }
        return parentView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        View childView = convertView;
        if (childView == null) {
            childView = LayoutInflater.from(context).inflate(R.layout.friend_row_item, null);
            ChildHolder holder = new ChildHolder();
            holder.icon = (RoundedImageView) childView.findViewById(R.id.friend_row_icon);
            holder.title = (TextView) childView.findViewById(R.id.friend_row_title);
            holder.accept = (ImageView) childView.findViewById(R.id.friend_row_accept);
            holder.reject = (ImageView) childView.findViewById(R.id.friend_row_deny);
            holder.delete = (ImageView) childView.findViewById(R.id.friend_row_delete);
            TextView statusTxt = (TextView) childView.findViewById(R.id.friend_row_status);
            statusTxt.setVisibility(View.GONE);
            holder.aListener = new AcceptListener();
            holder.accept.setOnClickListener(holder.aListener);
            holder.dListener = new DenyListener();
            holder.reject.setOnClickListener(holder.dListener);
            holder.deListener = new DeleteListener();
            holder.delete.setOnClickListener(holder.deListener);
            childView.setTag(holder);
        }
        ChildHolder holder = (ChildHolder) childView.getTag();
        User user = (User) getChild(groupPosition, childPosition);
        holder.title.setText(user.getFirstName() + " " + user.getLastName());
        database.getLoader().displayImage(user.getAvatarUrl(), holder.icon);
        if (groupPosition == 0) {
            holder.accept.setVisibility(View.GONE);
            holder.reject.setVisibility(View.GONE);
            holder.delete.setVisibility(View.VISIBLE);
            holder.deListener.setUser(user);
        } else {
            holder.accept.setVisibility(View.VISIBLE);
            holder.reject.setVisibility(View.VISIBLE);
            holder.delete.setVisibility(View.GONE);
            holder.aListener.setUser(user);
            holder.dListener.setUser(user);
        }
        return childView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        User user = (User) getChild(groupPosition, childPosition);
        L.e("user", user.getFirstName() + " " + user.getLastName());
        Intent intent = new Intent(context, AccountActivity.class);
        intent.putExtra(AccountActivity.USER_ID, user.getId());
        context.startActivity(intent);
        return true;
    }

    private static class ParentHolder {
        public TextView title;
    }

    private static class ChildHolder {
        public RoundedImageView icon;
        public TextView title;
        public ImageView accept;
        public ImageView reject;
        public ImageView delete;
        public AcceptListener aListener;
        public DenyListener dListener;
        public DeleteListener deListener;
    }

    private class AcceptListener implements View.OnClickListener {
        private User user;

        public void setUser(User user) {
            this.user = user;
        }

        @Override
        public void onClick(View v) {
            DawnIntentService.startActionAcceptRequest(context, user.getObjectId());
        }
    }

    private class DenyListener implements View.OnClickListener {
        private User user;

        public void setUser(User user) {
            this.user = user;
        }

        @Override
        public void onClick(View v) {
            DawnIntentService.startActionRejectRequest(context, user.getObjectId());
        }
    }

    private class DeleteListener implements View.OnClickListener {
        private User user;

        public void setUser(User user) {
            this.user = user;
        }

        @Override
        public void onClick(View v) {
            DawnIntentService.startActionDeleteRequest(context, user.getObjectId());
        }
    }
}
