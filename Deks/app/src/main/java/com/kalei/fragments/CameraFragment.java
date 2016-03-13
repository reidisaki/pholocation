package com.kalei.fragments;

import com.kalei.activities.MainActivity;
import com.kalei.interfaces.ICameraClickListener;
import com.kalei.interfaces.IPhotoTakenListener;
import com.kalei.managers.PrefManager;
import com.kalei.pholocation.R;
import com.kalei.utils.PhotoLocationUtils;
import com.kalei.views.CameraPreview;
import com.kalei.views.CaptureView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * Created by risaki on 2/22/16.
 */
public class CameraFragment extends PhotoLocationFragment implements OnClickListener, IPhotoTakenListener {
    private ImageView mSettingsImage, mShutter, mCameraSwitch, mFlash;
    private CaptureView mCaptureView;
    private CameraPreview mCameraPreview;
    private FrameLayout mShutterScreen, mSurfaceFrame, mPreviewPane;
    private RelativeLayout mCameraControls;
    private Handler mHandler;
    public boolean shouldBackOutofApp = true;
    public ICameraClickListener mCameraClickListener;
    public int numPicturesTaken = 1;
    private static int NUMBER_PICTURES_BEFORE_SHOWING_AD = 5;
    private OrientationEventListener mOrientationEventListener;
    public static int mOrientation = -1;

    private boolean mIsAnimating = false;
    public static final int ORIENTATION_PORTRAIT_NORMAL = 1;
    public static final int ORIENTATION_PORTRAIT_INVERTED = 2;
    public static final int ORIENTATION_LANDSCAPE_NORMAL = 3;
    public static final int ORIENTATION_LANDSCAPE_INVERTED = 4;

    public static CameraFragment newInstance() {
        CameraFragment fragment = new CameraFragment();
        Bundle bundle = new Bundle();
//        bundle.putInt("question", questionNumber);
//        bundle.putBoolean("isCorrect", isCorrect);

        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

        ((MainActivity) getActivity()).getActionBar().hide();
        View rootView = inflater.inflate(R.layout.fragment_camera, container, false);
        mSettingsImage = (ImageView) rootView.findViewById(R.id.settings_image);
        mSettingsImage.setOnClickListener(this);
        mShutter = (ImageView) rootView.findViewById(R.id.shutter);
        mShutter.setOnClickListener(this);
        mSurfaceFrame = (FrameLayout) rootView.findViewById(R.id.capture_view_frame);
        mPreviewPane = (FrameLayout) rootView.findViewById(R.id.camera_preview_frame);

        mCaptureView = (CaptureView) rootView.findViewById(R.id.capture_view);
        mCaptureView.setOnClickListener(this);
        mCaptureView.setOnPhotoTakenListener(this);

        mCameraPreview = (CameraPreview) rootView.findViewById(R.id.camera_preview);
        mCameraPreview.setOnPhotoTakenListener(this);

        mCameraSwitch = (ImageView) rootView.findViewById(R.id.camera_switch);
        mCameraSwitch.setOnClickListener(this);
        mShutterScreen = (FrameLayout) rootView.findViewById(R.id.shutterScreen);
        mHandler = new Handler();
        mFlash = (ImageView) rootView.findViewById(R.id.flash);
        mFlash.setOnClickListener(this);
        mFlash.setImageDrawable(getResources().getDrawable(PrefManager.getFlashOption(getActivity()) ? R.drawable.flash : R.drawable.flash_off));

        mCameraControls = (RelativeLayout) rootView.findViewById(R.id.camera_controls_relative);
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCameraClickListener = (ICameraClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement the correct listeners");
        }
    }

