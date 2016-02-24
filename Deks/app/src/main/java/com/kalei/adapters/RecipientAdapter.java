package com.kalei.adapters;

import com.kalei.models.Recipient;
import com.kalei.pholocation.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by risaki on 2/22/16.
 */
public class RecipientAdapter extends ArrayAdapter<Recipient> {
    private final Context mContext;
    private List<Recipient> mRecipientList;
    private ViewHolder holder;

    public RecipientAdapter(final Context context, final int resource, List<Recipient> list) {

        super(context, resource, list);
        mContext = context;
        mRecipientList = list;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) { // if convertView is null
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.row_receipient,
                    parent, false);
            holder = new ViewHolder();
            // initialize views
            convertView.setTag(holder);  // set tag on view
            holder.textView = (TextView) convertView.findViewById(R.id.txt_name);
            holder.textView.setText(mRecipientList.get(position).getEmail());
            holder.saveButton = (Button) convertView.findViewById(R.id.btn_save);
            holder.deleteButton = (ImageButton) convertView.findViewById(R.id.btn_delete);
            holder.editText = (EditText) convertView.findViewById(R.id.edit_name);
        } else {
            holder = (ViewHolder) convertView.getTag();
            // if not null get tag
            // no need to initialize
        }
        setListeners(holder);
//        textView.setText(values[position]);
        // change the icon for Windows and iPhone
//        String s = values[position];
//        if (s.startsWith("iPhone")) {
//            imageView.setImageResource(R.drawable.no);
//        } else {
//            imageView.setImageResource(R.drawable.ok);
//        }

        return convertView;
    }

    private void setListeners(final ViewHolder holder) {
        holder.saveButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                //save data;
                holder.textView.setVisibility(View.VISIBLE);
                holder.editText.setVisibility(View.GONE);
                holder.textView.setText(holder.editText.getText().toString());
                Toast.makeText(mContext, "Saved!", Toast.LENGTH_SHORT).show();
            }
        });
        holder.textView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                holder.textView.setVisibility(View.GONE);
                holder.editText.setVisibility(View.VISIBLE);
            }
        });
    }

    static class ViewHolder {
        Button saveButton;
        TextView textView;
        EditText editText;
        ImageButton deleteButton;
    }
}
