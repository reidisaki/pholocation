package com.kalei.pholocation;

import com.google.android.gms.maps.GoogleMapOptions;

import com.kalei.views.CaptureView;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class MainActivity extends Activity implements OnClickListener {

    private ImageView mSettingsImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSettingsImage = (ImageView) findViewById(R.id.settings_image);
        mSettingsImage.setOnClickListener(this);
        //1. camera activity
        //2. add gear icon to screen
        //3. show amodal where you can update the email address /save to shared preferences
        //4. create a google map snapshot of where the picture was taken
        //5. after a user takes a picture send it to an email address  attach both items and send automatically
        GoogleMapOptions options = new GoogleMapOptions().liteMode(true);
    }

    @Override
    public void onClick(final View v) {
        if (v.getId() == R.id.settings_image) {
            Log.i("Reid", "settings was clicked show modal");
            CaptureView.takeAPicture(this);
        }
    }
}
