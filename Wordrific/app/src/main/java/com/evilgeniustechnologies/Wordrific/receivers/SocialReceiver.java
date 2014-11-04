package com.evilgeniustechnologies.Wordrific.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.evilgeniustechnologies.Wordrific.models.DawnDatabase;
import com.evilgeniustechnologies.Wordrific.utilties.L;

import org.json.JSONException;
import org.json.JSONObject;

public class SocialReceiver extends BroadcastReceiver {

    public SocialReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
            DawnDatabase.getInstance().collectRequests(json.getString("task"), json.getString("friendshipObjId"), json.getString("alert"));
        } catch (JSONException e) {
            L.e(this.getClass().getName(), e.getMessage(), e);
        }
    }
}
