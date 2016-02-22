package com.kalei.fragments;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;

import com.flurry.android.FlurryAgent;
import com.kalei.IMailListener;
import com.kalei.pholocation.R;
import com.kalei.utils.PhotoLocationUtils;
import com.kalei.views.CaptureView;

import android.Manifest.permission;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
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
public class CameraFragment extends PhotoLocationFragment implements OnClickListener, ConnectionCallbacks, OnConnectionFailedListener, IMailListener {
    private ImageView mSettingsImage, mShutter;
    private EditText mEditEmail;
    private CaptureView mCaptureView;
    private FrameLayout mShutterScreen;
    private LinearLayout mEditLayout;
    private Button mSaveButton;

    private TextView mErrorText;

    private Handler mHandler;
    public GoogleApiClient mGoogleApiClient;
    public static Location mLocation;
    public IMailListener mMailListener;

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
        View rootView = inflater.inflate(R.layout.fragment_camera, container, false);
        mSettingsImage = (ImageView) rootView.findViewById(R.id.settings_image);
        mSettingsImage.setOnClickListener(this);
        mEditEmail = (EditText) rootView.findViewById(R.id.email_edit);
        mShutter = (ImageView) rootView.findViewById(R.id.shutter);
        mShutter.setOnClickListener(this);
        mCaptureView = (CaptureView) rootView.findViewById(R.id.capture_view);
        mCaptureView.setOnClickListener(this);
        mEditEmail.setText(PhotoLocationUtils.getData(getActivity()).get(PhotoLocationUtils.EMAIL_KEY));
        mShutterScreen = (FrameLayout) rootView.findViewById(R.id.shutterScreen);
        mEditLayout = (LinearLayout) rootView.findViewById(R.id.edit_layout);
        mErrorText = (TextView) rootView.findViewById(R.id.error_message_text);
        mSaveButton = (Button) rootView.findViewById(R.id.save_btn);
        mSaveButton.setOnClickListener(this);
        mHandler = new Handler();
        checkLocation();
        IMailListener listener = (IMailListener) this;
        mCaptureView.setOnMailListener(listener);
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mMailListener = (IMailListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement the IMailListener");
        }
    }

    private void checkLocation() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();
    }

    @Override
    public void onClick(final View v) {

        switch (v.getId()) {
            case R.id.settings_image:
                if (!PhotoLocationUtils.isValidEmail(mEditEmail.getText())) {
                    mErrorText.setVisibility(View.VISIBLE);
                } else {
                    mErrorText.setVisibility(View.GONE);
                }
                mEditEmail.setText(PhotoLocationUtils.getData(getActivity()).get(PhotoLocationUtils.EMAIL_KEY));
                mEditLayout.setVisibility(View.VISIBLE);

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
        Map<String, String> map = new HashMap<>();
        map.put(PhotoLocationUtils.EMAIL_KEY, mEditEmail.getText().toString());
        PhotoLocationUtils.saveData(map, getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();
        onSaveData();
    }

    @Override
    public void onConnected(final Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(getActivity(), permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(), permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        Log.i("Reid", "got location" + mLocation.getLongitude());
    }

    @Override
    public void onConnectionSuspended(final int i) {

    }

    @Override
    public void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(getActivity());
    }

    @Override
    public void onConnectionFailed(final ConnectionResult connectionResult) {

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

