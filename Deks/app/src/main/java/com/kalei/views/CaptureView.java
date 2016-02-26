package com.kalei.views;

import com.flurry.android.FlurryAgent;
import com.kalei.activities.MainActivity;
import com.kalei.interfaces.IMailListener;
import com.kalei.pholocation.PhotoLocationSender;
import com.kalei.pholocation.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Area;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CaptureView extends SurfaceView implements SurfaceHolder.Callback {

    SurfaceHolder holder;
    static Camera mCamera;
    static Context mContext;
    static boolean mIsPreviewRunning;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private static final int FOCUS_AREA_SIZE = 300;
    public IMailListener mMailListener;
    public int mCurrentCameraId = CameraInfo.CAMERA_FACING_BACK;

    public CaptureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        holder = getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mCurrentCameraId = MainActivity.currentCameraId;
    }

    public void setOnMailListener(IMailListener listener) {
        mMailListener = listener;
    }

    public void switchCamera() {

        if (mIsPreviewRunning) {
            mCamera.stopPreview();
        }
        //NB: if you don't release the current camera before switching, you app will crash
        mCamera.release();

        //swap the id of the camera to be used
        if (mCurrentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            Log.i("Reid", "facing front");
            mCurrentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
            MainActivity.currentCameraId = mCurrentCameraId;
        } else {
            Log.i("Reid", "facing back");
            mCurrentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
            MainActivity.currentCameraId = mCurrentCameraId;
        }
        mCamera = Camera.open(mCurrentCameraId);
        //Code snippet for this method from somewhere on android developers, i forget where
        setCameraDisplayOrientation(mCurrentCameraId, mCamera);
        try {
            //this step is critical or preview on new camera will no know where to render to
            mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mCamera == null) {
            return;
        }
        if (mIsPreviewRunning) {
            mCamera.stopPreview();
        }
        calculateOrientation(width, height);
        setCameraDisplayOrientation(mCurrentCameraId, mCamera);
        previewCamera();
    }

    private void calculateOrientation(int width, int height) {
        Log.i("Reid", "curren cameraid 0 is back: " + mCurrentCameraId);
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

                try {
                    mCamera.setParameters(parameters);
                } catch (RuntimeException e) {
                    FlurryAgent.logEvent("set parameters failed: " + e.getMessage());
                }
            }
            mCamera.autoFocus(mAutoFocusTakePictureCallback);
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
                Log.i("tap_to_focus", "success!");
            } else {
                Log.i("tap_to_focus", "fail!");
            }
        }
    };

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        try {
            releaseCameraAndPreview();
            mCamera = Camera.open(mCurrentCameraId);
            mCamera.setPreviewDisplay(holder);
        } catch (Exception e) {
            Log.i("Reid", "surface created error" + e.getMessage());
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
    public void takeAPicture(final Context context) {
        Camera.PictureCallback mPictureCallback = new PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                mCamera.startPreview();
                File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE, false);
                File originalPicture = getOutputMediaFile(MEDIA_TYPE_IMAGE, true);
                if (pictureFile == null) {
                    Log.d("Reid", "Error creating media file, check storage permissions: ");
                    return;
                }

                try {
                    FileOutputStream fos = new FileOutputStream(pictureFile);

                    fos.write(data);
                    fos.close();

                    FileOutputStream fos_original = new FileOutputStream(originalPicture);
                    fos_original.write(data);
                    fos_original.close();
                } catch (FileNotFoundException e) {
                    Log.d("Reid", "File not found: " + e.getMessage());
                } catch (IOException e) {
                    Log.d("Reid", "Error accessing file: " + e.getMessage());
                }

//                mPictureURI = Uri.fromFile(pictureFile);

                new PhotoLocationSender(context, pictureFile.toString(), mMailListener);
            }
        };
        try {
            mCamera.takePicture(null, null, mPictureCallback);
        } catch (RuntimeException e) {
            Log.i("Reid", "clicked too fast");
        }
    }

    /**
     * Create a File for saving an image or video
     */
    private static File getOutputMediaFile(int type, boolean isOriginal) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), mContext.getString(R.string.app_name));
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
            if (isOriginal) {
                mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                        "IMG_" + timeStamp + "_o.jpg");
            } else {
                mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                        "IMG_" + timeStamp + ".jpg");
            }
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    private void releaseCameraAndPreview() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    public static void setCameraDisplayOrientation(
            int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = ((Activity) mContext).getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        Log.i("Reid", "rotated screen");
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }
}
