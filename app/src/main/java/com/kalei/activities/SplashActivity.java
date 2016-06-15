package com.kalei.activities;

import android.Manifest.permission;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by risaki on 2/21/16.
 */
public class SplashActivity extends PhotoLocationActivity {
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ContextCompat.checkSelfPermission(this,
                permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{permission.CAMERA, permission.ACCESS_COARSE_LOCATION, permission.ACCESS_FINE_LOCATION,
                    permission.INTERNET, permission.WRITE_EXTERNAL_STORAGE, permission.ACCESS_WIFI_STATE, permission.READ_CONTACTS}, 0);
        } else {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    startActivity(new Intent(SplashActivity.this, AdActivity.class));
                }
            }, 200);
        }
    }

    public void onRequestPermissionsResult(int requestCode,
            String[] permissions,
            int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0) {
                startActivity(new Intent(SplashActivity.this, AdActivity.class));
            } else {
                finish();

                // Permission was denied or request was cancelled
            }
        }
    }
}
