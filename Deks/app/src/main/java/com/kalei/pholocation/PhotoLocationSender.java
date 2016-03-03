package com.kalei.pholocation;

import com.kalei.activities.MainActivity;
import com.kalei.interfaces.IMailListener;
import com.kalei.managers.PrefManager;
import com.kalei.models.Photo;
import com.kalei.utils.PhotoLocationUtils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
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
    private String mFileName, mScaledImage;
    private Location mLocation;
    private Handler mHandler;
    public IMailListener mMailListener;
    private static int TIME_TO_WAIT_TO_GET_LOCATION = 10000;//wait 10 seconds to get location at MAX

    public PhotoLocationSender(Context context, String filename, IMailListener listener, String scaledImage) {
        mContext = context;
        mIsSent = false;
        mFileName = filename;
        mScaledImage = scaledImage;
        mSender = new GMailSender(mContext.getString(R.string.username), mContext.getString(R.string.password), listener);
//        mSender.setOnMailListener(mMailListener);

        mLocation = MainActivity.mLocation;
        mHandler = new Handler(Looper.getMainLooper());
        setOnMailListener(listener);
        if (PhotoLocationUtils.isConnectedFast(mContext)) {
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
                        mMapLink = "COULD NOT get location SORRY!, and didn't want to wait any longer. Is GPS enabled? \n\n\n\n\n\n\n -sent by PhotoLocation, download the app here: https://play.google.com/store/apps/details?id=com.kalei.pholocation";
                        new SendEmailAsyncTask().execute();

                        mIsSent = true;
                    }
                }
            }, TIME_TO_WAIT_TO_GET_LOCATION);

            tryGetLocation();
        } else {
            mMapLink = "COULD NOT get location SORRY!, and didn't want to wait any longer. Is GPS enabled? \n\n\n\n\n\n\n -sent by PhotoLocation, download the app here: https://play.google.com/store/apps/details?id=com.kalei.pholocation";
            new SendEmailAsyncTask().execute();
        }
    }

    private void tryGetLocation() {
        Geocoder geoCoder = new Geocoder(mContext, Locale.getDefault());
        String mapLink = "";

        if (mLocation != null) {
            String add = "";
            try {
                List<Address> addresses = geoCoder.getFromLocation(mLocation.getLatitude(), mLocation.getLongitude(), 1);

                if (addresses.size() > 0) {
                    for (int i = 0; i < addresses.get(0).getMaxAddressLineIndex(); i++) {
                        add += addresses.get(0).getAddressLine(i) + ",";
                    }
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            if (add.length() > 0) {
                add = add.substring(0, add.length() - 1);//remove trailing comma
            } else {
                add = "could not get a data connection to get address";
            }

            mMapLink = "http://maps.google.com/?q=" + mLocation.getLatitude() + "," + mLocation.getLongitude() +
                    "\n\n" + add +
                    "\n\n\n\n\n -sent by PhotoLocation, download the app here: https://play.google.com/store/apps/details?id=com.kalei.pholocation ";
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
            mSender.sendMail(d.toString() + " " + mScaledImage,
                    mMapLink,
                    mContext.getString(R.string.username) + "@yahoo.com",
                    PhotoLocationUtils.getEmailStringList(mContext), mFileName, mScaledImage);
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
                ConnectivityManager connManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                if ((PrefManager.getSendWifiOnly(mContext) && !mWifi.isConnected()) || !PhotoLocationUtils.isConnectedFast(mContext)) {
                    //only send through wifi send later
                    Photo p = new Photo();
                    p.setScaledImage(mScaledImage);
                    p.setDateTaken(d);
                    p.setLocation(mLocation);
                    p.setMapLink(mMapLink);
                    p.setFileName(mFileName);
                    PrefManager.setPhoto(mContext, p);
                    Log.i("Reid", "postponing sending");
                } else {
                    Log.i("Reid", "sending");
                    if (mWifi.isConnected() && PrefManager.getSendWifiOnly(mContext) ||
                            (!PrefManager.getSendWifiOnly(mContext) && PhotoLocationUtils.isConnectedFast(mContext))) {
                        mSender.sendMail(d.toString() + " " + mScaledImage,
                                mMapLink,
                                mContext.getString(R.string.username) + "@yahoo.com",
                                PhotoLocationUtils.getEmailStringList(mContext), mFileName, mScaledImage);
                    }
                }
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
