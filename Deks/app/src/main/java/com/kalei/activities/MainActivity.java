package com.kalei.activities;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;

import com.crashlytics.android.Crashlytics;
import com.flurry.android.FlurryAgent;
import com.kalei.PhotoLocationApplication;
import com.kalei.fragments.CameraFragment;
import com.kalei.fragments.SettingsFragment;
import com.kalei.interfaces.ICameraClickListener;
import com.kalei.interfaces.IMailListener;
import com.kalei.pholocation.R;

import android.Manifest.permission;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import java.util.Date;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends PhotoLocationActivity implements IMailListener, ConnectionCallbacks, OnConnectionFailedListener, ICameraClickListener {
    public CameraFragment mCameraFragment;
    public SettingsFragment mSettingsFragment;
    private GoogleApiClient mGoogleApiClient;
    public static Location mLocation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FlurryAgent.init(this, PhotoLocationApplication.FLURRY_KEY);
        FlurryAgent.onStartSession(this);
        Fabric.with(this, new Crashlytics());
        checkLocation();
        loadToolbar("Settings");
        mCameraFragment = CameraFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.camera_container, mCameraFragment).commit();
        toolbar.setNavigationOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                getSupportFragmentManager().beginTransaction().replace(R.id.camera_container, mCameraFragment).commit();
            }
        });
    }

    public void loadToolbar(String title) {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public void onConnected(final Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        Log.i("Reid", "got location" + mLocation.getLongitude());
    }

    @Override
    public void onConnectionSuspended(final int i) {

    }

    private void checkLocation() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();
    }

    @Override
    public void onMailFailed(final Exception e) {
        FlurryAgent.logEvent("Mail failed: " + e.getMessage());
        Toast.makeText(this, "Could not send email: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onMailSucceeded() {
        Date d = new Date();
        FlurryAgent.logEvent("mail SUCCESS! " + d.toString());
        Toast.makeText(this, "Picture sent successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(final ConnectionResult connectionResult) {

    }

    @Override
    public void onSettingsClicked() {
        mSettingsFragment = SettingsFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.camera_container, mSettingsFragment).commit();
    }
}
