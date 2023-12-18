package com.schoolmgmtsys.root.ssg.app;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.schoolmgmtsys.root.ssg.R;

import java.util.List;

public class DemoLoginAdapter extends ArrayAdapter<DemoLoginAdapterOption> {

    public DemoLoginAdapter(Context context, List<DemoLoginAdapterOption> options) {
        super(context, 0, options);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder vh;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.demo_login_picker_item, parent, false);
            vh = new ViewHolder(convertView);
            convertView.setTag(vh);
        } else vh = (ViewHolder) convertView.getTag();

        DemoLoginAdapterOption option = getItem(position);
        if(option != null) vh.accountName.setText(option.accountName);

        return convertView;
    }

    private static final class ViewHolder {
        TextView accountName;

        private ViewHolder(View v) {
            accountName = (TextView) v.findViewById(R.id.account_name);
        }
    }
}