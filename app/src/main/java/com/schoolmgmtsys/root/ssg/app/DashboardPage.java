package com.schoolmgmtsys.root.ssg.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.flyco.tablayout.CommonTabLayout;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.solbricks.solbrickscal.MultiCalsUtils;
import com.solbricks.solbrickscal.TodayDetector;
import com.schoolmgmtsys.root.ssg.R;
import com.schoolmgmtsys.root.ssg.utils.App;
import com.schoolmgmtsys.root.ssg.utils.Concurrent;
import com.schoolmgmtsys.root.ssg.utils.DrawerListFragment;
import com.solutionsbricks.solbricksframework.helpers.TabEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class DashboardPage extends SlidingFragmentActivity implements TodayDetector.TodayDetectorInterface {

    private ListFragment mFrag;
    private TextView HeadTitle;
    private SweetAlertDialog langDialog;
    private CommonTabLayout dashboardTabs;
    private ViewPager dashboardPager;
    private ArrayList<Fragment> mFragments = new ArrayList<>();
    private ArrayList<String> mTitles = new ArrayList<>();

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (Concurrent.getLangDirection(this).equals("ar"))
            getSlidingMenu().setMode(SlidingMenu.RIGHT);
        else getSlidingMenu().setMode(SlidingMenu.LEFT);

        setContentView(R.layout.page_dashboard);

        ImageView backImage = (ImageView) findViewById(R.id.background_img);
        LinearLayout logBack = (LinearLayout) findViewById(R.id.full_layout);

        Resources res = getResources();
        if (!res.getBoolean(R.bool.x_gen_back_is_image)) {
            backImage.setVisibility(View.GONE);
            logBack.setBackgroundColor(Concurrent.getColor(this, R.color.x_gen_back));
        }

        // Remove progress bar
        findViewById(R.id.gen_loader).setVisibility(View.GONE);
        if (Concurrent.TDate == null) {
            TodayDetector mTDay = new TodayDetector(this);
            mTDay.getToday(getBaseContext());
        }
        getSlidingMenu().setBehindWidth((int) Math.round(Concurrent.getScreenWidth(this) * 70.0 / 100.0));
        setBehindContentView(R.layout.drawer_frame);

        if (savedInstanceState == null) {
            mFrag = new DrawerListFragment();
        } else {
            mFrag = (ListFragment) this.getSupportFragmentManager().findFragmentById(R.id.menu_frame);
        }

        //================ Dashboard tabs init ====================//

        dashboardTabs = (CommonTabLayout) findViewById(R.id.dashboard_tabs);
        dashboardPager = (ViewPager) findViewById(R.id.dashboard_pager);

        dashboardTabs.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                if (getLangDirection(getBaseContext()).equals("ar")) {
                    dashboardPager.setCurrentItem(2 - position); // 2 is ( tabs count - 1)
                }else{
                    dashboardPager.setCurrentItem(position);
                }
            }

            @Override
            public void onTabReselect(int position) {

            }
        });

        dashboardPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (getLangDirection(getBaseContext()).equals("ar")){
                    dashboardTabs.setCurrentTab(2 - position); // 2 is ( tabs count - 1)
                }else{
                    dashboardTabs.setCurrentTab(position);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //================== Set school name ===================//
        HeadTitle = (TextView) findViewById(R.id.head_drawer_title);
        String SchoolName = Concurrent.getSchoolName(this);
        if (SchoolName != null) HeadTitle.setText(SchoolName);
        else HeadTitle.setText("My School");

        //================== Set toggle button listener to open drawer ===================//
        findViewById(R.id.head_drawer_toggle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        //================== Maintain app versions check ===================//
        SharedPreferences Prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Prefs.edit().putString("YmFzZV91cmw", App.getAppBaseUrl()).apply();
        Prefs.edit().putString("bGljZW5jZV9rZXk", App.TASK_LICENCE_KEY).apply();

        int savedAppVersion = Prefs.getInt("savedAppVersion", 0);
        int CurrentAppVersion = 0;
        try {
//            CurrentAppVersion = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionCode;
            if (Build.VERSION.SDK_INT >= 28) {
                CurrentAppVersion = (int) this.getPackageManager().getPackageInfo(this.getPackageName(), 0).getLongVersionCode();
                Log.d("appVersionCode", "App Version Code: " + CurrentAppVersion);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (savedAppVersion > 0) {
            if (CurrentAppVersion > 0 && CurrentAppVersion != savedAppVersion) {
                Prefs.edit().clear().apply();

                Intent MyIntent = new Intent(getBaseContext(), LoginPage.class);
                MyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                MyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(MyIntent);
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                finish();
                if (CurrentAppVersion > 0)
                    Prefs.edit().putInt("savedAppVersion", CurrentAppVersion).apply();
            }
        } else {
            if (CurrentAppVersion > 0)
                Prefs.edit().putInt("savedAppVersion", CurrentAppVersion).apply();
        }


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String dashboardJsonDataString = extras.getString("dashboard_data");
            JsonObject dashboardJsonData = new Gson().fromJson(dashboardJsonDataString, JsonObject.class);

            if (dashboardJsonData != null) {

                parseDashboardData(dashboardJsonData);

            } else {

                Intent MyIntent = new Intent(getBaseContext(), LoginPage.class);
                MyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                MyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(MyIntent);
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                finish();

            }
        }

    }


    public void setTabs(JsonObject dashboardJsonData) {
        // Set tabs data
        ArrayList<CustomTabEntity> mTabEntities = new ArrayList<>();

      /*  mTabEntities.add(new TabEntity("", R.drawable.dash_tab_leaderboard_selected, R.drawable.dash_tab_leaderboard_unselected));
        mFragments.add(DashboardLeaderBoardFrag.getInstance(dashboardJsonData));
        mTitles.add("LeaderBoard");
*/
        mTabEntities.add(new TabEntity("", R.drawable.dash_tab_stat_selected, R.drawable.dash_tab_stat_unselected));
        mFragments.add(DashboardStatFrag.getInstance(dashboardJsonData));
        mTitles.add("Dashboard");

        mTabEntities.add(new TabEntity("", R.drawable.dash_tab_news_selected, R.drawable.dash_tab_news_unselected));
        mFragments.add(DashboardNewsFrag.getInstance(dashboardJsonData));
        mTitles.add("News-Events");

        dashboardTabs.setTabData(mTabEntities);

        dashboardPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
        dashboardPager.setCurrentItem(1);

    }

    public void parseDashboardData(JsonObject ValuesHolder) {
        SharedPreferences Prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        if (ValuesHolder.has("notification")) {
            JsonObject notificationsTimesOptions = ValuesHolder.get("notification").getAsJsonObject();
            if (notificationsTimesOptions != null) {
                Concurrent.setRefreshInMessagesInterval(getBaseContext(), Float.valueOf(Concurrent.tagsStringValidator(notificationsTimesOptions, "m")));
                Concurrent.setRefreshOutMessagesInterval(getBaseContext(), Float.valueOf(Concurrent.tagsStringValidator(notificationsTimesOptions, "c")));
            }
        }

        String gcalendar = Concurrent.tagsStringValidator(ValuesHolder, "gcalendar");

        if (gcalendar != null && !gcalendar.equals("")) {
            MultiCalsUtils.MultiCalendarType = gcalendar;
            Prefs.edit().putString("app_calendar", gcalendar).apply();
        }

        JsonObject BaseUserHolder = Concurrent.getJsonObject(ValuesHolder, "baseUser");

        String dateformat = Concurrent.tagsStringValidator(ValuesHolder, "dateformat");
        if (dateformat != null && !dateformat.equals("")) {
            Concurrent.DateFormat = dateformat.toLowerCase().replace("d", "dd").replace("m", "mm").replace("y", "yyyy");
        }


        Prefs.edit().putString("app_user_username", Concurrent.tagsStringValidator(BaseUserHolder, "username")).apply();
        Prefs.edit().putString("app_user_fullName", Concurrent.tagsStringValidator(BaseUserHolder, "fullName")).apply();

        String role = Concurrent.tagsStringValidator(ValuesHolder, "role");
        Prefs.edit().putString("app_user_role", role).apply();
        Concurrent.setAppRole(role);

        String mID = Concurrent.tagsStringValidator(BaseUserHolder, "id");
        PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putInt("app_user_id", Integer.valueOf(mID)).apply();
        try {
            Concurrent.APP_USER_ID = Integer.valueOf(mID);
        } catch (Exception e) {
        }

        JsonObject langObject = Concurrent.getJsonObject(ValuesHolder, "language");
        if (langObject != null) {
            for (Map.Entry<String, JsonElement> entry : langObject.entrySet()) {
                Concurrent.LanguageHolder.put(entry.getKey().toLowerCase(), entry.getValue().getAsString());
            }
            Prefs.edit().putString("signIn_trans", Concurrent.getLangSubWords("signIn", "Sign in")).apply();
            Prefs.edit().putString("userNameOrEmail_trans", Concurrent.getLangSubWords("userNameOrEmail", "Username/Email")).apply();
            Prefs.edit().putString("password_trans", Concurrent.getLangSubWords("password", "Password")).apply();
        }

        String AttendModel = Concurrent.tagsStringValidator(ValuesHolder, "attendanceModel");
        Concurrent.AttendanceModelIsClass = (AttendModel.equals("class"));


        String lastDir = Prefs.getString("lang_direction", null);

        if (ValuesHolder.has("languageUniversal")) {
            JsonElement UniLang = ValuesHolder.get("languageUniversal");
            String newDir;
            if (UniLang == null || UniLang.isJsonNull()) newDir = "en";
            else newDir = UniLang.getAsString();

            Prefs.edit().putString("lang_direction", newDir).apply();
            if ((lastDir != null && newDir != null && !lastDir.equals(newDir)) || (newDir.equals("ar") && lastDir == null)) {
                langDialog = new SweetAlertDialog(DashboardPage.this, SweetAlertDialog.WARNING_TYPE).setTitleText("Please Reopen App").setContentText("Language Configurations Changed").setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        moveTaskToBack(true);
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(1);

                        langDialog.cancel();
                    }
                });
                langDialog.setCanceledOnTouchOutside(true);
                try {
                    langDialog.show();
                } catch (Exception ignored) {
                }
            }
        }


        try {
            FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.menu_frame, mFrag);
            transaction.commitAllowingStateLoss();
        } catch (Exception ignored) {
        }

        String SiteTitleHolder = Concurrent.tagsStringValidator(ValuesHolder, "siteTitle");
        if (SiteTitleHolder != null) {
            Concurrent.APP_SCHOOL_NAME = SiteTitleHolder;
            Concurrent.setSchoolName(getBaseContext(), SiteTitleHolder);
            HeadTitle.setText(SiteTitleHolder);
        }

        String sectionState = null;
        try {
            sectionState = Concurrent.tagsStringValidator(ValuesHolder, "enableSections");
        } catch (Exception ignored) {
        }

        if (sectionState != null) {
            Boolean EnableSections = !sectionState.equals("0");
            Concurrent.APP_SECTION_ENABLED = EnableSections;
            Concurrent.setSectionState(getBaseContext(), EnableSections);
        }

        //================== Parse admin permission ================//
        try {
            JsonArray userPerm = ValuesHolder.get("perms").getAsJsonArray();
            if (userPerm != null) {
                Set<String> set = new HashSet<>();
                for (JsonElement module : userPerm) set.add(module.getAsString());
                Prefs.edit().remove("user_custom_perm").apply();
                Prefs.edit().putStringSet("user_custom_perm", set).apply();
                Concurrent.modulesPermissions = set;
            }

        } catch (Exception ignored2) {

        }

        //================== Parse activated modules ================//
        if (ValuesHolder.has("activatedModules")) {

            JsonArray activatedModules = ValuesHolder.get("activatedModules").getAsJsonArray();

            if (activatedModules.size() > 0) {
                Prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                Set<String> set = new HashSet<>();
                for (JsonElement module : activatedModules) {
                    set.add(module.getAsString());
                }
                Prefs.edit().putStringSet("active_modules", set).apply();
                Concurrent.modulesActivated = set;
            }
        }


            //================== Set tabs ================//

            setTabs(ValuesHolder);
        }

        @Override
        public void getTodayDate (String today){
            Concurrent.TDate = today;
        }


        private class MyPagerAdapter extends FragmentPagerAdapter {
            private MyPagerAdapter(FragmentManager fm) {
                super(fm);
            }

            @Override
            public int getCount() {
                return mFragments.size();
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return mTitles.get(getLangDirection(getBaseContext()).equals("ar") ? ( 2 - position ): position);
            }

            @Override
            public Fragment getItem(int position) {
                return mFragments.get(getLangDirection(getBaseContext()).equals("ar") ? ( 2 - position ): position);
            }
        }

    }
