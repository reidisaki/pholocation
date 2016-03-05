package com.kalei.fragments;

import com.flurry.android.FlurryAgent;
import com.kalei.activities.MainActivity;
import com.kalei.interfaces.ICameraClickListener;
import com.kalei.pholocation.R;
import com.kalei.utils.PhotoLocationUtils;
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
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Created by risaki on 2/22/16.
 */
public class CameraFragment extends PhotoLocationFragment implements OnClickListener {
    private ImageView mSettingsImage, mShutter, mCameraSwitch;
    private CaptureView mCaptureView;
    private FrameLayout mShutterScreen;

    private Handler mHandler;
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

        ((MainActivity) getActivity()).getSupportActionBar().hide();
        View rootView = inflater.inflate(R.layout.fragment_camera, container, false);
        mSettingsImage = (ImageView) rootView.findViewById(R.id.settings_image);
        mSettingsImage.setOnClickListener(this);
        mShutter = (ImageView) rootView.findViewById(R.id.shutter);
        mShutter.setOnClickListener(this);
        mCaptureView = (CaptureView) rootView.findViewById(R.id.capture_view);
        mCaptureView.setOnClickListener(this);
        mCameraSwitch = (ImageView) rootView.findViewById(R.id.camera_switch);
        mCameraSwitch.setOnClickListener(this);
        mShutterScreen = (FrameLayout) rootView.findViewById(R.id.shutterScreen);
        mHandler = new Handler();

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

                mCameraClickListener.onSettingsClicked();
                break;
            case R.id.shutter:
                numPicturesTaken++;
                if (numPicturesTaken % NUMBER_PICTURES_BEFORE_SHOWING_AD == 0) {
                    ((MainActivity) getActivity()).requestNewInterstitial();
                    numPicturesTaken = 1;
                }
                if (PhotoLocationUtils.getEmailStringList(getContext()).length() > 0) {
                    mCaptureView.takeAPicture(getActivity());
                    shutterShow();
                }
                break;
            case R.id.camera_switch:
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

    @Override
    public void onPause() {
        super.onPause();
        mOrientationEventListener.disable();
    }

    @Override
    public void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(getActivity());
    }

    private void changeRotation(int orientation, int lastOrientation) {
        switch (orientation) {
            case ORIENTATION_PORTRAIT_NORMAL:
                rotateIcons(mCameraSwitch, 0);
//                Log.v("Reid", "Orientation = 90");
                break;
            case ORIENTATION_LANDSCAPE_NORMAL:
                rotateIcons(mCameraSwitch, 90);
//                Log.v("Reid", "Orientation = 0");
                break;
            case ORIENTATION_PORTRAIT_INVERTED:
                rotateIcons(mCameraSwitch, 180);
//                Log.v("Reid", "Orientation = 270");
                break;
            case ORIENTATION_LANDSCAPE_INVERTED:
                rotateIcons(mCameraSwitch, -90);
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

    public void rotateIcons(final ImageView image, final int rotation) {

        if (!mIsAnimating) {
            RotateAnimation r; // = new RotateAnimation(ROTATE_FROM, ROTATE_TO);
            r = new RotateAnimation(0.0f, rotation, RotateAnimation.RELATIVE_TO_SELF, .5f, RotateAnimation.RELATIVE_TO_SELF, .5f);
            r.setDuration(750);
            r.setRepeatCount(0);
            image.startAnimation(r);
            r.setAnimationListener(new AnimationListener() {
                @Override
                public void onAnimationStart(final Animation animation) {

                }

                @Override
                public void onAnimationEnd(final Animation animation) {
                    mIsAnimating = false;
                    image.setRotation(rotation);
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
}

