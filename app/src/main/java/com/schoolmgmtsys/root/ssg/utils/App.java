package com.schoolmgmtsys.root.ssg.utils;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.solutionsbricks.solbricksframework.SolUtils;

import java.util.Locale;

import io.fabric.sdk.android.Fabric;

public class App extends MultiDexApplication {

    /* ============================= BASE URL INSTRUCTIONS ===========================================

     " BASE URL is web domain of your school which you use to open your school at web browser "

     ** Please follow these instructions when you write your Base URL:
        1. Base URL must be the same you registered inside our website Solutionsbricks.com when you purchased Android application licence
        2. Base URL must start with your protocol http:// or https:// [ same protocol and URL registered inside your Solutionsbricks.com account ]
        3. Don't write "/login" or any Roots Abacus page path at end of your base URL, simply write only basic url

    =============== More information or support at Solutionsbricks.com =====================*/

    public static String TASK_BASE_URL     = "https://rootssg.schoolmgmtsys.com/";

    /* ============================= LICENCE CODE INSTRUCTIONS ===========================================

     " When you purchase Android application licence from Solutionsbricks.com, you can find your licence code inside your account's products "

     ** Please follow these instructions when you write your licence code:
        1. Every solution sold by Solutionsbricks.com have its unique licence code, so don't use licence code of any another product like Ios application or web products
        2. Android licence code must start with schstdand
        3. After purchase Android application, find licence code inside your account at Solutionsbricks.com, go to " My Bricks " section then to " My Products " section then find Android application product then copy licence code to here inside the code.

     =============== More information or support at Solutionsbricks.com =====================*/

    public static String TASK_LICENCE_KEY   = "schstdand-8512122.3-5d6e9fec729b2";

    public App() {
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseApp.initializeApp(this);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        Fabric.with(this, new Crashlytics());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String langDir = prefs.getString("lang_direction", null);
        if (langDir != null) {
            Locale locale = new Locale(langDir);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getApplicationContext().getResources().updateConfiguration(config, null);
        }
        prefs.edit().putString("ca", TASK_BASE_URL).apply();
        prefs.edit().putString("ad", TASK_LICENCE_KEY).apply();

        SolUtils.APP_BASE_URL = TASK_BASE_URL;
        SolUtils.TASK_PROFILE_IMG = Constants.TASK_PROFILE_IMG;
        SolUtils.DateFormat = Concurrent.DateFormat;
        SolUtils.ovUpEnded = false;
    }
    public static String getAppBaseUrl(){
       /* if(TASK_BASE_URL.endsWith("/")){
            return TASK_BASE_URL+"index.php";
        }else{
            return TASK_BASE_URL+"/index.php";
        }*/
        if(TASK_BASE_URL.endsWith("/")){
            return TASK_BASE_URL+"upload";
        }else{
            return TASK_BASE_URL+"/upload";
        }
    }
    public static String getAppBaseUrlWithoutModRewite(){
        return TASK_BASE_URL.replace("/index.php","");
    }
}
