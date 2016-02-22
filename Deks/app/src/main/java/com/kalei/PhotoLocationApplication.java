package com.kalei;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.flurry.android.FlurryAgent;

import android.app.Application;

import io.fabric.sdk.android.Fabric;

/**
 * Created by risaki on 2/20/16.
 */
public class PhotoLocationApplication extends Application {

    public static String FLURRY_KEY = "JGBTZJXTZFXBS5VY6T56";

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(true).build())
                .build();
        // configure Flurry
        FlurryAgent.setLogEnabled(false);

        // init Flurry
        FlurryAgent.init(this, FLURRY_KEY);
    }
}
