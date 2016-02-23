package com.kalei.pholocation;

import com.kalei.activities.MainActivity;
import com.kalei.interfaces.IMailListener;
import com.kalei.utils.PhotoLocationUtils;

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
    Context mContext;
    private boolean mIsSent;
    private String mMapLink;
    private String mFileName;
    private Location mLocation;
    private Handler mHandler;
    public IMailListener mMailListener;
    private static int TIME_TO_WAIT_TO_GET_LOCATION = 10000;//wait 10 seconds to get location at MAX

    public PhotoLocationSender(Context context, String email, String filename, IMailListener listener) {
        mContext = context;
        mIsSent = false;
        mFileName = filename;
        mSender = new GMailSender(mContext.getString(R.string.username), mContext.getString(R.string.password), listener);
//        mSender.setOnMailListener(mMailListener);
        mLocation = MainActivity.mLocation;
        mHandler = new Handler();
        setOnMailListener(listener);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mLocation = MainActivity.mLocation;
                if (mLocation == null) {
                    Log.i("Reid", "couldn't get location");
                    //waited 10 seconds maximum check to see if location was found yet.
                    tryGetLocation();
                }
                if (!mIsSent) {
                    Log.i("Reid", "waited 10 seconds trying to send now");
                    mMapLink = "COULD NOT get location SORRY!, and didn't want to wait any longer";
                    new SendEmailAsyncTask().execute();
                    mIsSent = true;
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
            } else {
                Log.i("Reid", "mLocation was null");
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public void setOnMailListener(final IMailListener mailListener) {
        mMailListener = mailListener;
    }

    public void sendMail() {
        Log.i("Reid", "SendMail called()");
        if (BuildConfig.DEBUG) {
            Log.v(SendEmailAsyncTask.class.getName(), "doInBackground()");
        }
        try {
            Date d = new Date();
            mSender.sendMail("new image " + d.toString(),
                    mMapLink,
                    mContext.getString(R.string.username) + "@yahoo.com",
                    PhotoLocationUtils.getData(mContext).get(PhotoLocationUtils.EMAIL_KEY), mFileName);
        } catch (AuthenticationFailedException e) {
            Log.i("Reid", "Not sending authentication failure");
            Log.e(SendEmailAsyncTask.class.getName(), "Bad account details");
            e.printStackTrace();
        } catch (MessagingException e) {
            Log.i("Reid", "Not sending messaging exception: " + e.getMessage());
//                Log.e(SendEmailAsyncTask.class.getName(), mSender.getTo(null) + "failed");
            e.printStackTrace();
        } catch (Exception e) {
            Log.i("Reid", "Not sending exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    class SendEmailAsyncTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            if (BuildConfig.DEBUG) {
                Log.v(SendEmailAsyncTask.class.getName(), "doInBackground()");
            }
            try {
                Date d = new Date();
                mSender.sendMail("new image " + d.toString(),
                        mMapLink,
                        mContext.getString(R.string.username) + "@yahoo.com",
                        PhotoLocationUtils.getData(mContext).get(PhotoLocationUtils.EMAIL_KEY), mFileName);
                return true;
            } catch (AuthenticationFailedException e) {
                Log.i("Reid", "Not sending authentication failure");
                Log.e(SendEmailAsyncTask.class.getName(), "Bad account details");
                e.printStackTrace();
                return false;
            } catch (MessagingException e) {
                Log.i("Reid", "Not sending messaging exception: " + e.getMessage());
//                Log.e(SendEmailAsyncTask.class.getName(), mSender.getTo(null) + "failed");
                e.printStackTrace();
                return false;
            } catch (Exception e) {
                Log.i("Reid", "Not sending exception: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }
    }
}
