package com.kalei.activities;

import com.crashlytics.android.Crashlytics;
import com.flurry.android.FlurryAgent;
import com.kalei.IMailListener;
import com.kalei.PhotoLocationApplication;
import com.kalei.fragments.CameraFragment;
import com.kalei.fragments.SettingsFragment;
import com.kalei.pholocation.R;

import android.os.Bundle;

import java.util.Date;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends PhotoLocationActivity implements IMailListener {
    public CameraFragment mCameraFragment;
    public SettingsFragment mSettingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FlurryAgent.init(this, PhotoLocationApplication.FLURRY_KEY);
        FlurryAgent.onStartSession(this);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        mCameraFragment = CameraFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.camera_container, mCameraFragment).commit();
    }

    @Override
    public void onMailFailed(final Exception e) {
        FlurryAgent.logEvent("Mail failed: " + e.getMessage());
    }

    @Override
    public void onMailSucceeded() {
        Date d = new Date();
        FlurryAgent.logEvent("mail SUCCESS! " + d.toString());
    }
}
