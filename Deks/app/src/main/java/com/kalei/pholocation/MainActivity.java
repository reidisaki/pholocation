package com.kalei.pholocation;

import com.google.android.gms.maps.GoogleMapOptions;

import com.kalei.views.CaptureView;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity implements OnClickListener {

    private ImageView mSettingsImage, mShutter;
    private EditText mEditEmail;
    private CaptureView mCaptureView;
    private static String EMAIL_KEY = "email_key";
    private static String MY_PREFS_NAME = "photolocation";

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

        //1. camera activity
        //2. add gear icon to screen
        //3. show amodal where you can update the email address /save to shared preferences
        //4. create a google map snapshot of where the picture was taken
        //5. after a user takes a picture send it to an email address  attach both items and send automatically
        GoogleMapOptions options = new GoogleMapOptions().liteMode(true);
    }

    @Override
    public void onClick(final View v) {
        Log.i("Reid", "view was clicked");
        switch (v.getId()) {
            case R.id.settings_image:
                mEditEmail.setText(getData().get(EMAIL_KEY));
                mEditEmail.setVisibility(View.VISIBLE);
                mShutter.setVisibility(View.GONE);
                break;
            case R.id.shutter:
                CaptureView.takeAPicture(this, mEditEmail.getText().toString());
                break;
            default:
                onSaveData();
                mShutter.setVisibility(View.VISIBLE);
                mEditEmail.setVisibility(View.GONE);
                break;
        }
    }

    private void onSaveData() {
        Map<String, String> map = new HashMap<>();
        map.put(EMAIL_KEY, mEditEmail.getText().toString());
        saveData(map);
    }

    @Override
    protected void onPause() {
        super.onPause();
        onSaveData();
    }

    private void saveData(Map<String, String> map) {
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        for (String i : map.keySet()) {
            editor.putString(i, map.get(i));
        }
        editor.commit();
    }

    private Map<String, String> getData() {
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        Map<String, String> map = new HashMap<String, String>();
        map.put(EMAIL_KEY, prefs.getString(EMAIL_KEY, "pchung528+catchall@gmail.com"));//"No name defined" is the default value.
        return map;
    }
}
