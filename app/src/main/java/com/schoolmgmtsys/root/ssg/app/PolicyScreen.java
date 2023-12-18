package com.schoolmgmtsys.root.ssg.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.javiersantos.bottomdialogs.BottomDialog;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.schoolmgmtsys.root.ssg.Notifications.MyFirebaseMessagingService;
import com.schoolmgmtsys.root.ssg.R;
import com.schoolmgmtsys.root.ssg.utils.App;
import com.schoolmgmtsys.root.ssg.utils.Concurrent;
import com.schoolmgmtsys.root.ssg.utils.Constants;
import com.solutionsbricks.solbricksframework.Call;
import com.solutionsbricks.solbricksframework.Callback;
import com.solutionsbricks.solbricksframework.FormBody;
import com.solutionsbricks.solbricksframework.OkHttpClient;
import com.solutionsbricks.solbricksframework.Request;
import com.solutionsbricks.solbricksframework.Response;

import java.io.IOException;
import java.net.ConnectException;
import java.util.concurrent.TimeUnit;


public class PolicyScreen extends AppCompatActivity {

    private Integer Res_PageLayout = R.layout.page_policy_screen;
    public static final String TAG = PolicyScreen.class.getSimpleName();
private String LogUsernameValue;
private String LogPasswordValue;
private String PolicyText;
private String userId;
    private ImageView img_back;
    private TextView policyText;
    private Integer LoginErrorCounter = 1;
    private RelativeLayout Status_0,Status_1;
    private SharedPreferences Prefs;
    private String response;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);




        setContentView(Res_PageLayout);
        Bundle extras = getIntent().getExtras();
        Prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if(extras!=null){

            LogUsernameValue = extras.getString("app_username");
            LogPasswordValue = extras.getString("app_password");
            PolicyText = extras.getString("policy_text");
            userId = extras.getString("userId");

        }


        //img_view_receipt=(ImageView)findViewById(R.id.img_view_receipt);
        Status_0=(RelativeLayout) findViewById(R.id.status_0);
        Status_1=(RelativeLayout) findViewById(R.id.status_1);

        Status_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences Prefs = PreferenceManager.getDefaultSharedPreferences(PolicyScreen.this);
                Prefs.edit().remove("app_username").apply();
                Prefs.edit().remove("app_password").apply();
                Intent MyIntent = new Intent(PolicyScreen.this, LoginPage.class);
                MyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                MyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                PolicyScreen.this.startActivity(MyIntent);
                ((Activity) PolicyScreen.this).finish();
            }
        });



        Status_0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadData("1");
            }
        });

        policyText=(TextView) findViewById(R.id.policy_text);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            policyText.setText(Html.fromHtml(PolicyText,Html.FROM_HTML_MODE_LEGACY));
        } else {
            policyText.setText(Html.fromHtml(PolicyText));
        }

     //   policyText.setText(PolicyText);
        img_back=(ImageView)findViewById(R.id.img_back);
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }










    public void loadData(String status) {

        String TOKEN = Concurrent.getAppToken(PolicyScreen.this);
        Log.e("vvvvv"," "+TOKEN);
        if (TOKEN != null) {

            FormBody.Builder formBody = new FormBody.Builder();
            formBody.add("is_accepted", status);
            formBody.add("user_id", userId);


            String refreshedToken = FirebaseInstanceId.getInstance().getToken();
            if(refreshedToken != null)formBody.add("android_token", refreshedToken);
            Log.e("asxasxasxas"," "+refreshedToken);

            OkHttpClient client = new OkHttpClient().newBuilder(PolicyScreen.this).connectTimeout(7, TimeUnit.SECONDS).build();

           /* Request.Builder requestBuilder = new Request.Builder()
                    .url(App.getAppBaseUrl() + Constants.TASK_LOGIN);*/

            Request.Builder requestBuilder = new Request.Builder()
                    .url(App.TASK_BASE_URL + "upload/auth/authenticate/policy");
            Log.v("mtag","dddd "+App.getAppBaseUrl() + Constants.TASK_LOGIN);

            requestBuilder.post(formBody.build());

            Request request = requestBuilder.build();

            Call call = client.newCall(request);


            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, final IOException e) {
                    Log.v("mtag","eeew "+e.getMessage());
                    runOnUiThread(new Runnable() {
                        public void run() {
                           // LogSignIn.setEnabled(true);
                            //LogSignIn.setProgress(-1);
                            if (e instanceof ConnectException) {
                                //LogSignIn.setText(Concurrent.getLangSubWords("noConnection", "No Internet Connection"));
                                Toast.makeText(PolicyScreen.this, Concurrent.getLangSubWords("noConnection", "No Internet Connection"), Toast.LENGTH_LONG).show();
                            } else {
                                if(Concurrent.isFloat(e.getMessage())){
                                    showError(e.getMessage());
                                }else{
                                    showError("5010");
                                }
                            }
                        }
                    });
                }




                @Override
                public void onResponse(Call call, final Object serverResponse) {
                   // final String response = (String) serverResponse;

                    final com.solutionsbricks.solbricksframework.Response responseObj = (com.solutionsbricks.solbricksframework.Response)serverResponse;

                    try {
                        response = responseObj.body().string();
                    } catch (Exception e) {
                        showError("5001");
                        return;
                    }
                    Log.v("mtag","vvv "+response);


                    if (response != null) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                try {
                                    JsonParser parser = new JsonParser();
                                    JsonObject ValuesHolder;
                                    try {
                                        ValuesHolder = parser.parse(response).getAsJsonObject();
                                    } catch (Exception e) {
                                        Toast.makeText(PolicyScreen.this, "5001", Toast.LENGTH_LONG).show();
//                                        dashboardError("5001");
                                        return;
                                    }
                                    if (ValuesHolder != null) {




                                                Prefs.edit().putString("app_username", LogUsernameValue).apply();
                                                Concurrent.APP_USERNAME = LogUsernameValue;
                                                Prefs.edit().putString("app_password", LogPasswordValue).apply();
                                                Concurrent.APP_PASS = LogPasswordValue;




                                                Intent MyIntent = new Intent(getBaseContext(), SplashPage.class);
                                                MyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                MyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                MyIntent.putExtra("bypass_login", true);
                                                startActivity(MyIntent);
                                                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                                                finish();




                                    } else {
                                        Toast.makeText(PolicyScreen.this, "5001", Toast.LENGTH_LONG).show();

//                                        dashboardError("5001");
                                    }

                                } catch (final Exception e) {
                                    Toast.makeText(PolicyScreen.this, "5002", Toast.LENGTH_LONG).show();

//                                    dashboardError("5002");
                                }
                            }
                        });
                    }

                }
            });
        }
    }



    public void showError(final String errorCode) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(PolicyScreen.this, Concurrent.getLangSubWords("errorOccurred", "Error Occurred") + " ( Error Code: " + errorCode + " - Login Process )", Toast.LENGTH_LONG).show();

            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