    @Override
    public void onClick(final View v) {

        switch (v.getId()) {
            case R.id.settings_image:
                shouldBackOutofApp = false;
                mCameraClickListener.onSettingsClicked();
                break;
            case R.id.shutter:
                shouldBackOutofApp = false;
                numPicturesTaken++;
                if (numPicturesTaken % NUMBER_PICTURES_BEFORE_SHOWING_AD == 0) {
                    ((MainActivity) getActivity()).requestNewInterstitial();
                    numPicturesTaken = 1;
                }
                if (PhotoLocationUtils.getEmailStringList(getContext()).length() > 0) {
                    mCaptureView.takeAPicture(getActivity());
                    shutterShow();
                }

//                        mCameraControls.setVisibility(View.GONE);
                break;
            case R.id.flash:
                if (PrefManager.getFlashOption(getActivity())) {
                    mFlash.setImageDrawable(getResources().getDrawable(R.drawable.flash_off));
                    PrefManager.setFlashOption(getActivity(), false);
                } else {
                    mFlash.setImageDrawable(getResources().getDrawable(R.drawable.flash));
                    PrefManager.setFlashOption(getActivity(), true);
                }
                break;
            case R.id.camera_switch:
                shouldBackOutofApp = true;
                mCaptureView.switchCamera();
                break;
            default:
                mCaptureView.setAlpha(1f);
                mShutter.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void shutterShow() {
        mShutterScreen.setVisibility(View.VISIBLE);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mShutterScreen.setVisibility(View.GONE);
            }
        }, 300);
    }

    private void changeRotation(int orientation, int lastOrientation) {
        switch (orientation) {
            case ORIENTATION_PORTRAIT_NORMAL:
                rotateIcons(mCameraSwitch, 90, 0, true, 0);
//                Log.v("Reid", "Orientation = 90");
                Log.i("Reid", "portrait normal 90");
                break;
            case ORIENTATION_LANDSCAPE_NORMAL:
                rotateIcons(mCameraSwitch, 0, 90, false, 90);
                Log.i("Reid", "landscape normal");
//                Log.v("Reid", "Orientation = 0");
                break;
            case ORIENTATION_PORTRAIT_INVERTED:
                rotateIcons(mCameraSwitch, 0, 90, false, 180);
//                Log.v("Reid", "Orientation = 270");
                Log.i("Reid", "portait inverted");
                break;
            case ORIENTATION_LANDSCAPE_INVERTED:
                rotateIcons(mCameraSwitch, 0, 90, false, 270);
                Log.i("Reid", "landscape inverted");
//                Log.v("Reid", "Orientation = 180");
                break;
        }
    }

    /**
     * Rotates given Drawable
     *
     * @param drawableId Drawable Id to rotate
     * @param degrees    Rotate drawable by Degrees
     *
     * @return Rotated Drawable
     */
    private Drawable getRotatedImage(int drawableId, int degrees) {
        Bitmap original = BitmapFactory.decodeResource(getResources(), drawableId);
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);

        Bitmap rotated = Bitmap.createBitmap(original, 0, 0, original.getWidth(), original.getHeight(), matrix, true);
        return new BitmapDrawable(rotated);
    }

    public void rotateIcons(final ImageView image, final int fromRotation, final int rotation, final boolean isReverse, final float finalDestination) {

        if (!mIsAnimating) {
            RotateAnimation r; // = new RotateAnimation(ROTATE_FROM, ROTATE_TO);
            r = new RotateAnimation(fromRotation, rotation, RotateAnimation.RELATIVE_TO_SELF, .5f, RotateAnimation.RELATIVE_TO_SELF, .5f);
            r.setDuration(750);
            r.setRepeatCount(0);
            image.startAnimation(r);
            r.setAnimationListener(new AnimationListener() {
                @Override
                public void onAnimationStart(final Animation animation) {
                    if (isReverse) {
                        image.setRotation(finalDestination);
                    }
                }

                @Override
                public void onAnimationEnd(final Animation animation) {
                    mIsAnimating = false;
                    if (!isReverse) {
                        image.setRotation(finalDestination);
                    }
                }

                @Override
                public void onAnimationRepeat(final Animation animation) {

                }
            });
            mIsAnimating = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mOrientationEventListener == null) {

            mOrientationEventListener = new OrientationEventListener(getActivity(), SensorManager.SENSOR_DELAY_UI) {

                public void onOrientationChanged(int orientation) {
                    // determine our orientation based on sensor response
                    int lastOrientation = mOrientation;

                    if (orientation >= 315 || orientation < 45) {
                        if (mOrientation != ORIENTATION_PORTRAIT_NORMAL) {
                            mOrientation = ORIENTATION_PORTRAIT_NORMAL;
                        }
                    } else if (orientation < 315 && orientation >= 225) {
                        if (mOrientation != ORIENTATION_LANDSCAPE_NORMAL) {
                            mOrientation = ORIENTATION_LANDSCAPE_NORMAL;
                        }
                    } else if (orientation < 225 && orientation >= 135) {
                        if (mOrientation != ORIENTATION_PORTRAIT_INVERTED) {
                            mOrientation = ORIENTATION_PORTRAIT_INVERTED;
                        }
                    } else { // orientation <135 && orientation > 45
                        if (mOrientation != ORIENTATION_LANDSCAPE_INVERTED) {
                            mOrientation = ORIENTATION_LANDSCAPE_INVERTED;
                        }
                    }

                    if (lastOrientation != mOrientation) {
                        changeRotation(mOrientation, lastOrientation);
                    }
                }
            };
        }
        if (mOrientationEventListener.canDetectOrientation()) {
            mOrientationEventListener.enable();
        }
    }

    @Override
    public void onPhotoConfirm() {
        Log.i("pl", "photo confirmed");
        showCamera();
        Toast.makeText(getActivity(), "Success", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPhotoCancel() {
        Log.i("pl", "photo cancel");
        showCamera();
    }

    public void showCamera() {
        shouldBackOutofApp = true;
        mCameraPreview.mCaptionText.setText("");
        //deletes saved file.
//        mCameraPreview.cleanUp();
        mSurfaceFrame.setVisibility(View.VISIBLE);
        mPreviewPane.setVisibility(View.GONE);

        Animation animation = new TranslateAnimation(0, 0, 500, 0);
        animation.setDuration(500);
        animation.setFillAfter(true);
        animation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(final Animation animation) {
                mCameraControls.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(final Animation animation) {

            }

            @Override
            public void onAnimationRepeat(final Animation animation) {

            }
        });
        mCameraControls.startAnimation(animation);
    }

    @Override
    public void onPhotoTaken(String scaledImage, String originalImage) {
        Animation animation = new TranslateAnimation(0, 0, 0, 500);
        animation.setDuration(500);
        animation.setFillAfter(true);
        animation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(final Animation animation) {

            }

            @Override
            public void onAnimationEnd(final Animation animation) {
                mCameraControls.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(final Animation animation) {

            }
        });
        mCameraControls.startAnimation(animation);

        mSurfaceFrame.setVisibility(View.GONE);
        mPreviewPane.setVisibility(View.VISIBLE);
        mCameraPreview.setVisibility(R.id.progress, View.VISIBLE);
        mCameraPreview.setVisibility(R.id.imageView, View.GONE);

        Log.i("pl", "photo taken");
    }

    @Override
    public void onPhotoProcessed(String scaledImagePath, String originalImagePath) {
        Log.i("pl", "Photo ready to be processed");
        mCameraPreview.setImagePathsAndImageView(scaledImagePath, originalImagePath);
        mCameraPreview.setVisibility(R.id.progress, View.GONE);
        mCameraPreview.setVisibility(R.id.imageView, View.VISIBLE);
    }
}

