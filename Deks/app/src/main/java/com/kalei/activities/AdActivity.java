package com.kalei.activities;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import com.kalei.pholocation.R;
import com.kalei.utils.PhotoLocationUtils;

import android.content.Intent;
import android.os.Bundle;

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
            public void onAdClosed() {

                if (PhotoLocationUtils.getEmailStringList(getApplicationContext()).length() == 0) {
                    startActivity(new Intent(AdActivity.this, IntroActivity.class));
                } else {
                    startActivity(new Intent(AdActivity.this, MainActivity.class));
                }
            }
        });
        requestNewInterstitial();
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("SEE_YOUR_LOGCAT_TO_GET_YOUR_DEVICE_ID")
                .build();

        mInterstitialAd.loadAd(adRequest);
    }
}
