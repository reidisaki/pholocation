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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

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
//                if (!PhotoLocationUtils.isValidEmail(mEditEmail.getText())) {
//                    mErrorText.setVisibility(View.VISIBLE);
//                } else {
//                    mErrorText.setVisibility(View.GONE);
//                }
//                mEditEmail.setText(PhotoLocationUtils.getData(getActivity()).get(PhotoLocationUtils.EMAIL_KEY));
//                mEditLayout.setVisibility(View.VISIBLE);

                break;
            case R.id.shutter:
                if (PhotoLocationUtils.isValidEmail(mEditEmail.getText())) {
                    mCaptureView.takeAPicture(getActivity(), mEditEmail.getText().toString());
                    shutterShow();
                } else {
                    displayErrorMessage();
                }
                break;
            case R.id.save_btn:
                onSaveData();
                mEditLayout.setVisibility(View.GONE);
                mErrorText.setVisibility(View.GONE);
                mShutter.setVisibility(View.VISIBLE);
                mCaptureView.setAlpha(1f);
                break;
            default:
                onSaveData();
                mCaptureView.setAlpha(1f);
                mShutter.setVisibility(View.VISIBLE);
                mErrorText.setVisibility(View.GONE);
                mEditLayout.setVisibility(View.GONE);
                break;
        }
        if (v != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    private void displayErrorMessage() {
        mEditLayout.setVisibility(View.VISIBLE);
        mErrorText.setVisibility(View.VISIBLE);
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

    private void onSaveData() {
        //TODO: this is GOING AWAY
        Map<String, String> map = new HashMap<>();
        map.put(PhotoLocationUtils.EMAIL_KEY, mEditEmail.getText().toString());
//        PhotoLocationUtils.saveData()
    }

    @Override
    public void onPause() {
        super.onPause();
        onSaveData();
    }

    @Override
    public void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(getActivity());
    }

    @Override
    public void onMailFailed(final Exception e) {
        mMailListener.onMailFailed(e);
    }

    @Override
    public void onMailSucceeded() {
        mMailListener.onMailSucceeded();
    }
}

