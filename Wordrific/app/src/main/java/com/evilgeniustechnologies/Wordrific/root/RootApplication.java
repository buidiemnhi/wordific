package com.evilgeniustechnologies.Wordrific.root;

import android.app.Application;

import com.evilgeniustechnologies.Wordrific.R;
import com.evilgeniustechnologies.Wordrific.activities.DawnActivity;
import com.evilgeniustechnologies.Wordrific.utilties.L;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.parse.Parse;
import com.parse.PushService;

/**
 * Created by vendetta on 6/23/14.
 */
public class RootApplication extends Application {

    public RootApplication() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable debugging
//        L.enableDebugging();

        // Init Parse
        Parse.initialize(this, this.getString(R.string.parse_appId), this.getString(R.string.parse_client));

        // Specify an Activity to handle all pushes by default.
        PushService.setDefaultPushCallback(this, DawnActivity.class);

        // Init Image Loader
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.avatar)
                .showImageForEmptyUri(R.drawable.avatar)
                .showImageOnFail(R.drawable.avatar)
                .displayer(new FadeInBitmapDisplayer(1000))
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .defaultDisplayImageOptions(defaultOptions)
                .threadPoolSize(5)
                .build();
        ImageLoader.getInstance().init(config);
    }
}
