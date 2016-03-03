package com.kalei.receivers;

import com.flurry.android.FlurryAgent;
import com.kalei.interfaces.IMailListener;
import com.kalei.managers.PrefManager;
import com.kalei.models.Photo;
import com.kalei.pholocation.GMailSender;
import com.kalei.pholocation.R;
import com.kalei.utils.PhotoLocationUtils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.InboxStyle;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by risaki on 3/2/16.
 */
public class WifiReceiver extends BroadcastReceiver {
    NotificationCompat.Builder mBuilder;
    NotificationManager mNotificationManager;
    public static int mSuccessfulSends = 0;
    public static int mFailedSends = 0;
    public List<String> imageFileNames;
    public List<String> imageFailedFileNames;
    private static final String NOTIFICATION_DELETED_ACTION = "NOTIFICATION_DELETED";

    @Override
    public void onReceive(final Context context, Intent intent) {
        mBuilder = new NotificationCompat.Builder(context);
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        imageFileNames = new ArrayList<>();
        imageFailedFileNames = new ArrayList<>();

        NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        if (isOnline(context)) {
            Log.i("Reid", "Connected to wifi and sending data");
            //TODO: send photos through email here, and if wifi connectivity is lost. back out.. phase 2
            List<Photo> photoList = PrefManager.getPhotoList(context);
            for (Photo p : photoList) {
                GMailSender mSender = new GMailSender(context.getString(R.string.username), context.getString(R.string.password), new IMailListener() {
                    @Override
                    public void onMailFailed(final Exception e, String imageName) {
                        FlurryAgent.logEvent("Mail failed: " + e.getMessage());
                        mFailedSends++;
                        Intent intent = new Intent(NOTIFICATION_DELETED_ACTION);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
                        imageName = imageName.substring(imageName.lastIndexOf("/") + 1, imageName.length());
                        mBuilder.setSmallIcon(R.drawable.ic_launcher);
                        mBuilder.setContentTitle("Failed sending picture" + mBuilder.setContentText(imageName));
                        mBuilder.setDeleteIntent(pendingIntent);
                        mBuilder.setContentText(mFailedSends + (mFailedSends == 1 ? " picture " : " pictures ") + " failed sending" + imageName);
                        imageFailedFileNames.add(imageName);
                        InboxStyle style = new InboxStyle().setSummaryText(mFailedSends + " failed to send");
                        for (String s : imageFailedFileNames) {
                            style.addLine(s);
                        }
                        mBuilder.setStyle(style);
                        mNotificationManager.notify(1, mBuilder.build());
                    }

                    @Override
                    public void onMailSucceeded(String imageName) {
                        //show notification
                        mSuccessfulSends++;
                        Date d = new Date();
                        FlurryAgent.logEvent("mail SUCCESS! " + d.toString());
                        Intent intent = new Intent(NOTIFICATION_DELETED_ACTION);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
                        imageName = imageName.substring(imageName.lastIndexOf("/") + 1, imageName.length());
                        mBuilder.setSmallIcon(R.drawable.ic_launcher);
                        mBuilder.setContentTitle(mSuccessfulSends + (mSuccessfulSends == 1 ? " picture " : " pictures ") + "sent successfully");
                        mBuilder.setContentText(imageName);
                        mBuilder.setDeleteIntent(pendingIntent);
                        imageFileNames.add(imageName);
                        InboxStyle style = new InboxStyle().setSummaryText(mSuccessfulSends + " sent");
                        for (String s : imageFileNames) {
                            style.addLine(s);
                        }
                        mBuilder.setStyle(style);

                        mNotificationManager.notify(0, mBuilder.build());
                    }
                });
                try {
                    mSender.sendMail(p.getDateTaken() + " " + p.getScaledImage() + " - via wifi",
                            p.getMapLink(),
                            context.getString(R.string.username) + "@yahoo.com",
                            PhotoLocationUtils.getEmailStringList(context), p.getFilePath(), p.getScaledImage());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            photoList.clear();
            PrefManager.savePhotoList(context, photoList);
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String ssid = wifiInfo.getSSID();
        }
    }

    public boolean isOnline(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnected());
    }
}