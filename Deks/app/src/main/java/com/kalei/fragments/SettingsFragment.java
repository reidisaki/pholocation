package com.kalei.fragments;

import com.kalei.PhotoLocationApplication;
import com.kalei.activities.MainActivity;
import com.kalei.adapters.RecipientAdapter;
import com.kalei.models.Recipient;
import com.kalei.pholocation.R;
import com.kalei.utils.PhotoLocationUtils;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

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

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        ((MainActivity) getActivity()).getSupportActionBar().show();
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        TextView versionText = (TextView) rootView.findViewById(R.id.version_text);
        ListView listView = (ListView) rootView.findViewById(R.id.list);
        List<Recipient> recipientList = PhotoLocationUtils.getData(getActivity());
        if (recipientList.size() == 0) {
            recipientList.add(new Recipient("Enter a new email, click here"));
        }
        RecipientAdapter adapter = new RecipientAdapter(getActivity(), R.layout.row_receipient, recipientList);
        listView.setAdapter(adapter);

        versionText.setText("v " + PhotoLocationApplication.getInstance().getVersionName(getActivity()));
        return rootView;
    }
}
