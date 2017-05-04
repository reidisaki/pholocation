package com.kalei.fragments;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import com.android.ex.chips.BaseRecipientAdapter;
import com.android.ex.chips.RecipientEditTextView;
import com.android.ex.chips.RecipientEntry;
import com.android.ex.chips.recipientchip.DrawableRecipientChip;
import com.kalei.PhotoLocationApplication;
import com.kalei.activities.MainActivity;
import com.kalei.activities.PhotoLocationActivity;
import com.kalei.managers.PrefManager;
import com.kalei.pholocation.R;
import com.kalei.utils.PhotoLocationUtils;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.annotation.Nullable;
import android.text.util.Rfc822Tokenizer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by risaki on 2/22/16.
 */
public class SettingsFragment extends PhotoLocationFragment implements OnCheckedChangeListener {
    public RecipientEditTextView emailRetv;
    //    amazon
//    public AdLayout adView;
    public AdView adView;

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

        if (VERSION.SDK_INT > VERSION_CODES.KITKAT) {
            ((MainActivity) getActivity()).getActionBar().show();
        }
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        TextView versionText = (TextView) rootView.findViewById(R.id.version_text);
        TextView currentEmails = (TextView) rootView.findViewById(R.id.current_emails);
        emailRetv =
                (RecipientEditTextView) rootView.findViewById(R.id.email_retv);
        emailRetv.setTokenizer(new Rfc822Tokenizer());
        emailRetv.setAdapter(new BaseRecipientAdapter(getActivity()));
        emailRetv.requestFocus();

        emailRetv.dismissDropDownOnItemSelected(true);
        for (RecipientEntry r : PhotoLocationUtils.getDataObjects(getActivity())) {
            emailRetv.addRecipient(r);
        }

        ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
                .toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        Button saveBtn = (Button) rootView.findViewById(R.id.save_btn);
        String currentEmailString = PhotoLocationUtils.getEmailStringList(getActivity());
        currentEmails.setText(currentEmailString != null && currentEmailString.length() == 0 ? "Please enter at least one email" : currentEmailString);
        saveBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                DrawableRecipientChip[] chips = emailRetv.getSortedRecipients();

                if (chips.length == 0) {
                    RecipientEntry r = RecipientEntry.constructGeneratedEntry(emailRetv.getText().toString(), emailRetv.getText().toString(), true);
                    emailRetv.addRecipient(r);
                    chips = emailRetv.getSortedRecipients();
                }
                List<RecipientEntry> list = new ArrayList<>();
                for (DrawableRecipientChip chip : chips) {
                    list.add(chip.getEntry());
                }
                if (chips.length > 0) {
                    PhotoLocationUtils.saveDataObjects(list, getActivity());
                    Toast.makeText(getActivity(), "Saved", Toast.LENGTH_SHORT).show();
                }
                //hide keyboard
                if (v != null) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                ((MainActivity) getActivity()).clickBack();
            }
        });

        // If you declared AdLayout in your xml you would instead
        // replace the 3 lines above with the following line:
//        adView = (AdLayout) rootView.findViewById(R.id.adview);
//
//        AdTargetingOptions adOptions = new AdTargetingOptions();
//        // Optional: Set ad targeting options here.
//        adView.loadAd(adOptions); // Retrieves an ad on background thread

        //GOOGLE BS
        AdView mAdView = (AdView) rootView.findViewById(R.id.adView);
        if (mAdView != null) {
            String android_id = Secure.getString(getActivity().getContentResolver(),
                    Secure.ANDROID_ID);
            AdRequest adRequest = new AdRequest.Builder().build();

            mAdView.loadAd(adRequest);
        }
        versionText.setText("v " + PhotoLocationApplication.getInstance().getVersionName(getActivity()));
        Switch wifiSwitch = (Switch) rootView.findViewById(R.id.send_wifi_switch);
        Switch saveOriginaLSwitch = (Switch) rootView.findViewById(R.id.save_original_switch);
        Switch requireCommentSwitch = (Switch) rootView.findViewById(R.id.require_comment_switch);

        //TODO: Remove this later
        if (PhotoLocationApplication.debug) {
            TextView sendTextOnlyText = (TextView) rootView.findViewById(R.id.send_text_only_text);
            sendTextOnlyText.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(final View v) {
                    PrefManager.clear(getActivity());
                    Log.i("Reid", "Cleared shared prefs");
                }
            });
        }
        //End remove

        wifiSwitch.setChecked(PrefManager.getSendWifiOnly(getActivity()));
        wifiSwitch.setOnCheckedChangeListener(this);

        saveOriginaLSwitch.setChecked(PrefManager.getOriginalOnly(getActivity()));
        saveOriginaLSwitch.setOnCheckedChangeListener(this);

        requireCommentSwitch.setChecked(PrefManager.getCommentRequired(getActivity()));
        requireCommentSwitch.setOnCheckedChangeListener(this);
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (adView != null) {
            adView.destroy();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(emailRetv.getWindowToken(), 0);
    }

    @Override
    public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.send_wifi_switch:
                PrefManager.setSendWifiOnly(getActivity(), isChecked);
                break;
            case R.id.save_original_switch:
                PrefManager.saveOriginalPhoto(getActivity(), isChecked);
                break;
            case R.id.require_comment_switch:
                PrefManager.setCommentRequired(getActivity(), isChecked);
                break;
        }
    }
}
