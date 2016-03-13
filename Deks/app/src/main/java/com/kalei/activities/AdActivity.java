package com.kalei.activities;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import com.kalei.pholocation.R;
import com.kalei.utils.PhotoLocationUtils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Created by risaki on 2/21/16.
 */
public class AdActivity extends PhotoLocationActivity {
    InterstitialAd mInterstitialAd;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.admob_id));

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                mInterstitialAd.show();
            }

            @Override
            public void onAdFailedToLoad(final int errorCode) {
                super.onAdFailedToLoad(errorCode);
                gotoActivity();
            }

            @Override
            public void onAdClosed() {
                gotoActivity();
            }
        });
        final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);

        String deviceid = tm.getDeviceId();
        Log.i("pl", "deviceId: " + deviceid);
        requestNewInterstitial();
    }

    private void gotoActivity() {
        if (PhotoLocationUtils.getEmailStringList(getApplicationContext()).length() == 0) {
            startActivity(new Intent(AdActivity.this, IntroActivity.class));
        } else {
            startActivity(new Intent(AdActivity.this, MainActivity.class));
        }
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("990005115558117")
                .build();

        mInterstitialAd.loadAd(adRequest);
    }
}
