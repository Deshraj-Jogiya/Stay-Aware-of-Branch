package com.schoolmgmtsys.root.ssg.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.javiersantos.bottomdialogs.BottomDialog;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.solbricks.solbrickscal.TodayDetector;
import com.schoolmgmtsys.root.ssg.Notifications.MyFirebaseMessagingService;
import com.schoolmgmtsys.root.ssg.R;
import com.schoolmgmtsys.root.ssg.utils.App;
import com.schoolmgmtsys.root.ssg.utils.Concurrent;
import com.solutionsbricks.solbricksframework.Call;
import com.solutionsbricks.solbricksframework.Callback;
import com.solutionsbricks.solbricksframework.FormBody;
import com.solutionsbricks.solbricksframework.OkHttpClient;
import com.solutionsbricks.solbricksframework.Request;
import com.solutionsbricks.solbricksframework.Response;

import java.io.IOException;
import java.net.ConnectException;
import java.util.concurrent.TimeUnit;

public class SplashPage extends Activity implements TodayDetector.TodayDetectorInterface {


    private boolean BypassLogin;
    private SharedPreferences Prefs;
    private Integer LoginErrorCounter = 1;
    private Integer DashboardErrorCounter = 1;
    private String response;
    private Bundle extras;
    public static final String TAG = SplashPage.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_splash);

        extras = getIntent().getExtras();
        if (extras != null) {
            BypassLogin = extras.getBoolean("bypass_login");
        }

        if (savedInstanceState != null) {
            BypassLogin = savedInstanceState.getBoolean("BypassLogin");
        }


        RelativeLayout logBack = (RelativeLayout) findViewById(R.id.log_full_layout);
        ImageView backImage = (ImageView) findViewById(R.id.background_img);
        if (Concurrent.TDate == null) {
            TodayDetector mTDay = new TodayDetector(this);
            mTDay.getToday(getBaseContext());
        }

        Resources res = getResources();
        if (!res.getBoolean(R.bool.x_login_back_is_image)) {
            backImage.setVisibility(View.GONE);
            logBack.setBackgroundColor(Concurrent.getColor(this, R.color.x_login_back));
        }

        if (BypassLogin) {
            loadData();
        } else {
            loginFirst();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean("BypassLogin", BypassLogin);
    }

    public void loginFirst() {

        Prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String appUsername = Prefs.getString("app_username", null);
        String appPassword = Prefs.getString("app_password", null);
        if (appUsername != null && appPassword != null && !appUsername.equals("") && !appPassword.equals("")) {


            FormBody.Builder formBody = new FormBody.Builder();
            formBody.add("username", appUsername);
            formBody.add("password", appPassword);

            String refreshedToken = FirebaseInstanceId.getInstance().getToken();
            if(refreshedToken != null)formBody.add("android_token", refreshedToken);

            OkHttpClient client = new OkHttpClient().newBuilder(SplashPage.this).connectTimeout(7, TimeUnit.SECONDS).build();

            /*Request.Builder requestBuilder = new Request.Builder()
                    .url(App.getAppBaseUrl() + Constants.TASK_LOGIN);*/

            Request.Builder requestBuilder = new Request.Builder()
                    .url(App.TASK_BASE_URL + "upload/auth/authenticate");

            requestBuilder.post(formBody.build());

            Request request = requestBuilder.build();


            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, final IOException e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            if (e instanceof ConnectException) {
                                loginError(null);
                                Toast.makeText(SplashPage.this, Concurrent.getLangSubWords("noConnection", "No Internet Connection"), Toast.LENGTH_LONG).show();
                            } else {
                                if (Concurrent.isFloat(e.getMessage())) {
                                    loginError(e.getMessage());
                                    Log.v(TAG, "OnFailure1: " + e.getMessage());
                                } else {
                                    loginError(null);
                                    Log.v(TAG, "OnFailure2: " + e.getMessage());
                                }
                            }
                        }
                    });
                }

                @Override
                public void onResponse(Call call, final Object serverResponse) {
                    final Response responseObj = (Response) serverResponse;
                    try {
                        response = responseObj.body().string();
                        Log.v(TAG, "OnResponse: " + response);
                    } catch (Exception e) {
                        loginError("5001");
                        return;
                    }

                    if (response != null) {

                        runOnUiThread(new Runnable() {
                            public void run() {
                                try {
                                    if (responseObj.isSuccessful()) {

                                        String token;
                                        JsonParser parser = new JsonParser();
                                        JsonObject ValuesHolder;
                                        try {
                                            ValuesHolder = parser.parse(response).getAsJsonObject();
                                        } catch (Exception e) {
                                            loginError("5001");
                                            return;
                                        }

                                        if (ValuesHolder != null) {
                                            token = Concurrent.tagsStringValidator(ValuesHolder, "token");
                                            if (token != null && token.length() > 1) {
                                                Concurrent.setAppToken(getBaseContext(), token);
                                                loadData();
                                            } else {
                                                loginError("5011");
                                            }
                                        } else {
                                            loginError("5001");
                                        }
                                    } else {
                                        checkErrorType(response);
                                    }
                                } catch (final Exception e) {
                                    loginError("5002");
                                }
                            }
                        });
                    }

                }
            });
        } else {
            Intent MyIntent = new Intent(getBaseContext(), LoginPage.class);
            MyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            MyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(MyIntent);
            overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            finish();
        }
    }

    public void checkErrorType(String response) {
        JsonParser parser = new JsonParser();
        JsonObject ValuesHolder = null;

        try {
            ValuesHolder = parser.parse(response).getAsJsonObject();
        } catch (Exception e) {
            loginError("5001");
        }

        if (ValuesHolder != null) {
            if (ValuesHolder.has("error")) {
                try {
                    String errorObj = Concurrent.tagsStringValidator(ValuesHolder,"error");
                    if (errorObj != null) {
                        if (errorObj.contains("invalid_credentials")) {
                            Toast.makeText(SplashPage.this, Concurrent.getLangSubWords("invalidcredentials", "Invalid Credentials"), Toast.LENGTH_LONG).show();
                            loginError(null);
                        } else {
                            loginError("5005");
                        }
                    } else {
                        loginError("5004");
                    }
                } catch (Exception e) {
                    loginError("5003");
                }
            } else {
                loginError("5008");
            }
        } else {
            loginError("5006");
        }

    }

    public void dashboardError(String errorCode) {
        if(errorCode != null && errorCode.equals("101010")){
            loadData();
            Toast.makeText(this, "Please wait...", Toast.LENGTH_LONG).show();
        }else{
            if (DashboardErrorCounter <= 3) {
                String errorTitle;
                if (errorCode != null) {
                    errorTitle = Concurrent.getLangSubWords("errorOccurred", "Error Occurred") + " ( Error Code: " + errorCode + " )";
                    Log.v(TAG, "dashboardError: " + errorCode);
                    Log.v(TAG, "dashboardError: " + errorTitle);
                } else {
                    errorTitle = Concurrent.getLangSubWords("errorOccurred", "Error Occurred");
                }
                Concurrent.showMessageDialog(SplashPage.this,
                        new BottomDialog.ButtonCallback() {
                            @Override
                            public void onClick(@NonNull BottomDialog bottomDialog) {
                                loadData();
                            }
                        },
                        "error_dialog", errorTitle,
                        Concurrent.getLangSubWords("tapToTryAgain", "Tap to try again"),
                        Concurrent.getLangSubWords("tryAgain", "Try Again")
                );
                DashboardErrorCounter++;
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

    public void loginError(String errorCode) {
        if (LoginErrorCounter <= 3) {
            String errorTitle;
            if (errorCode != null) {
                errorTitle = Concurrent.getLangSubWords("errorOccurred", "Error Occurred") + " ( Error Code: " + errorCode + " - Login Process )";
            } else {
                errorTitle = Concurrent.getLangSubWords("errorOccurred", "Error Occurred");
            }
            Concurrent.showMessageDialog(SplashPage.this,
                    new BottomDialog.ButtonCallback() {
                        @Override
                        public void onClick(@NonNull BottomDialog bottomDialog) {
                            loginFirst();
                        }
                    },
                    "error_dialog", errorTitle,
                    Concurrent.getLangSubWords("tapToTryAgain", "Tap to try again"),
                    Concurrent.getLangSubWords("tryAgain", "Try Again")
            );
            LoginErrorCounter++;
        } else {
            Intent MyIntent = new Intent(getBaseContext(), LoginPage.class);
            MyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            MyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(MyIntent);
            overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            finish();
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }

    public void loadData() {
        if (Concurrent.getAppUsername(getBaseContext()) == null || Concurrent.getAppPassword(getBaseContext()) == null) {
            Intent MyIntent = new Intent(getBaseContext(), LoginPage.class);
            startActivity(MyIntent);
            overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        }

        String TOKEN = Concurrent.getAppToken(this);
        Log.e("vvvvv"," "+TOKEN);
        if (TOKEN != null) {

            FormBody.Builder formBody = new FormBody.Builder();
            formBody.add("nLowAndVersion", "400");

            OkHttpClient client = new OkHttpClient().newBuilder(SplashPage.this).connectTimeout(7, TimeUnit.SECONDS).build();

            Request.Builder requestBuilder = new Request.Builder()
                    .url("https://rootssg.schoolmgmtsys.com/upload/dashaboard");

            requestBuilder.post(formBody.build());

            Request request = requestBuilder.build();


            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, final IOException e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            if (e instanceof ConnectException) {
                                dashboardError(null);
                                Toast.makeText(SplashPage.this, Concurrent.getLangSubWords("noConnection", "No Internet Connection"), Toast.LENGTH_LONG).show();
                            } else {
                                if (Concurrent.isFloat(e.getMessage())) {
                                    dashboardError(e.getMessage());
                                } else {

                                    dashboardError("4001");
                                }
                            }
                        }
                    });
                }

                @Override
                public void onResponse(Call call, final Object serverResponse) {
                    final String response = (String) serverResponse;
                    if (response != null) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                try {
                                    JsonParser parser = new JsonParser();
                                    JsonObject ValuesHolder;
                                    try {
                                        ValuesHolder = parser.parse(response).getAsJsonObject();
                                    } catch (Exception e) {
                                        dashboardError("5001");
                                        return;
                                    }
                                    if (ValuesHolder != null) {
                                        if (ValuesHolder.has("error")) {
                                            if (Concurrent.tagsStringValidator(ValuesHolder, "error").equals("androidNotCompatible")) {

                                                String vNumber = Concurrent.tagsStringValidator(ValuesHolder, "low");
                                                Toast.makeText(SplashPage.this, "Compatibility Error - VNumber:" + vNumber, Toast.LENGTH_LONG).show();

                                                SharedPreferences Prefs = PreferenceManager.getDefaultSharedPreferences(SplashPage.this);
                                                Prefs.edit().remove("app_username").apply();
                                                Prefs.edit().remove("app_password").apply();
                                                Intent MyIntent = new Intent(SplashPage.this, LoginPage.class);
                                                MyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                MyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(MyIntent);
                                                finish();
                                            }
                                        } else {
                                            Intent MyIntent = new Intent(getBaseContext(), DashboardPage.class);
                                            MyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            MyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            MyIntent.putExtra("dashboard_data", new Gson().toJson(ValuesHolder));
                                            startActivity(MyIntent);
                                            overridePendingTransition(R.anim.fadein, R.anim.fadeout);

                                            if (extras != null && extras.containsKey("where")) {
                                                try {
                                                    String notificationWhere = extras.getString("where");
                                                    String notificationID = extras.getString("id");
                                                    Intent intent = MyFirebaseMessagingService.redirectNotification(getBaseContext(), notificationWhere, notificationID);
                                                    startActivity(intent);
                                                }catch (Exception e){

                                                }
                                            }


                                            finish();

                                        }

                                    } else {
                                        dashboardError("5001");
                                    }

                                } catch (final Exception e) {

                                    dashboardError("5002");
                                }
                            }
                        });
                    }

                }
            });
        }
    }

    @Override
    public void getTodayDate(String today) {
        Concurrent.TDate = today;
    }

}
