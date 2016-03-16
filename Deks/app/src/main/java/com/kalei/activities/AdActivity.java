package com.kalei.activities;

import com.amazon.device.ads.Ad;
import com.amazon.device.ads.AdError;
import com.amazon.device.ads.AdListener;
import com.amazon.device.ads.AdProperties;
import com.amazon.device.ads.InterstitialAd;
import com.kalei.utils.PhotoLocationUtils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

public class AdActivity extends PhotoLocationActivity {
    InterstitialAd interstitialAd;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create the interstitial.
        interstitialAd = new InterstitialAd(this);

        // Set the listener to use the callbacks below.
        interstitialAd.setListener(new AdListener() {
            @Override
            public void onAdLoaded(final Ad ad, final AdProperties adProperties) {
                interstitialAd.showAd();
            }

            @Override
            public void onAdFailedToLoad(final Ad ad, final AdError adError) {
                Log.i("pl", "ad failed: " + adError.getMessage());
                gotoActivity();
            }

            @Override
            public void onAdExpanded(final Ad ad) {

            }

            @Override
            public void onAdCollapsed(final Ad ad) {

            }

            @Override
            public void onAdDismissed(final Ad ad) {
                gotoActivity();
            }
        });

        // Load the interstitial.
        interstitialAd.loadAd();
        final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);

        String deviceid = tm.getDeviceId();
        Log.i("pl", "deviceId: " + deviceid);
    }

    private void gotoActivity() {
        if (PhotoLocationUtils.getEmailStringList(getApplicationContext()).length() == 0) {
            startActivity(new Intent(AdActivity.this, IntroActivity.class));
        } else {
            startActivity(new Intent(AdActivity.this, MainActivity.class));
        }
    }
}
/**
 * Created by risaki on 2/21/16.
 * <p/>
 * public class AdActivity extends PhotoLocationActivity { InterstitialAd mInterstitialAd;
 *
 * @Override public void onCreate(final Bundle savedInstanceState) { super.onCreate(savedInstanceState);
 * <p/>
 * mInterstitialAd = new InterstitialAd(this); mInterstitialAd.setAdUnitId(getString(R.string.admob_id));
 * <p/>
 * mInterstitialAd.setAdListener(new AdListener() {
 * @Override public void onAdLoaded() { super.onAdLoaded(); mInterstitialAd.show(); }
 * @Override public void onAdFailedToLoad(final int errorCode) { super.onAdFailedToLoad(errorCode); gotoActivity(); }
 * @Override public void onAdClosed() { gotoActivity(); } }); final TelephonyManager tm = (TelephonyManager)
 * getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
 * <p/>
 * String deviceid = tm.getDeviceId(); Log.i("pl", "deviceId: " + deviceid); requestNewInterstitial();
 * <p/>
 * <p/>
 * new Handler().postDelayed(new Runnable() { public void run() { gotoActivity(); } }, 2000); }
 * <p/>
 * private void gotoActivity() { if (PhotoLocationUtils.getEmailStringList(getApplicationContext()).length() == 0) { startActivity(new Intent(AdActivity.this,
 * IntroActivity.class)); } else { startActivity(new Intent(AdActivity.this, MainActivity.class)); } }
 * <p/>
 * private void requestNewInterstitial() { AdRequest adRequest = new AdRequest.Builder() .addTestDevice("990005115558117") .build();
 * <p/>
 * mInterstitialAd.loadAd(adRequest); } }
 */