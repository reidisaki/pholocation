package com.kalei.activities;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import com.amazon.device.ads.Ad;
import com.amazon.device.ads.AdError;
import com.amazon.device.ads.AdListener;
import com.amazon.device.ads.AdProperties;
import com.amazon.device.ads.InterstitialAd;
import com.flurry.android.FlurryAgent;
import com.kalei.PhotoLocationApplication;
import com.kalei.fragments.CameraFragment;
import com.kalei.fragments.SettingsFragment;
import com.kalei.interfaces.ICameraClickListener;
import com.kalei.pholocation.R;
import com.kalei.utils.PhotoLocationUtils;

import android.Manifest.permission;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import android.widget.Toolbar;

public class MainActivity extends PhotoLocationActivity implements ConnectionCallbacks, OnConnectionFailedListener, ICameraClickListener,
                                                                   LocationListener {
    public CameraFragment mCameraFragment;
    public SettingsFragment mSettingsFragment;
    private GoogleApiClient mGoogleApiClient;
    public static Location mLocation;
    InterstitialAd mInterstitialAd;
    public static int currentCameraId = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mSettingsFragment = (SettingsFragment) getSupportFragmentManager().findFragmentByTag("settings");
        }
        if (PhotoLocationUtils.getEmailStringList(this).length() == 0) {
            mSettingsFragment = SettingsFragment.newInstance();
        }
        setContentView(R.layout.activity_main);
        FlurryAgent.init(this, PhotoLocationApplication.FLURRY_KEY);
        FlurryAgent.onStartSession(this);
        loadToolbar("Settings");
        mCameraFragment = CameraFragment.newInstance();
        if (mSettingsFragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.camera_container, mSettingsFragment).commit();
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.camera_container, mCameraFragment).commit();
        }
        toolbar.setNavigationOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                clickBack();
                //hide keyboard
                if (v != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });
    }

    public void clickBack() {
        if (PhotoLocationUtils.getEmailStringList(getApplicationContext()).length() > 0 ||
                PhotoLocationUtils.isValidEmail(mSettingsFragment.emailRetv.getText())) {
            getSupportFragmentManager().beginTransaction().replace(R.id.camera_container, mCameraFragment).commit();
        } else {
            Toast.makeText(getApplicationContext(), "Please enter a valid email address. ", Toast.LENGTH_SHORT).show();
        }
    }

    public void loadToolbar(String title) {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setActionBar(toolbar);
            getActionBar().setTitle(title);
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public void onConnected(final Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.i("pl", "Failed onConnected permissions");
            return;
        }
        Log.i("pl", "requesting location updates");
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, createLocationRequest(), this);
    }

    protected LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        return mLocationRequest;
    }

    @Override
    public void onConnectionSuspended(final int i) {

    }

    private void checkLocation() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .enableAutoManage(this, this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(final ConnectionResult connectionResult) {

    }

    @Override
    public void onSettingsClicked() {
        mSettingsFragment = SettingsFragment.newInstance();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportFragmentManager().beginTransaction().replace(R.id.camera_container, mSettingsFragment, "settings").commit();
    }

    public void requestNewInterstitial() {

        // Set the listener to use the callbacks below.

        final InterstitialAd interstitialAd = new InterstitialAd(this);

        // Set the listener to use the callbacks below.
        interstitialAd.setListener(new AdListener() {
            @Override
            public void onAdLoaded(final Ad ad, final AdProperties adProperties) {
                interstitialAd.showAd();
            }

            @Override
            public void onAdFailedToLoad(final Ad ad, final AdError adError) {
                Log.i("pl", "ad failed: " + adError.getMessage());
            }

            @Override
            public void onAdExpanded(final Ad ad) {

            }

            @Override
            public void onAdCollapsed(final Ad ad) {

            }

            @Override
            public void onAdDismissed(final Ad ad) {

            }
        });

        // Load the interstitial.
        interstitialAd.loadAd();
        //GOOGLE BS
//        mInterstitialAd = new InterstitialAd(this);
//        mInterstitialAd.setAdUnitId(getString(R.string.admob_id));
//
//        mInterstitialAd.setAdListener(new AdListener() {
//            @Override
//            public void onAdLoaded() {
//                super.onAdLoaded();
//                mInterstitialAd.show();
//            }
//
//            @Override
//            public void onAdClosed() {
//
//            }
//        });
//
//        AdRequest adRequest = new AdRequest.Builder()
//                .addTestDevice("990005115558117")
//                .build();
//        final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
//
//        String deviceid = tm.getDeviceId();
//        Log.i("pl", "deviceId: " + deviceid);
//
//        mInterstitialAd.loadAd(adRequest);
    }

    @Override
    public void onBackPressed() {
        if ((mSettingsFragment != null && mSettingsFragment.isVisible())) {
            clickBack();
        } else if (!mCameraFragment.shouldBackOutofApp) {
            mCameraFragment.showCamera();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onLocationChanged(final Location location) {
        Log.i("pl", "location changed: " + location.toString());
        mLocation = location;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("pl", "removing location updates");
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkLocation();
    }
}