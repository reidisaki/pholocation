package com.kalei.views;

import com.flurry.android.FlurryAgent;
import com.kalei.activities.MainActivity;
import com.kalei.fragments.CameraFragment;
import com.kalei.interfaces.IPhotoTakenListener;
import com.kalei.managers.PrefManager;
import com.kalei.pholocation.R;
import com.kalei.utils.PhotoLocationUtils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Area;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
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
    public int mCurrentCameraId = CameraInfo.CAMERA_FACING_BACK;
    private GestureDetector mGestureDetector;
    private final long STALE_TIME_DELTA = 30000;
    private float mDist;
    private IPhotoTakenListener mPhotoTakenListener;

    public CaptureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        holder = getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mCurrentCameraId = MainActivity.currentCameraId;
        mGestureDetector = new GestureDetector(context, new GestureListener());
    }

    public void switchCamera() {

        if (mIsPreviewRunning) {
            mCamera.stopPreview();
        }
        //NB: if you don't release the current camera before switching, you app will crash
        mCamera.release();

        //swap the id of the camera to be used
        if (mCurrentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            Log.i("pl", "facing front");
            mCurrentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
            MainActivity.currentCameraId = mCurrentCameraId;
        } else {
            Log.i("pl", "facing back");
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
        Camera.Parameters params = mCamera.getParameters();
        if (PrefManager.getFlashOption(mContext)) {
            if (PhotoLocationUtils.hasFlash(params)) {
                params.setFlashMode(Parameters.FLASH_MODE_ON);
                Log.i("", "flash mode on surface Changed");
            }
        } else {
            Log.i("pl", "flash mode OFF surface Changed");
            params.setFlashMode(Parameters.FLASH_MODE_OFF);
        }
        List<Camera.Size> sizes = params.getSupportedPictureSizes();
        //Quality of sizes
        Log.i("pl", "image sizes: " + sizes.size() + "getting size: " + ((sizes.size() - Math.round(sizes.size() * .75f))));
        Camera.Size size = sizes.get((sizes.size() - Math.round(sizes.size() * .75f)));
//        Camera.Size size = sizes.get(sizes.size() - 1); smallest size
//        Log.i("pl", "width: " + size.width + " height: " + size.height);

        params.setPictureSize(size.width, size.height);
        mCamera.setParameters(params);
        calculateOrientation(size.width, size.height);
        setCameraDisplayOrientation(mCurrentCameraId, mCamera);
        previewCamera();
    }

    private void calculateOrientation(int width, int height) {
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

    public void setOnPhotoTakenListener(IPhotoTakenListener listener) {
        mPhotoTakenListener = listener;
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
                    parameters.setFlashMode(PrefManager.getFlashOption(mContext) ? Parameters.FLASH_MODE_ON : Parameters.FLASH_MODE_OFF);
                    mCamera.setParameters(parameters);
                } catch (RuntimeException e) {
                    FlurryAgent.logEvent("set parameters failed: " + e.getMessage());
                }
            }
            try {
                mCamera.autoFocus(mAutoFocusTakePictureCallback);
            } catch (RuntimeException e) {
                FlurryAgent.logEvent("auto focus failed: " + e.getMessage());
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
            Log.i("pl", "surface created error" + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
        mCamera.release();
    }

    private boolean hasTooMuchTimeElapsed(long timePhotoTaken) {
        boolean hasTooMuchTimeElapsed = false;

        Long currentTime = new Date().getTime();
        if (currentTime - timePhotoTaken > STALE_TIME_DELTA) {
            hasTooMuchTimeElapsed = true;
        }
        return hasTooMuchTimeElapsed;
    }

    private int getOrientationRotation(int mCameraRotation) {
        int degrees = 0;
        if (mCameraRotation == CameraFragment.ORIENTATION_PORTRAIT_NORMAL) {
            degrees = 90;
            Log.i("pl", "portait");
        }
        if (mCameraRotation == CameraFragment.ORIENTATION_LANDSCAPE_INVERTED) {
            Log.i("pl", "landscape inverted");
            degrees = 180;
        }
        if (mCameraRotation == CameraFragment.ORIENTATION_LANDSCAPE_NORMAL) {
            degrees = 0;
            Log.i("pl", "landscape ");
        }
        if (mCameraRotation == CameraFragment.ORIENTATION_PORTRAIT_INVERTED) {
            degrees = -180;
            Log.i("pl", "portait inverted");
        }

        return degrees;
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Get the pointer ID
        try {
            Camera.Parameters params = mCamera.getParameters();
            int action = event.getAction();

            if (event.getPointerCount() > 1) {
                // handle multi-touch events
                if (action == MotionEvent.ACTION_POINTER_DOWN) {
                    mDist = getFingerSpacing(event);
                } else if (action == MotionEvent.ACTION_MOVE && params.isZoomSupported()) {
                    mCamera.cancelAutoFocus();
                    handleZoom(event, params);
                }
            } else {
                // handle single touch events
                if (action == MotionEvent.ACTION_UP) {
                    handleFocus(event, params);
                }
            }
        } catch (NullPointerException e) {
            FlurryAgent.logEvent("null pointer exception" + e.getMessage());
        }
        mGestureDetector.onTouchEvent(event);
        return true;
    }

    private void handleZoom(MotionEvent event, Camera.Parameters params) {
        int maxZoom = params.getMaxZoom();
        int zoom = params.getZoom();
        float newDist = getFingerSpacing(event);
        if (newDist > mDist) {
            //zoom in
            if (zoom < maxZoom) {
                zoom++;
            }
        } else if (newDist < mDist) {
            //zoom out
            if (zoom > 0) {
                zoom--;
            }
        }
        mDist = newDist;
        params.setZoom(zoom);
        mCamera.setParameters(params);
    }

    public void handleFocus(MotionEvent event, Camera.Parameters params) {
        int pointerId = event.getPointerId(0);
        int pointerIndex = event.findPointerIndex(pointerId);
        // Get the pointer's current position
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);

        List<String> supportedFocusModes = params.getSupportedFocusModes();
        if (supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean b, Camera camera) {
                    // currently set to auto-focus on single touch
                }
            });
        }
    }

    /**
     * Determine the space between the first two fingers
     */
    private float getFingerSpacing(MotionEvent event) {
        // ...
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        //zoom event was too slow, need to make it 4 times as fast
        return (float) Math.sqrt(x * x + y * y) * 4;
    }

    public class SavePhotoTask extends AsyncTask<Integer, Void, Void> {
        String pictureFilePath, originalFilePath;
        byte[] data;

        public SavePhotoTask(String pictureFilePath, String originalFilePath, byte[] stuff) {
            this.pictureFilePath = pictureFilePath;
            this.originalFilePath = originalFilePath;
            this.data = stuff;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mPhotoTakenListener.onPhotoTaken(pictureFilePath, originalFilePath);
        }

        @Override
        protected Void doInBackground(Integer... orientation) {
            try {
                Bitmap bmpNew = BitmapFactory.decodeByteArray(this.data, 0, this.data.length);
                Matrix mtx = new Matrix();

                mtx.postRotate(getOrientationRotation(orientation[0]));
                // Rotating Bitmap
                bmpNew = Bitmap.createBitmap(bmpNew, 0, 0, bmpNew.getWidth(), bmpNew.getHeight(), mtx, true);
//                    bmpNew = Bitmap.createScaledBitmap(bmpNew, bmpNew.getWidth(), bmpNew.getHeight(), true);

                FileOutputStream fos = new FileOutputStream(pictureFilePath);

                bmpNew.compress(CompressFormat.JPEG, 25, fos); //this also writes to t he folder
                fos.close();

                ///beginning test
                if (PrefManager.getOriginalOnly(mContext)) {
                    Bitmap bmpOG = BitmapFactory.decodeByteArray(this.data, 0, this.data.length);
                    FileOutputStream fos_original = new FileOutputStream(originalFilePath);
                    Matrix mtx2 = new Matrix();
                    mtx2.postRotate(getOrientationRotation(orientation[0]));
                    bmpOG = Bitmap.createBitmap(bmpOG, 0, 0, bmpOG.getWidth(), bmpOG.getHeight(), mtx2, true);
//                    bmpOG.compress(CompressFormat.JPEG, 100, fos);
                    PhotoLocationUtils
                            .insertImage(mContext.getContentResolver(), bmpOG, originalFilePath, "picture"); //this willw rite to a specified directory
//                    fos_original.write(data);
//                    File f = new File(originalPicture.toString());
//                    f.delete();//delete temp saved in directory
                    fos_original.close();
                }
                //end text
            } catch (FileNotFoundException e) {
                Log.d("pl", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d("pl", "Error accessing file: " + e.getMessage());
            }
            Log.i("pl", "got location? " + (MainActivity.mLocation != null));
            //after 10 seconds create the photo object save it and call the service.
            //save photo object into cache to be sent later.
            return null;
        }

        @Override
        protected void onPostExecute(final Void aVoid) {
            super.onPostExecute(aVoid);
            mPhotoTakenListener.onPhotoProcessed(pictureFilePath, originalFilePath);
//            Handler handler = new Handler();
//            mTimePreviousPhotoTaken = mTimePhotoTaken;
//            if (MainActivity.mLocation == null || (hasTooMuchTimeElapsed(mTimePreviousPhotoTaken) && mTimePreviousPhotoTaken != mTimePhotoTaken)) {
//                handler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        Log.i("pl", "waited 10 seconds");
//                        savePhoto(pictureFilePath, originalFilePath);
//                        mContext.startService(getPhotoUploadIntent());
//                    }
//                }, 10000);
//            } else {
//            savePhoto(pictureFilePath, originalFilePath);
//                mContext.startService(getPhotoUploadIntent());
//            }
//            mTimePhotoTaken = new Date().getTime();
        }
    }

    /***
     * Take a picture and and convert it from bytes[] to Bitmap.
     */
    public void takeAPicture(final Context context) {
//        mCanTakePicture = false;
        final int mCameraRotation = CameraFragment.mOrientation;
        Parameters parameters = mCamera.getParameters();
        if (PrefManager.getFlashOption(mContext)) {
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(parameters);
            parameters.setFlashMode(Parameters.FLASH_MODE_ON);
            mCamera.setParameters(parameters);
            Log.i("pl", "flash mode on");
        } else {
            Log.i("pl", "flash mode off");
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(parameters);
        }
        mCamera.setParameters(parameters);

        Camera.PictureCallback mPictureCallback = new PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                mCamera.startPreview();
                final File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE, false);
                final File originalPicture = getOutputMediaFile(MEDIA_TYPE_IMAGE, true);
                if (pictureFile == null) {
                    Log.d("pl", "Error creating media file, check storage permissions: ");
                    return;
                }
                new SavePhotoTask(pictureFile.toString(), originalPicture.toString(), data).execute(mCameraRotation);
            }
        };
        try {

            mCamera.takePicture(null, null, mPictureCallback);
        } catch (RuntimeException e) {
            Log.i("pl", "clicked too fast" + e.getMessage());
        }
    }

    private class GestureListener extends SimpleOnGestureListener {
        @Override
        public boolean onDown(final MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(final MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(final MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(final MotionEvent e1, final MotionEvent e2, final float distanceX, final float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(final MotionEvent e) {

        }

        @Override
        public boolean onFling(final MotionEvent e1, final MotionEvent e2, final float velocityX, final float velocityY) {
            return false;
        }

        @Override
        public boolean onDoubleTap(final MotionEvent e) {
            if (mCamera != null) {
                Parameters params = mCamera.getParameters();
                params.setZoom(0);
                mCamera.setParameters(params);
            }
            return super.onDoubleTap(e);
        }
    }
    //might need this for patricks s5 memory error
//    public class RotateTask extends AsyncTask<Integer, Void, Bitmap> {
//        private WeakReference<Bitmap> rotateBitmap;
//        private WeakReference<Bitmap> original;
//        private String originalPath;
//
//        public RotateTask(Bitmap original, String originalPath) {
//            this.original = new WeakReference<Bitmap>(original);
//            this.originalPath = originalPath;
//        }
//
//        @Override
//        protected Bitmap doInBackground(Integer... params) {
//            Matrix matrix = new Matrix();
//            matrix.postRotate(params[0]);
//            rotateBitmap = new WeakReference<Bitmap>(Bitmap
//                    .createBitmap(original.get(), 0, 0, original.get().getWidth(), original.get().getHeight(), matrix, true));
//            return rotateBitmap.get();
//        }
//
//        @Override
//        protected void onPostExecute(Bitmap result) {
//
//            FileOutputStream fos_original = null;
//            try {
//                fos_original = new FileOutputStream(originalPath);
//                PhotoLocationUtils
//                        .insertImage(mContext.getContentResolver(), result, originalPath, "picture"); //this willw rite to a specified directory
//                fos_original.close();
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
}

