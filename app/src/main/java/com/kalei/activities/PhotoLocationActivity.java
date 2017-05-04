package com.kalei.activities;

import com.crashlytics.android.Crashlytics;
import com.kalei.pholocation.R;

import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.Toolbar;

import io.fabric.sdk.android.Fabric;

/**
 * Created by risaki on 2/22/16.
 */
public abstract class PhotoLocationActivity extends FragmentActivity {
    public Toolbar toolbar;

    protected boolean isKitKat = true;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(LayoutParams.FLAG_FULLSCREEN, LayoutParams.FLAG_FULLSCREEN);

//        AdRegistration.setAppKey(getString(R.string.amazon_ad_key));
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.splash);
        if (VERSION.SDK_INT > VERSION_CODES.KITKAT) {
            isKitKat = false;
            toolbar = (Toolbar) findViewById(R.id.toolbar);

            if (toolbar != null) {
                setActionBar(toolbar);
//            setSupportActionBar(toolbar);
            }
        }
    }
}
