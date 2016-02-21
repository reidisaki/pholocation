package com.kalei.pholocation;

import com.kalei.utils.PhoLocationUtils;

import android.Manifest.permission;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;

/**
 * Created by risaki on 2/20/16.
 */
public class PhotoLocationSender {

    private static GMailSender mSender;
    static Context mContext;
    private final String mEmail;
    private LocationManager mLocationManager;
    private static int LOCATION_REFRESH_TIME = 5000;
    private static int LOCATION_REFRESH_DISTANCE = 50;
    private final LocationListener mLocationListener;
    private static Location mLocation;
    private static boolean mIsSent;
    private static String mMapLink;
    private static String mFileName;
    private Handler mHandler;
    private static int TIME_TO_WAIT_TO_GET_LOCATION = 10000;//wait 10 seconds to get location at MAX

    public PhotoLocationSender(Context context, String email, String filename) {
        mContext = context;
        mIsSent = false;
        mFileName = filename;
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        mSender = new GMailSender("reidisaki", "Password01!");

        mEmail = email;
        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!mIsSent) {
                    mMapLink = "COULD NOT get location SORRY!, and didn't want to wait any longer";
                    new SendEmailAsyncTask().execute();
                    mIsSent = true;
                }
            }
        }, TIME_TO_WAIT_TO_GET_LOCATION);

        mLocationListener = new

                LocationListener() {
                    @Override
                    public void onLocationChanged(final Location location) {
                        //your code here
                        mLocation = location;
                        Geocoder geoCoder = new Geocoder(mContext, Locale.getDefault());
                        String mapLink = "";

                        try {
                            if (mLocation != null) {
                                List<Address> addresses = geoCoder.getFromLocation(mLocation.getLatitude(), mLocation.getLongitude(), 1);

                                String add = "";
                                if (addresses.size() > 0) {
                                    for (int i = 0; i < addresses.get(0).getMaxAddressLineIndex(); i++) {
                                        add += addresses.get(0).getAddressLine(i) + ",";
                                    }
                                }

                                mMapLink = "http://maps.google.com/?q=" + add.replace(" ", "+");
//                        mMapLink = "https://maps.googleapis.com/maps/api/staticmap?center=" + add +
//                                "i&zoom=17&size=600x300&maptype=roadmap&markers=color:red%7Clabel:X%7C" +
//                                mLocation.getLatitude() + "," + mLocation.getLongitude() + "&key=AIzaSyBryuOc-tskt2bkYh_vxfYq_HVRW5ddjoI";
                                Log.i("Reid", mapLink);
                            }
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        if (!mIsSent) {
                            mIsSent = true;
                            new SendEmailAsyncTask().execute();
                        }
                    }

                    @Override
                    public void onStatusChanged(final String provider, final int status, final Bundle extras) {

                    }

                    @Override
                    public void onProviderEnabled(final String provider) {

                    }

                    @Override
                    public void onProviderDisabled(final String provider) {

                    }
                }

        ;
        if (ActivityCompat.checkSelfPermission(mContext, permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext, permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)

        {
            return;
        }

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                LOCATION_REFRESH_DISTANCE, mLocationListener);
    }

    class SendEmailAsyncTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            if (BuildConfig.DEBUG) {
                Log.v(SendEmailAsyncTask.class.getName(), "doInBackground()");
            }
            try {
                Log.i("Reid", "do in background async task");
                Date d = new Date();
                mSender.sendMail("new image " + d.toString(),
                        mMapLink,
                        "reidisaki@yahoo.com",
                        PhoLocationUtils.getData(mContext).get(MainActivity.EMAIL_KEY), mFileName);
                return true;
            } catch (AuthenticationFailedException e) {
                Log.e(SendEmailAsyncTask.class.getName(), "Bad account details");
                e.printStackTrace();
                return false;
            } catch (MessagingException e) {
//                Log.e(SendEmailAsyncTask.class.getName(), mSender.getTo(null) + "failed");
                e.printStackTrace();
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}
