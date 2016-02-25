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
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by risaki on 2/22/16.
 */
public class CameraFragment extends PhotoLocationFragment implements OnClickListener, IMailListener {
    private ImageView mSettingsImage, mShutter;
    private EditText mEditEmail;
    private CaptureView mCaptureView;
    private FrameLayout mShutterScreen;
    private LinearLayout mEditLayout;
    private Button mSaveButton;

    private TextView mErrorText;

    private Handler mHandler;
    public IMailListener mMailListener;
    public ICameraClickListener mCameraClickListener;

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
        mEditEmail = (EditText) rootView.findViewById(R.id.email_edit);
        mShutter = (ImageView) rootView.findViewById(R.id.shutter);
        mShutter.setOnClickListener(this);
        mCaptureView = (CaptureView) rootView.findViewById(R.id.capture_view);
        mCaptureView.setOnClickListener(this);
        mEditEmail.setText("testing this is gonna be deleted TODO reid");
        mShutterScreen = (FrameLayout) rootView.findViewById(R.id.shutterScreen);
        mEditLayout = (LinearLayout) rootView.findViewById(R.id.edit_layout);
        mErrorText = (TextView) rootView.findViewById(R.id.error_message_text);
        mSaveButton = (Button) rootView.findViewById(R.id.save_btn);
        mSaveButton.setOnClickListener(this);
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
                if (PhotoLocationUtils.getEmailStringList(getContext()).length() > 0) {
                    mCaptureView.takeAPicture(getActivity(), mEditEmail.getText().toString());
                    shutterShow();
                } else {
                    mCameraClickListener.onNoEmailSet();
                }
                break;
            default:
                mCaptureView.setAlpha(1f);
                mShutter.setVisibility(View.VISIBLE);
                mErrorText.setVisibility(View.GONE);
                mEditLayout.setVisibility(View.GONE);
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

