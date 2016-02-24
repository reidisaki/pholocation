package com.kalei.fragments;

import com.android.ex.chips.BaseRecipientAdapter;
import com.android.ex.chips.RecipientEditTextView;
import com.android.ex.chips.recipientchip.DrawableRecipientChip;
import com.kalei.PhotoLocationApplication;
import com.kalei.activities.MainActivity;
import com.kalei.pholocation.R;
import com.kalei.utils.PhotoLocationUtils;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.util.Rfc822Tokenizer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by risaki on 2/22/16.
 */
public class SettingsFragment extends PhotoLocationFragment {
    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        Bundle bundle = new Bundle();
//        bundle.putInt("question", questionNumber);
//        bundle.putBoolean("isCorrect", isCorrect);

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

        ((MainActivity) getActivity()).getSupportActionBar().show();
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        TextView versionText = (TextView) rootView.findViewById(R.id.version_text);
        TextView currentEmails = (TextView) rootView.findViewById(R.id.current_emails);
        final RecipientEditTextView emailRetv =
                (RecipientEditTextView) rootView.findViewById(R.id.phone_retv);
        emailRetv.setTokenizer(new Rfc822Tokenizer());
        emailRetv.setAdapter(new BaseRecipientAdapter(getActivity()));

        emailRetv.dismissDropDownOnItemSelected(true);

        Button saveBtn = (Button) rootView.findViewById(R.id.save_btn);
        String currentEmailString = PhotoLocationUtils.getEmailStringList(getActivity());
        currentEmails.setText(currentEmailString.length() == 0 ? "No emails set" : currentEmailString);
        saveBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {

                DrawableRecipientChip[] chips = emailRetv.getSortedRecipients();
                if (chips.length > 0) {
                    PhotoLocationUtils.saveData(chips, getActivity());
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
//        ListView listView = (ListView) rootView.findViewById(R.id.list);
//        List<Recipient> recipientList = PhotoLocationUtils.getData(getActivity());
//        if (recipientList.size() == 0) {
//            recipientList.add(new Recipient("Enter a new email, click here"));
//        }
//        RecipientAdapter adapter = new RecipientAdapter(getActivity(), R.layout.row_receipient, recipientList);
//        listView.setAdapter(adapter);

        versionText.setText("v " + PhotoLocationApplication.getInstance().getVersionName(getActivity()));
        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
    }
}
