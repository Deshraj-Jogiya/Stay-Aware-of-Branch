package com.schoolmgmtsys.root.ssg.app;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.schoolmgmtsys.root.ssg.models.ClassesSchModel;
import com.schoolmgmtsys.root.ssg.R;
import com.schoolmgmtsys.root.ssg.utils.Concurrent;
import com.schoolmgmtsys.root.ssg.utils.Constants;
import com.schoolmgmtsys.root.ssg.utils.CustomPagerAdapter;
import com.schoolmgmtsys.root.ssg.utils.DrawerListFragment;

import java.util.ArrayList;
import java.util.HashMap;

@SuppressLint("SetJavaScriptEnabled")
public class ClassesSchPage extends SlidingFragmentActivity {

    private int CurrPage;
    private CustomPagerAdapter adapter;
    private HashMap<Integer, ArrayList<ClassesSchModel>> classSchMap;
    private ArrayList<String> daysNames;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Concurrent.getLangDirection(this).equals("ar"))
            getSlidingMenu().setMode(SlidingMenu.RIGHT);
        else getSlidingMenu().setMode(SlidingMenu.LEFT);

        setContentView(R.layout.classes_sch);

        ImageView backImage = (ImageView) findViewById(R.id.background_img);
        RelativeLayout logBack = (RelativeLayout) findViewById(R.id.full_layout);

        Resources res = getResources();
        if (!res.getBoolean(R.bool.x_gen_back_is_image)) {
            backImage.setVisibility(View.GONE);
            logBack.setBackgroundColor(Concurrent.getColor(this, R.color.x_gen_back));
        }

        getSlidingMenu().setBehindWidth((int) Math.round(Concurrent.getScreenWidth(this) * 70.0 / 100.0));
        setBehindContentView(R.layout.drawer_frame);

        ListFragment mFrag;
        FragmentTransaction t = this.getSupportFragmentManager().beginTransaction();
        if (savedInstanceState == null) {
            mFrag = new DrawerListFragment();
        } else {
            mFrag = (ListFragment) this.getSupportFragmentManager().findFragmentById(R.id.menu_frame);
        }
        t.replace(R.id.menu_frame, mFrag);
        t.commit();


        TextView HeadTitle = (TextView) findViewById(R.id.head_drawer_title);
        HeadTitle.setText(Concurrent.getLangSubWords("ClassSchedule", "Class Schedule"));

        ImageView ToogleBtn = (ImageView) findViewById(R.id.head_drawer_toggle);
        ToogleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        findViewById(R.id.gen_loader).setVisibility(View.INVISIBLE);

        classSchMap = (HashMap<Integer, ArrayList<ClassesSchModel>>) getIntent().getSerializableExtra("classSchList");
        daysNames = getIntent().getStringArrayListExtra("daysNames");

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        SmartTabLayout viewPagerTab = (SmartTabLayout) findViewById(R.id.tab);

        adapter = new CustomPagerAdapter(getSupportFragmentManager(), this, classSchMap,daysNames);
        CurrPage = Constants.detectToday(true);

        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(CurrPage, true);
        viewPagerTab.setViewPager(viewPager);
    }



}
