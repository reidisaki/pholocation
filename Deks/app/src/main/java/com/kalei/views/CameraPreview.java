package com.kalei.views;

import com.kalei.activities.MainActivity;
import com.kalei.interfaces.IPhotoTakenListener;
import com.kalei.pholocation.R;
import com.kalei.services.PhotoService;
import com.kalei.utils.PhotoLocationUtils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import java.io.File;

/**
 * Created by risaki on 3/11/16.
 */
public class CameraPreview extends LinearLayout implements OnClickListener {
    public static final String LONGITUDE = "longitude_key";
    public static final String LATTITUDE = "lattitude_key";
    public Button mOkButton, mCancelButton;
    public EditText mCaptionText;
    public ImageView mImageView;
    private IPhotoTakenListener mPhotoTakenListener;
    public ProgressBar mProgress;
    private String mImageFilepath;
    private String mOriginalImagePath;

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupView();
    }

    public void setOnPhotoTakenListener(IPhotoTakenListener listener) {
        mPhotoTakenListener = listener;
    }

    private void setupView() {
        final View view = LayoutInflater.from(getContext()).inflate(R.layout.preview, this);//inflater.inflate(R.layout.preview, this);

        mOkButton = (Button) view.findViewById(R.id.okay);
        mCancelButton = (Button) view.findViewById(R.id.cancel);
        mCaptionText = (EditText) view.findViewById(R.id.caption);
        mImageView = (ImageView) view.findViewById(R.id.imageView);
        mOkButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);
        mProgress = (ProgressBar) view.findViewById(R.id.progress);
        mCaptionText.setOnEditorActionListener(new OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                return false;
            }
        });
//        mPreviewButton = (ImageButton) view.findViewById(R.id.btn_preview);
    }

    public void setVisibility(int viewId, int visiblity) {
        View view = null;
        switch (viewId) {
            case R.id.progress:
                view = mProgress;
                break;
            case R.id.imageView:
                view = mImageView;
                break;
        }
        view.setVisibility(visiblity);
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.okay:
                Log.i("pl", "clicked ok");
                String caption = mCaptionText.getText().toString();
                mPhotoTakenListener.onPhotoConfirm();
                PhotoLocationUtils.savePhoto(getContext(), mImageFilepath, mOriginalImagePath);
                getContext().startService(getPhotoUploadIntent(caption));

                break;
            case R.id.cancel:
                Log.i("pl", "clicked cancel");
                deletePhoto(mImageFilepath);
                mPhotoTakenListener.onPhotoCancel();
                break;
        }
    }

    private void deletePhoto(final String imageFilepath) {
        if (imageFilepath != null && imageFilepath.length() > 0) {
            File f = new File(imageFilepath);
            f.delete();//delete temp saved in directory
        }
    }

    public void setImagePathsAndImageView(final String imagepath, String originalPath) {
        mImageFilepath = imagepath;
        //not really using this ever.
        mOriginalImagePath = originalPath;
        File imgFile = new File(imagepath);

        if (imgFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            mImageView.setImageBitmap(myBitmap);
        }
        Log.i("pl", "ok button enabled");
        mOkButton.setEnabled(true);
    }

    private Intent getPhotoUploadIntent(String caption) {
        Intent i = new Intent(getContext(), PhotoService.class);
        // potentially add data to the intent
//        i.putExtra(ORIGINAL_PICTURE_KEY, originalPicture);
//        i.putExtra(SCALED_PICTURE_KEY, scaledPicture);
        i.putExtra(PhotoLocationUtils.CAPTION_KEY, caption);
        if (MainActivity.mLocation != null) {
            i.putExtra(LONGITUDE, MainActivity.mLocation.getLongitude());
            i.putExtra(LATTITUDE, MainActivity.mLocation.getLatitude());
        }
        return i;
    }

    public void cleanUp() {
        //deletes the scaled version
        if (mImageFilepath != null) {
            deletePhoto(mImageFilepath);
        }
    }
}
