package com.kalei.pholocation;

import com.google.android.gms.common.api.GoogleApiClient;

import com.kalei.utils.PhoLocationUtils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
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
    private static boolean mIsSent;
    private static String mMapLink;
    private static String mFileName;
    private final Location mLocation;
    private Handler mHandler;
    public GoogleApiClient mGoogleApiClient;
    private static int TIME_TO_WAIT_TO_GET_LOCATION = 10000;//wait 10 seconds to get location at MAX

    public PhotoLocationSender(Context context, String email, String filename) {
        mContext = context;
        mIsSent = false;
        mFileName = filename;
        mSender = new GMailSender("reidisaki", "Password01!");
        mLocation = MainActivity.mLocation;
        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!mIsSent && mLocation == null) {
                    mMapLink = "COULD NOT get location SORRY!, and didn't want to wait any longer";
                    new SendEmailAsyncTask().execute();
                    mIsSent = true;
                } else {
                    //waited 10 seconds maximum check to see if location was found yet.
                    tryGetLocation();
                }
            }
        }, TIME_TO_WAIT_TO_GET_LOCATION);

        tryGetLocation();
    }

    private void tryGetLocation() {
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
                if (!mIsSent) {
                    mIsSent = true;
                    new SendEmailAsyncTask().execute();
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
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
