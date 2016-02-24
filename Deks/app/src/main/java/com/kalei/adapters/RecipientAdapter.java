package com.kalei.adapters;

import com.kalei.models.Recipient;
import com.kalei.pholocation.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

/**
 * Created by risaki on 2/22/16.
 */
public class RecipientAdapter extends ArrayAdapter<Recipient> {
    private final Context mContext;
    private List<Recipient> mRecipientList;

    public RecipientAdapter(final Context context, final int resource, List<Recipient> list) {

        super(context, resource, list);
        mContext = context;
        mRecipientList = list;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.row_receipient, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.txt_name);
        textView.setText(mRecipientList.get(position).getFirstName());
        Button saveButton = (Button) rowView.findViewById(R.id.btn_save);
        ImageButton deleteButton = (ImageButton) rowView.findViewById(R.id.btn_delete);
//        textView.setText(values[position]);
        // change the icon for Windows and iPhone
//        String s = values[position];
//        if (s.startsWith("iPhone")) {
//            imageView.setImageResource(R.drawable.no);
//        } else {
//            imageView.setImageResource(R.drawable.ok);
//        }

        return rowView;
    }
}
