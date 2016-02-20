package com.kalei.views;

import android.Manifest.permission;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Area;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.text.Html;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CaptureView extends SurfaceView implements SurfaceHolder.Callback {

    public static Bitmap mBitmap;
    private static String mEmail;
    SurfaceHolder holder;
    static Camera mCamera;
    static Context mContext;
    static boolean mIsPreviewRunning;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private static final int FOCUS_AREA_SIZE = 300;
    private LocationManager mLocationManager;
    private static int LOCATION_REFRESH_TIME = 5000;
    private static int LOCATION_REFRESH_DISTANCE = 50;
    private final LocationListener mLocationListener;
    private static Location mLocation;
    private static boolean mHasLocation;
    private static boolean mHasPicture;
    private static Uri mPictureURI;
    private static boolean mIsSent;
    private static String mMapLink;

    public CaptureView(Context context, AttributeSet attrs) {
        super(context, attrs);

        holder = getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mContext = context;
        mHasLocation = false;
        mIsSent = false;
        mHasPicture = false;
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        mLocationListener = new LocationListener() {

            @Override
            public void onLocationChanged(final Location location) {
                //your code here
                mHasLocation = true;
                mLocation = location;
                Geocoder geoCoder = new Geocoder(mContext, Locale.getDefault());
                String mapLink = "";

                try {
                    if (mLocation != null) {
                        List<Address> addresses = geoCoder.getFromLocation(mLocation.getLatitude(), mLocation.getLongitude(), 1);

                        String add = "";
                        if (addresses.size() > 0) {
                            for (int i = 0; i < addresses.get(0).getMaxAddressLineIndex(); i++) {
                                add += addresses.get(0).getAddressLine(i) + "\n";
                            }
                        }

                        mMapLink = "http://maps.google.com/?q=" + add;
//                        mMapLink = "https://maps.googleapis.com/maps/api/staticmap?center=" + add +
//                                "i&zoom=17&size=600x300&maptype=roadmap&markers=color:red%7Clabel:X%7C" +
//                                mLocation.getLatitude() + "," + mLocation.getLongitude() + "&key=AIzaSyBryuOc-tskt2bkYh_vxfYq_HVRW5ddjoI";
                        Log.i("Reid", mapLink);
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                if (mHasPicture && !mIsSent) {
                    sendPicture(mMapLink);
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
        };
        if (ActivityCompat.checkSelfPermission(mContext, permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext, permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)

        {
            return;
        }

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                LOCATION_REFRESH_DISTANCE, mLocationListener);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mIsPreviewRunning) {
            mCamera.stopPreview();
        }

        Parameters parameters = mCamera.getParameters();
        Display display = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        if (display.getRotation() == Surface.ROTATION_0) {
            parameters.setPreviewSize(height, width);
            mCamera.setDisplayOrientation(90);
        }

        if (display.getRotation() == Surface.ROTATION_90) {
            parameters.setPreviewSize(width, height);
        }

        if (display.getRotation() == Surface.ROTATION_180) {
            parameters.setPreviewSize(height, width);
        }

        if (display.getRotation() == Surface.ROTATION_270) {
            parameters.setPreviewSize(width, height);
            mCamera.setDisplayOrientation(180);
        }
        previewCamera();
    }

    public void previewCamera() {
        try {

            this.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        focusOnTouch(event);
                    }
                    return false;
                }
            });
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
            mIsPreviewRunning = true;
        } catch (Exception e) {
//            Log.d(APP_CLASS, "Cannot start preview", e);
        }
    }

    private void focusOnTouch(MotionEvent event) {
        if (mCamera != null) {

            Camera.Parameters parameters = mCamera.getParameters();
            if (parameters.getMaxNumMeteringAreas() > 0) {

                Rect rect = calculateFocusArea(event.getX(), event.getY());

                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                List<Area> meteringAreas = new ArrayList<Area>();
                meteringAreas.add(new Camera.Area(rect, 800));
                parameters.setFocusAreas(meteringAreas);

                mCamera.setParameters(parameters);
                mCamera.autoFocus(mAutoFocusTakePictureCallback);
            } else {
                mCamera.autoFocus(mAutoFocusTakePictureCallback);
            }
        }
    }

    private Rect calculateFocusArea(float x, float y) {
        int left = clamp(Float.valueOf((x / this.getWidth()) * 2000 - 1000).intValue(), FOCUS_AREA_SIZE);
        int top = clamp(Float.valueOf((y / this.getHeight()) * 2000 - 1000).intValue(), FOCUS_AREA_SIZE);

        return new Rect(left, top, left + FOCUS_AREA_SIZE, top + FOCUS_AREA_SIZE);
    }

    private int clamp(int touchCoordinateInCameraReper, int focusAreaSize) {
        int result;
        if (Math.abs(touchCoordinateInCameraReper) + focusAreaSize / 2 > 1000) {
            if (touchCoordinateInCameraReper > 0) {
                result = 1000 - focusAreaSize / 2;
            } else {
                result = -1000 + focusAreaSize / 2;
            }
        } else {
            result = touchCoordinateInCameraReper - focusAreaSize / 2;
        }
        return result;
    }
// implement this callback to trigger the focus.

    private Camera.AutoFocusCallback mAutoFocusTakePictureCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if (success) {
                // do something...
                Log.i("tap_to_focus", "success!");
            } else {
                // do something...
                Log.i("tap_to_focus", "fail!");
            }
        }
    };

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        try {
            mCamera = Camera.open();
            mCamera.setPreviewDisplay(holder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
        mCamera.release();
    }

    /***
     * Take a picture and and convert it from bytes[] to Bitmap.
     */
    public static void takeAPicture(final Context context, String email) {
        mEmail = email;
        Camera.PictureCallback mPictureCallback = new PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                mHasPicture = true;
                mCamera.startPreview();
                File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                if (pictureFile == null) {
                    Log.d("REid", "Error creating media file, check storage permissions: ");
                    return;
                }

                try {
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(data);
                    fos.close();
                } catch (FileNotFoundException e) {
                    Log.d("Reid", "File not found: " + e.getMessage());
                } catch (IOException e) {
                    Log.d("Reid", "Error accessing file: " + e.getMessage());
                }

                mPictureURI = Uri.fromFile(pictureFile);
                if (mHasLocation) {
                    sendPicture(mMapLink);
                } else {
                    //show loading image now
                    Toast.makeText(mContext, "Trying to get location hang on a sec..", Toast.LENGTH_SHORT).show();
                }
            }
        };
        mCamera.takePicture(null, null, mPictureCallback);
    }

    private static void sendPicture(String mapLink) {
        mIsSent = true;
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setType("image/jpg");

        i.putExtra(Intent.EXTRA_EMAIL, new String[]{mEmail});
        i.putExtra(Intent.EXTRA_SUBJECT, "");
        i.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(String.format("<a href=\"%s\">Map location</a>", mapLink)));

        i.putExtra(Intent.EXTRA_STREAM, mPictureURI);
        mContext.startActivity(i);
    }

    public static File writebitmaptofilefirst(String filename, Bitmap bitmap) {
        File extStorageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        FileOutputStream out = null;

        File mFolder = new File(extStorageDirectory + "/temp_images");
        if (!mFolder.exists()) {
            mFolder.mkdir();
        }
        try {
            out = new FileOutputStream(filename);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File file = new File(extStorageDirectory, filename + ".png");

        return file;
    }

    private static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * Create a File for saving an image or video
     */
    private static File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }
}
