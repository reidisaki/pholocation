package com.kalei.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import com.android.ex.chips.RecipientEntry;
import com.flurry.android.FlurryAgent;
import com.kalei.interfaces.IMailListener;
import com.kalei.managers.PrefManager;
import com.kalei.models.Photo;
import com.kalei.pholocation.GMailSender;
import com.kalei.pholocation.R;
import com.kalei.receivers.WifiReceiver;
import com.kalei.views.CaptureView;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.InboxStyle;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by risaki on 2/20/16.
 */
public class PhotoLocationUtils {

    public static String EMAIL_KEY = "email_key";
    public static String MY_PREFS_NAME = "photolocation";
    public static int mSuccessfulSends = 0;
    public static int mFailedSends = 0;

    public static void saveDataObjects(List<RecipientEntry> chips, Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE).edit();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Uri.class, new UriSerializer())
                .create();

        editor.putString(EMAIL_KEY, gson.toJson(chips).toString());
        editor.commit();
    }

    public static List<RecipientEntry> getDataObjects(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        Type listType = new TypeToken<ArrayList<RecipientEntry>>() {
        }.getType();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Uri.class, new UriDeserializer())
                .create();
        List<RecipientEntry> list = gson.fromJson(prefs.getString(EMAIL_KEY, null), listType);
        if (list == null) {
            list = new ArrayList<>();
        }
        return list;
    }

    public static boolean isValidEmail(CharSequence target) {
        return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static String getEmailStringList(Context context) {
        StringBuilder sb = new StringBuilder();
        for (RecipientEntry r : getDataObjects(context)) {
            sb.append(r.getDestination() + ",");
        }
        String s = sb.toString();

        return s.length() > 0 ? s.substring(0, s.length() - 1) : s;
    }

    public static class UriSerializer implements JsonSerializer<Uri> {
        public JsonElement serialize(Uri src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }
    }

    public static class UriDeserializer implements JsonDeserializer<Uri> {
        @Override
        public Uri deserialize(final JsonElement src, final Type srcType,
                final JsonDeserializationContext context) throws JsonParseException {
            return Uri.parse(src.getAsString());
        }
    }

    /*
         * A copy of the Android internals  insertImage method, this method populates the
         * meta data with DATE_ADDED and DATE_TAKEN. This fixes a common problem where media
         * that is inserted manually gets saved at the end of the gallery (because date is not populated).
         * @see android.provider.MediaStore.Images.Media#insertImage(ContentResolver, Bitmap, String, String)
         */
    public static final void insertImage(ContentResolver cr,
            Bitmap source,
            String title,
            String description) {
        OutputStream fOut = null;
        File file = new File(title);

        try {
            fOut = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        source.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
        try {
            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ContentValues values = new ContentValues();
        values.put(Images.Media.TITLE, "test");
        values.put(Images.Media.DESCRIPTION, "description");
        values.put(Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(Images.ImageColumns.BUCKET_ID, file.toString().toLowerCase(Locale.US).hashCode());
        values.put(Images.ImageColumns.BUCKET_DISPLAY_NAME, file.getName().toLowerCase(Locale.US));
        values.put("_data", file.getAbsolutePath());

        cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
//        ContentValues values = new ContentValues();
//        values.put(Images.Media.TITLE, title);
//        values.put(Images.Media.DISPLAY_NAME, title);
//        values.put(Images.Media.DESCRIPTION, description);
//        values.put(Images.Media.MIME_TYPE, "image/jpeg");
//        // Add the date meta data to ensure the image is added at the front of the gallery
//        values.put(Images.Media.DATE_ADDED, System.currentTimeMillis());
//        values.put(Images.Media.DATE_TAKEN, System.currentTimeMillis());
//
//        Uri url = null;
//        String stringUrl = null;    /* value to be returned */
//
//        try {
//            url = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
//
//            if (source != null) {
//                OutputStream imageOut = cr.openOutputStream(url);
//                try {
//                    source.compress(Bitmap.CompressFormat.JPEG, 70, imageOut);
//                } finally {
//                    imageOut.close();
//                }
//
//                long id = ContentUris.parseId(url);
//                // Wait until MINI_KIND thumbnail is generated.
//                Bitmap miniThumb = Images.Thumbnails.getThumbnail(cr, id, Images.Thumbnails.MINI_KIND, null);
//                // This is for backward compatibility.
//                storeThumbnail(cr, miniThumb, id, 50F, 50F, Images.Thumbnails.MICRO_KIND);
//            } else {
//                cr.delete(url, null, null);
//                url = null;
//            }
//        } catch (Exception e) {
//            if (url != null) {
//                cr.delete(url, null, null);
//                url = null;
//            }
//        }
//
//        if (url != null) {
//            stringUrl = url.toString();
//        }
//
//        return stringUrl;
    }

    /**
     * A copy of the Android internals StoreThumbnail method, it used with the insertImage to populate the android.provider.MediaStore.Images.Media#insertImage
     * with all the correct meta data. The StoreThumbnail method is private so it must be duplicated here.
     *
     * @see android.provider.MediaStore.Images.Media (StoreThumbnail private method)
     */
    private static final Bitmap storeThumbnail(
            ContentResolver cr,
            Bitmap source,
            long id,
            float width,
            float height,
            int kind) {

        // create the matrix to scale it
        Matrix matrix = new Matrix();

        float scaleX = width / source.getWidth();
        float scaleY = height / source.getHeight();

        matrix.setScale(scaleX, scaleY);

        Bitmap thumb = Bitmap.createBitmap(source, 0, 0,
                source.getWidth(),
                source.getHeight(), matrix,
                true
        );

        ContentValues values = new ContentValues(4);
        values.put(Images.Thumbnails.KIND, kind);
        values.put(Images.Thumbnails.IMAGE_ID, (int) id);
        values.put(Images.Thumbnails.HEIGHT, thumb.getHeight());
        values.put(Images.Thumbnails.WIDTH, thumb.getWidth());

        Uri url = cr.insert(Images.Thumbnails.EXTERNAL_CONTENT_URI, values);

        try {
            OutputStream thumbOut = cr.openOutputStream(url);
            thumb.compress(Bitmap.CompressFormat.JPEG, 100, thumbOut);
            thumbOut.close();
            return thumb;
        } catch (FileNotFoundException ex) {
            return null;
        } catch (IOException ex) {
            return null;
        }
    }

    public static NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    /**
     * Check if there is any connectivity
     */
    public static boolean isConnected(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        return (info != null && info.isConnected());
    }

    /**
     * Check if there is any connectivity to a Wifi network
     */
    public static boolean isConnectedWifi(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI);
    }

    /**
     * Check if there is any connectivity to a mobile network
     */
    public static boolean isConnectedMobile(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE);
    }

    /**
     * Check if there is fast connectivity
     */
    public static boolean isConnectedFast(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        return (info != null && info.isConnected() && isConnectionFast(info.getType(), info.getSubtype()));
    }

    /**
     * Check if the connection is fast
     */
    public static boolean isConnectionFast(int type, int subType) {
        if (type == ConnectivityManager.TYPE_WIFI) {
            return true;
        } else if (type == ConnectivityManager.TYPE_MOBILE) {
            switch (subType) {
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    return false; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    return false; // ~ 14-64 kbps
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    return false; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    return true; // ~ 400-1000 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    return true; // ~ 600-1400 kbps
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    return false; // ~ 100 kbps
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    return true; // ~ 2-14 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPA:
                    return true; // ~ 700-1700 kbps
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    return true; // ~ 1-23 Mbps
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    return true; // ~ 400-7000 kbps
            /*
             * Above API level 7, make sure to set android:targetSdkVersion
             * to appropriate level to use these
             */
                case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11
                    return true; // ~ 1-2 Mbps
                case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
                    return true; // ~ 5 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
                    return true; // ~ 10-20 Mbps
                case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
                    return false; // ~25 kbps
                case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
                    return true; // ~ 10+ Mbps
                // Unknown
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                default:
                    return false;
            }
        } else {
            return false;
        }
    }

    public static boolean isOnlineAndFast(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        //if send wifi only then only use wifi
        //if not wifi, then only check if is fast internet
        return (netInfo != null && netInfo.isConnected() &&
                ((PhotoLocationUtils.isConnectedFast(context) && !PrefManager.getSendWifiOnly(context)) || PhotoLocationUtils.isConnectedWifi(context)));
    }

    public static void processEmailPicture(final Context context, Intent intent) {
        final NotificationCompat.Builder mBuilder;
        final NotificationManager mNotificationManager;
        final List<String> imageFileNames;
        final List<String> imageFailedFileNames;
        final String NOTIFICATION_DELETED_ACTION = "NOTIFICATION_DELETED";
        if (PhotoLocationUtils.isOnlineAndFast(context)) {
            mBuilder = new NotificationCompat.Builder(context);
            mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            imageFileNames = new ArrayList<>();
            imageFailedFileNames = new ArrayList<>();
//        String originalPicture, scaledImage;
//        originalPicture = intent.getStringExtra(CaptureView.ORIGINAL_PICTURE_KEY);
//        scaledImage = intent.getStringExtra(CaptureView.SCALED_PICTURE_KEY);
            List<Photo> photoList = PrefManager.getPhotoList(context);

            for (Photo p : photoList) {
                p.setMapLink(getMapLink(intent.getExtras().getDouble(CaptureView.LATTITUDE), intent.getExtras().getDouble(CaptureView.LONGITUDE), context));
                GMailSender mSender = new GMailSender(context.getString(R.string.username), context.getString(R.string.password), new IMailListener() {
                    @Override
                    public void onMailFailed(final Exception e, String imageName) {
                        FlurryAgent.logEvent("Mail failed: " + e.getMessage());
                        mFailedSends++;
                        Intent intent = new Intent(WifiReceiver.NOTIFICATION_DELETED_ACTION);
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
                    mSender.sendMail(p.getDateTaken() + " " + p.getScaledImage(),
                            p.getMapLink(),
                            context.getString(R.string.username) + "@yahoo.com",
                            PhotoLocationUtils.getEmailStringList(context), p.getFilePath(), p.getScaledImage());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            photoList.clear();
            PrefManager.savePhotoList(context, photoList);
        } else {
            Log.i("Reid", "not sending");
        }
        mSuccessfulSends = 0;
        mFailedSends = 0;
    }

    private static String getMapLink(double lattitude, double longitude, Context context) {
        String mapLink = "Could not connect to internet";
        if (!PhotoLocationUtils.isConnected(context)) {
            return mapLink;
        }

        if (lattitude == 0) {
            mapLink = "COULD NOT get location SORRY!, and didn't want to wait any longer. Is GPS enabled? \n\n\n\n\n\n\n -sent by PhotoLocation, download the app here: https://play.google.com/store/apps/details?id=com.kalei.pholocation";
        } else {
            String add = "";
            try {

                Geocoder geoCoder = new Geocoder(context, Locale.getDefault());
                List<Address> addresses = geoCoder.getFromLocation(lattitude, longitude, 1);

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

            mapLink = "http://maps.google.com/?q=" + lattitude + "," + lattitude +
                    "\n\n" + add +
                    "\n\n\n\n\n -sent by PhotoLocation, download the app here: https://play.google.com/store/apps/details?id=com.kalei.pholocation ";
        }
        return mapLink;
    }
}
