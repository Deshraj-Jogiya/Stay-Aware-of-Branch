package com.schoolmgmtsys.root.ssg.utils;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.schoolmgmtsys.root.ssg.app.LoginPage;
import com.schoolmgmtsys.root.ssg.R;
import com.solutionsbricks.solbricksframework.helpers.CustomImageView;

public class DrawerListFragment extends ListFragment {

    public DrawerListFragment() {

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.drawer_list, null);
        SharedPreferences Prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        CustomImageView img =  view.findViewById(R.id.drawer_user_img);
        Integer id = Prefs.getInt("app_user_id", 0);
        if (id != 0) {
            img.profileID = String.valueOf(id);
            img.load();
        }
        TextView name =  view.findViewById(R.id.drawer_user_name);
        TextView email =  view.findViewById(R.id.drawer_user_email);
        LinearLayout logOut =  view.findViewById(R.id.logout);
        name.setText(Prefs.getString("app_user_fullName", ""));
        email.setText(Prefs.getString("app_user_username", ""));

        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences Prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                Prefs.edit().remove("app_username").apply();
                Prefs.edit().remove("app_password").apply();
                Intent MyIntent = new Intent(getContext(), LoginPage.class);
                MyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                MyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                getContext().startActivity(MyIntent);
                ((Activity) getContext()).finish();
            }
        });
        return view;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setListAdapter(new DrawerAdapter(getActivity()));

    }

}
