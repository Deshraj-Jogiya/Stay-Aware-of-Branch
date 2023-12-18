package com.schoolmgmtsys.root.ssg.app;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.schoolmgmtsys.root.ssg.R;
import com.schoolmgmtsys.root.ssg.utils.Concurrent;
import com.schoolmgmtsys.root.ssg.utils.DrawerListFragment;

import java.util.ArrayList;

public class ControlActivity extends SlidingFragmentActivity {

    private String TargetFragment;
    private Fragment FireOnObject;
    private ArrayList<Parcelable> ExtraArrayList;
    private ArrayList<Parcelable> ExtraArrayList2;
    private int ExtraInt1 = -1;
    private Parcelable EditObject;
    private ProgressBar mProgressBar;
    private String ExtraString1 = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Concurrent.getLangDirection(this).equals("ar"))
            getSlidingMenu().setMode(SlidingMenu.RIGHT);
        else getSlidingMenu().setMode(SlidingMenu.LEFT);

        setContentView(R.layout.control_main);


        FrameLayout contentLayout = (FrameLayout) findViewById(R.id.fragment_content);
        ImageView contentBackImage = (ImageView) findViewById(R.id.background_img);


        Resources res = getResources();
        if (!res.getBoolean(R.bool.x_gen_back_is_image)) {
            contentBackImage.setVisibility(View.GONE);
            contentLayout.setBackgroundColor(Concurrent.getColor(this, R.color.x_gen_back));
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

        mProgressBar = (ProgressBar) findViewById(R.id.gen_loader);

        ImageView ToogleBtn = (ImageView) findViewById(R.id.head_drawer_toggle);
        ToogleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        mProgressBar.setVisibility(View.INVISIBLE);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_content);


        if (fragment == null) {
            if (savedInstanceState == null) {
                Bundle extras = getIntent().getExtras();
                if (extras == null) {
                    TargetFragment = null;
                } else {
                    TargetFragment = extras.getString("TARGET_FRAGMENT");
                    ExtraArrayList = extras.getParcelableArrayList("EXTRA_LIST");
                    ExtraArrayList2 = extras.getParcelableArrayList("EXTRA_LIST2");
                    EditObject = extras.getParcelable("EDIT_OBJECT");
                    ExtraInt1 = extras.getInt("EXTRA_INT_1");
                    ExtraString1 = extras.getString("EXTRA_STRING_1");

                    String HEAD_FIND_WORD = extras.getString("EXTRA_HEAD_FIND_WORD");
                    String HEAD_REPLACE_WORD = extras.getString("EXTRA_HEAD_REPLACE_WORD");
                    if (HEAD_FIND_WORD != null && HEAD_REPLACE_WORD != null)
                        ((TextView) findViewById(R.id.head_drawer_title)).setText(Concurrent.getLangSubWords(HEAD_FIND_WORD, HEAD_REPLACE_WORD));

                }
            } else {
                TargetFragment = (String) savedInstanceState.getSerializable("TARGET_FRAGMENT");
            }

            if (TargetFragment != null) {
                if ("DashLeaderViewPage".equals(TargetFragment)) {
                    FireOnObject = new DashLeaderViewPage();
                } else if ("ClassesSchPage".equals(TargetFragment)) {
                    FireOnObject = new ClassesSchChoosePage();
                } else if ("ExamsMarksChoose".equals(TargetFragment)) {
                    FireOnObject = new ExamsMarksChoose();
                } else if ("studentShowMarks".equals(TargetFragment)) {
                    FireOnObject = new StudentsShowMarksPage();
                } else if ("studentAttendance".equals(TargetFragment)) {
                    FireOnObject = new StudentsAttendancePage();
                } else if ("ParentsAttendance".equals(TargetFragment)) {
                    FireOnObject = new ParentsAttendancePage();
                } else if ("AssignmentViewAnswers".equals(TargetFragment)) {
                    FireOnObject = new AssignmentViewAnswers();
                } else if ("OnlineExamShowMarks".equals(TargetFragment)) {
                    FireOnObject = new OnlineExamShowMarks();
                } else if ("SearchView".equals(TargetFragment)) {
                    FireOnObject = new SearchView();
                }
            }
            if (FireOnObject != null) {
                FragmentTransaction ft = fm.beginTransaction();
                Bundle bundle = new Bundle();
                boolean passBundle = false;

                if (ExtraArrayList != null) {
                    bundle.putParcelableArrayList("EXTRA_LIST_FRAG", ExtraArrayList);
                    passBundle = true;
                }
                if (ExtraArrayList2 != null) {
                    bundle.putParcelableArrayList("EXTRA_LIST_FRAG_2", ExtraArrayList2);
                    passBundle = true;
                }
                if (ExtraInt1 != -1) {
                    bundle.putInt("EXTRA_INT_1_FRAG", ExtraInt1);
                    passBundle = true;
                }
                if (ExtraString1 != null) {
                    bundle.putString("EXTRA_STRING_1", ExtraString1);
                    passBundle = true;
                }
                if (EditObject != null) {
                    bundle.putParcelable("EDIT_OBJECT", EditObject);
                    passBundle = true;
                }
                if (passBundle) FireOnObject.setArguments(bundle);
                ft.add(R.id.fragment_content, FireOnObject);
                ft.commit();
            } else {
                Intent MyIntent = new Intent(getBaseContext(), DashboardPage.class);
                startActivity(MyIntent);
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            }
        }
    }


    public ProgressBar getProgressBar() {
        return mProgressBar;
    }

}
