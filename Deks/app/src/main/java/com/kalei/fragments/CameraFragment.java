package com.kalei.fragments;

import com.flurry.android.FlurryAgent;
import com.kalei.activities.MainActivity;
import com.kalei.interfaces.ICameraClickListener;
import com.kalei.interfaces.IMailListener;
import com.kalei.pholocation.R;
import com.kalei.utils.PhotoLocationUtils;
import com.kalei.views.CaptureView;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Created by risaki on 2/22/16.
 */
public class CameraFragment extends PhotoLocationFragment implements OnClickListener, IMailListener {
    private ImageView mSettingsImage, mShutter, mCameraSwitch;
    private CaptureView mCaptureView;
    private FrameLayout mShutterScreen;

    private Handler mHandler;
    public IMailListener mMailListener;
    public ICameraClickListener mCameraClickListener;
    public int numPicturesTaken = 1;
    private static int NUMBER_PICTURES_BEFORE_SHOWING_AD = 5;

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
        IMailListener listener = (IMailListener) this;
        mCaptureView.setOnMailListener(listener);
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mMailListener = (IMailListener) context;
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
                } else {
                    mCameraClickListener.onNoEmailSet();
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
    }

    @Override
    public void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(getActivity());
    }

    @Override
    public void onMailFailed(final Exception e, String imageName) {

        mMailListener.onMailFailed(e, imageName);
    }

    @Override
    public void onMailSucceeded(String imageName) {
        mMailListener.onMailSucceeded(imageName);
    }
}

