package com.kalei.activities;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

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
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.InboxStyle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends PhotoLocationActivity implements IMailListener, ConnectionCallbacks, OnConnectionFailedListener, ICameraClickListener,
                                                                   LocationListener {
    public CameraFragment mCameraFragment;
    public SettingsFragment mSettingsFragment;
    private GoogleApiClient mGoogleApiClient;
    public static Location mLocation;
    NotificationCompat.Builder mBuilder;
    NotificationManager mNotificationManager;
    public static int mSuccessfulSends = 0;
    public static int mFailedSends = 0;
    InterstitialAd mInterstitialAd;
    public List<String> imageFileNames;
    public List<String> imageFailedFileNames;
    public static int currentCameraId = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBuilder = new NotificationCompat.Builder(this);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        imageFileNames = new ArrayList<>();
        imageFailedFileNames = new ArrayList<>();
        if (savedInstanceState != null) {
            mSettingsFragment = (SettingsFragment) getSupportFragmentManager().findFragmentByTag("settings");
        }
        if (PhotoLocationUtils.getEmailStringList(this).length() == 0) {
            mSettingsFragment = SettingsFragment.newInstance();
        }
        setContentView(R.layout.activity_main);
        FlurryAgent.init(this, PhotoLocationApplication.FLURRY_KEY);
        FlurryAgent.onStartSession(this);
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
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();
    }

    @Override
    public void onMailFailed(final Exception e, String imageName) {
        FlurryAgent.logEvent("Mail failed: " + e.getMessage());
        mFailedSends++;
//        Toast.makeText(this, "Could not send email: " + e.getMessage(), Toast.LENGTH_LONG).show();
        Intent intent = new Intent(NOTIFICATION_DELETED_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        registerReceiver(receiver, new IntentFilter(NOTIFICATION_DELETED_ACTION));
        imageName = imageName.substring(imageName.lastIndexOf("/") + 1, imageName.length());
        mBuilder.setSmallIcon(R.drawable.ic_launcher);
        mBuilder.setContentTitle("Failed sending picture" + mBuilder.setContentText(imageName));
        mBuilder.setDeleteIntent(pendingIntent);
        mBuilder.setContentText(mFailedSends + (mFailedSends == 1 ? " picture " : " pictures ") + " failed sending" + imageName);
        imageFailedFileNames.add(imageName);
        InboxStyle style = new InboxStyle().setSummaryText(mFailedSends + " failed to send");
        for (String s : imageFailedFileNames) {
            style.addLine(s);
        }
        mBuilder.setStyle(style);
        mNotificationManager.notify(1, mBuilder.build());
    }

    @Override
    public void onMailSucceeded(String imageName) {
        Date d = new Date();
        mSuccessfulSends++;
        FlurryAgent.logEvent("mail SUCCESS! " + d.toString());
        Intent intent = new Intent(NOTIFICATION_DELETED_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        registerReceiver(receiver, new IntentFilter(NOTIFICATION_DELETED_ACTION));
        imageName = imageName.substring(imageName.lastIndexOf("/") + 1, imageName.length());
        mBuilder.setSmallIcon(R.drawable.ic_launcher);
        mBuilder.setContentTitle(mSuccessfulSends + (mSuccessfulSends == 1 ? " picture " : " pictures ") + "sent successfully");
        mBuilder.setContentText(imageName);
        mBuilder.setDeleteIntent(pendingIntent);
        imageFileNames.add(imageName);
        InboxStyle style = new InboxStyle().setSummaryText(mSuccessfulSends + " sent");
        for (String s : imageFileNames) {
            style.addLine(s);
        }
        mBuilder.setStyle(style);

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

    private static final String NOTIFICATION_DELETED_ACTION = "NOTIFICATION_DELETED";

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mSuccessfulSends = 0;
            mFailedSends = 0;
            imageFailedFileNames.clear();
            imageFileNames.clear();
            unregisterReceiver(this);
        }
    };

    @Override
    public void onBackPressed() {
        if (mSettingsFragment != null && mSettingsFragment.isVisible()) {
            clickBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onLocationChanged(final Location location) {
        mLocation = location;
    }
}