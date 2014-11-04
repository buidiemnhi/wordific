package com.evilgeniustechnologies.Wordrific.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.evilgeniustechnologies.Wordrific.R;
import com.evilgeniustechnologies.Wordrific.daomodel.Configuration;

/**
 * Created by vendetta on 6/20/14.
 */
public class AboutFragment extends BaseFragment {

    public AboutFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.rule, container, false);
        WebView ruleContent = (WebView) layout.findViewById(R.id.rules_content);
        Configuration configuration = database.getConfigurationDao().loadAll().get(0);
        ruleContent.loadData(configuration.getGameRules(), "text/html", null);
        return layout;
    }
}