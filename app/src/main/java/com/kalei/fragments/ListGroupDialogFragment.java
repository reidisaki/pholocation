package com.kalei.fragments;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import com.android.ex.chips.RecipientEntry;
import com.kalei.pholocation.R;
import com.kalei.utils.PhotoLocationUtils;
import com.kalei.utils.PhotoLocationUtils.UriDeserializer;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by risaki on 6/23/17.
 */

public class ListGroupDialogFragment extends DialogFragment {
    public static final java.lang.String GROUP_BUTTON_ID = "groupbuttonid";
    public static String GROUP_LIST_NAME_KEY = "grouplistkey";
    public static String GROUP_LIST_DATA_KEY = "grouplistdatakey";
    public String mOldGroupName;

    public interface ListGroupDialogFragmentListener {
        void onSaved(String groupName, String buttonId);
        void onDeleted(String buttonId);
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {

        final ListGroupDialogFragmentListener mListener = (ListGroupDialogFragmentListener) getTargetFragment();
        Bundle mArgs = getArguments();
        final String listValue = mArgs.getString(GROUP_LIST_NAME_KEY);
        final String listDataValues = mArgs.getString(GROUP_LIST_DATA_KEY);
        final String buttonId = mArgs.getString(GROUP_BUTTON_ID);

        mOldGroupName = listValue;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setTitle("Manage list");
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View v = inflater.inflate(R.layout.list_group_view, null);
        final EditText listNameEditText = (EditText) v.findViewById(R.id.list_name);
        listNameEditText.setText(listValue);

        builder.setView(v)
                // Add action buttons
                .setPositiveButton("save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //save the group of names/emails.chips
                        //delete old group, and save with the new name
                        Type listType = new TypeToken<ArrayList<RecipientEntry>>() {
                        }.getType();
                        Gson gson = new GsonBuilder()
                                .registerTypeAdapter(Uri.class, new UriDeserializer())
                                .create();
                        List<RecipientEntry> list = gson.fromJson(listDataValues, listType);
                        //delete old group name and update with new one, if it's the same name then do nothing.
                        PhotoLocationUtils.deleteGroup(getActivity(), mOldGroupName);
                        PhotoLocationUtils.saveDataObjects(listNameEditText.getText().toString(), list, getActivity());
                        mListener.onSaved(listNameEditText.getText().toString(), buttonId);
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ListGroupDialogFragment.this.getDialog().cancel();
                    }
                })
                .setNeutralButton("delete", new OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        PhotoLocationUtils.deleteGroup(getActivity(), listValue);
                        mListener.onDeleted(buttonId);
                    }
                });
        return builder.create();
    }
}
