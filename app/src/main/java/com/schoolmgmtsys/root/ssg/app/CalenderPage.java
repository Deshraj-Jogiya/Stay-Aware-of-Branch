package com.schoolmgmtsys.root.ssg.app;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.MyContextWrapper;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.schoolmgmtsys.root.ssg.R;
import com.schoolmgmtsys.root.ssg.utils.Concurrent;
import com.schoolmgmtsys.root.ssg.utils.DrawerListFragment;
import com.solutionsbricks.solbricksframework.helpers.SBDatePickerDialog;


public class CalenderPage extends SlidingFragmentActivity implements SBDatePickerDialog.DatePickerMultiCalsInterface{

    MaterialCalendarView widget;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Concurrent.getLangDirection(this).equals("ar"))
            getSlidingMenu().setMode(SlidingMenu.RIGHT);
        else getSlidingMenu().setMode(SlidingMenu.LEFT);

        setContentView(R.layout.page_calender);

        getSlidingMenu().setBehindWidth((int) Math.round(Concurrent.getScreenWidth(this) * 70.0 / 100.0));
        setBehindContentView(R.layout.drawer_frame);

        ImageView backImage = (ImageView) findViewById(R.id.background_img);
        FrameLayout logBack = (FrameLayout) findViewById(R.id.full_layout);

        Resources res = getResources();
        if (!res.getBoolean(R.bool.x_gen_back_is_image)) {
            backImage.setVisibility(View.GONE);
            logBack.setBackgroundColor(Concurrent.getColor(this, R.color.x_gen_back));
        }

        findViewById(R.id.gen_loader).setVisibility(View.GONE);
        findViewById(R.id.refresh).setVisibility(View.GONE);

        TextView HeadTitle = (TextView) findViewById(R.id.head_drawer_title);
        HeadTitle.setText(Concurrent.getLangSubWords("Calender", "Calender"));

        ImageView ToogleBtn = (ImageView) findViewById(R.id.head_drawer_toggle);
        ToogleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        ListFragment mFrag;
        FragmentTransaction t = this.getSupportFragmentManager().beginTransaction();
        if (savedInstanceState == null) {
            mFrag = new DrawerListFragment();
        } else {
            mFrag = (ListFragment) this.getSupportFragmentManager().findFragmentById(R.id.menu_frame);
        }
        t.replace(R.id.menu_frame, mFrag);
        t.commit();

        findViewById(R.id.calendar_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //============================= Open View When Click  ====================//
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                Fragment prev = getSupportFragmentManager().findFragmentByTag("calendar");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);

                // Create and show the dialog.
                DialogFragment newFragment = new SBDatePickerDialog().newInstance(getBaseContext(), "calendar","dd-mm-yyyy");
                newFragment.show(ft, "calendar");

            }
        });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(MyContextWrapper.wrap(newBase, Concurrent.getLangDirection(newBase)));
    }



    @Override
    public void onDatePicked(Intent data) {
        Bundle bundle = data.getExtras();
        String SelectedDateAsString = bundle.getString("date");
        //String SelectedDateAsTimeStamp = bundle.getString("timestamp");       // Not used
        //String senderTag = bundle.getString("tag");                           // Not used
        //String reformattedDate = Concurrent.changeDateFormat(SelectedDateAsString,"dd/MM/yyyy","yyyy-MM-dd");
        Intent MyIntent = new Intent(getBaseContext(), CalenderItemPage.class);
        MyIntent.putExtra("date_value", SelectedDateAsString);
        startActivity(MyIntent);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);

    }

}
