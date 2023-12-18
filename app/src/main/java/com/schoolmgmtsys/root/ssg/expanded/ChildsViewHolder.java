package com.schoolmgmtsys.root.ssg.expanded;

import android.view.View;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.ViewHolder.ChildViewHolder;
import com.schoolmgmtsys.root.ssg.models.DashLeadModel;
import com.schoolmgmtsys.root.ssg.R;
import com.solutionsbricks.solbricksframework.helpers.CustomImageView;

public class ChildsViewHolder extends ChildViewHolder {

    private TextView name;
    private TextView msg;
    private CustomImageView image;

    public ChildsViewHolder(View itemView) {
        super(itemView);
        name = (TextView) itemView.findViewById(R.id.dash_lead_name);
        msg = (TextView) itemView.findViewById(R.id.dash_lead_msg);
        image = (CustomImageView) itemView.findViewById(R.id.dash_lead_img);
    }

    public void bind(DashLeadModel item) {
        name.setText(item.name);
        msg.setText(item.msg);
        image.profileID = String.valueOf(item.id);
        image.load();
    }
}
