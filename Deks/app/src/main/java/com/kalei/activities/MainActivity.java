package com.kalei.activities;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
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
import com.kalei.utils.PhotoLocationUtils;

import android.Manifest.permission;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.util.Date;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends PhotoLocationActivity implements IMailListener, ConnectionCallbacks, OnConnectionFailedListener, ICameraClickListener {
    public CameraFragment mCameraFragment;
    public SettingsFragment mSettingsFragment;
    private GoogleApiClient mGoogleApiClient;
    public static Location mLocation;
    NotificationCompat.Builder mBuilder;
    NotificationManager mNotificationManager;

    InterstitialAd mInterstitialAd;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBuilder = new NotificationCompat.Builder(this);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (savedInstanceState != null) {
            mSettingsFragment = (SettingsFragment) getSupportFragmentManager().findFragmentByTag("settings");
        }
        if (PhotoLocationUtils.getEmailStringList(this).length() == 0) {
            mSettingsFragment = SettingsFragment.newInstance();
        }
        setContentView(R.layout.activity_main);
        FlurryAgent.init(this, PhotoLocationApplication.FLURRY_KEY);
        FlurryAgent.onStartSession(this);
        Fabric.with(this, new Crashlytics());
        checkLocation();
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
        if (PhotoLocationUtils.getEmailStringList(getApplicationContext()).length() > 0) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
            getSupportFragmentManager().beginTransaction().replace(R.id.camera_container, mCameraFragment).commit();
        } else {
            Toast.makeText(getApplicationContext(), "Please enter a valid email address. ", Toast.LENGTH_SHORT).show();
        }
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
    public void onMailFailed(final Exception e, String imageName) {
        FlurryAgent.logEvent("Mail failed: " + e.getMessage());
//        Toast.makeText(this, "Could not send email: " + e.getMessage(), Toast.LENGTH_LONG).show();
        mBuilder.setSmallIcon(R.drawable.fart_backup);
        mBuilder.setContentTitle("Failed sending picture");
        mBuilder.setContentText("Sorry, I wasn't able to send this image: " + imageName);
        mNotificationManager.notify(0, mBuilder.build());
    }

    @Override
    public void onMailSucceeded(String imageName) {
        Date d = new Date();
        FlurryAgent.logEvent("mail SUCCESS! " + d.toString());
//        Toast.makeText(this, "Picture sent successfully", Toast.LENGTH_SHORT).show();

        mBuilder.setSmallIcon(R.drawable.fart_backup);
        mBuilder.setContentTitle("Sent picture successfully");
        mBuilder.setContentText("this image: " + imageName);
        mNotificationManager.notify(0, mBuilder.build());
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

    @Override
    public void onNoEmailSet() {
        Toast.makeText(this, "No email has been set yet", Toast.LENGTH_LONG).show();
        onSettingsClicked();
    }

    public void requestNewInterstitial() {
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.admob_id));

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                mInterstitialAd.show();
            }

            @Override
            public void onAdClosed() {

            }
        });

        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("SEE_YOUR_LOGCAT_TO_GET_YOUR_DEVICE_ID")
                .build();

        mInterstitialAd.loadAd(adRequest);
    }
}