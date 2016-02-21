package com.kalei.pholocation;

import com.kalei.utils.PhoLocationUtils;
import com.kalei.views.CaptureView;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity implements OnClickListener {

    private ImageView mSettingsImage, mShutter;
    private EditText mEditEmail;
    private CaptureView mCaptureView;
    private FrameLayout mShutterScreen;
    public static String EMAIL_KEY = "email_key";
    public static String MY_PREFS_NAME = "photolocation";
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        //1. camera activity
        //2. add gear icon to screen
        //3. show amodal where you can update the email address /save to shared preferences
        //4. create a google map snapshot of where the picture was taken
        //5. after a user takes a picture send it to an email address  attach both items and send automatically
    }

    @Override
    public void onClick(final View v) {
        Log.i("Reid", "view was clicked");
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
}
