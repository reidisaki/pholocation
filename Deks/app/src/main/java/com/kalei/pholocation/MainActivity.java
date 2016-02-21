package com.kalei.pholocation;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;

import com.crashlytics.android.Crashlytics;
import com.kalei.utils.PhoLocationUtils;
import com.kalei.views.CaptureView;

import android.Manifest.permission;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.Map;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends Activity implements OnClickListener, ConnectionCallbacks, OnConnectionFailedListener {

    private ImageView mSettingsImage, mShutter;
    private EditText mEditEmail;
    private CaptureView mCaptureView;
    private FrameLayout mShutterScreen;
    public static String EMAIL_KEY = "email_key";
    public static String MY_PREFS_NAME = "photolocation";
    private Handler mHandler;
    public GoogleApiClient mGoogleApiClient;
    public static Location mLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        mSettingsImage = (ImageView) findViewById(R.id.settings_image);
        mSettingsImage.setOnClickListener(this);
        mEditEmail = (EditText) findViewById(R.id.email_edit);
        mShutter = (ImageView) findViewById(R.id.shutter);
        mShutter.setOnClickListener(this);
        mCaptureView = (CaptureView) findViewById(R.id.capture_view);
        mCaptureView.setOnClickListener(this);
        mEditEmail.setText(PhoLocationUtils.getData(this).get(EMAIL_KEY));
        mShutterScreen = (FrameLayout) findViewById(R.id.shutterScreen);
        mHandler = new Handler();
        checkLocation();
        //1. camera activity
        //2. add gear icon to screen
        //3. show amodal where you can update the email address /save to shared preferences
        //4. create a google map snapshot of where the picture was taken
        //5. after a user takes a picture send it to an email address  attach both items and send automatically
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
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.settings_image:
                mEditEmail.setText(PhoLocationUtils.getData(this).get(EMAIL_KEY));
                mEditEmail.setVisibility(View.VISIBLE);
                mShutter.setVisibility(View.GONE);
                break;
            case R.id.shutter:

                mCaptureView.takeAPicture(this, mEditEmail.getText().toString());
                shutterShow();
                break;
            default:
                onSaveData();
                mShutter.setVisibility(View.VISIBLE);
                mEditEmail.setVisibility(View.GONE);
                break;
        }
    }

    private void shutterShow() {
        mShutterScreen.setVisibility(View.VISIBLE);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mShutterScreen.setVisibility(View.GONE);
            }
        }, 500);
    }

    private void onSaveData() {
        Map<String, String> map = new HashMap<>();
        map.put(EMAIL_KEY, mEditEmail.getText().toString());
        PhoLocationUtils.saveData(map, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        onSaveData();
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

    @Override
    public void onConnectionFailed(final ConnectionResult connectionResult) {

    }
}
