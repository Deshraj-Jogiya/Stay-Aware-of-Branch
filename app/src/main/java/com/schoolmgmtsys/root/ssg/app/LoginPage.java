package com.schoolmgmtsys.root.ssg.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.processbutton.iml.ActionProcessButton;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.marvinlabs.widget.floatinglabel.edittext.FloatingLabelEditText;
import com.schoolmgmtsys.root.ssg.R;
import com.schoolmgmtsys.root.ssg.utils.App;
import com.schoolmgmtsys.root.ssg.utils.Concurrent;
import com.schoolmgmtsys.root.ssg.utils.Constants;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.solutionsbricks.solbricksframework.Call;
import com.solutionsbricks.solbricksframework.Callback;
import com.solutionsbricks.solbricksframework.FormBody;
import com.solutionsbricks.solbricksframework.OkHttpClient;

import com.solutionsbricks.solbricksframework.Request;


import com.solutionsbricks.solbricksframework.SolUtils;
import com.yarolegovich.lovelydialog.LovelyChoiceDialog;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class LoginPage extends Activity implements FloatingLabelEditText.EditTextListener {

    private FloatingLabelEditText LogUsername;
    private FloatingLabelEditText LogPassword;
    private ActionProcessButton LogSignIn;
    private RelativeLayout InputsLayout;
    private SharedPreferences Prefs;
    private String LogUsernameValue;
    private String LogPasswordValue;
    private String response = null;
    private LovelyChoiceDialog demoLoginDialog;
    private Boolean isDemoLoginEnabled = true;
    String refreshedToken2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_login);
        refreshedToken2 = FirebaseInstanceId.getInstance().getToken();


        Prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        InputsLayout = (RelativeLayout) findViewById(R.id.log_inputs_layout);
        LogUsername = (FloatingLabelEditText) findViewById(R.id.log_inputs_user);
        LogPassword = (FloatingLabelEditText) findViewById(R.id.log_inputs_pass);
        LogSignIn = (ActionProcessButton) findViewById(R.id.log_inputs_signBtn);

        LogUsername.setLabelText(Concurrent.getLangSubWordsFromPrefs(getBaseContext(),"userNameOrEmail_trans", "Username/Email"));
        LogPassword.setLabelText(Concurrent.getLangSubWordsFromPrefs(getBaseContext(),"password_trans", "Password"));


        RelativeLayout logBack = (RelativeLayout) findViewById(R.id.log_full_layout);
        ImageView backImage = (ImageView) findViewById(R.id.background_img);


        Resources res = getResources();
        if (!res.getBoolean(R.bool.x_login_back_is_image)) {
            backImage.setVisibility(View.GONE);
            logBack.setBackgroundColor(Concurrent.getColor(this, R.color.x_login_back));
        }

        LogUsername.setEditTextListener(this);
        LogPassword.setEditTextListener(this);

        LogSignIn.setMode(ActionProcessButton.Mode.ENDLESS);
        LogSignIn.setText(Concurrent.getLangSubWordsFromPrefs(getBaseContext(),"signIn_trans", "Sign in"));
        InputsLayout.setVisibility(View.VISIBLE);

        LogSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
            }
        });



        List<DemoLoginAdapterOption> accountsList = new ArrayList<>();
        accountsList.add(new DemoLoginAdapterOption("Admin","admin","0101123"));

          ArrayAdapter<DemoLoginAdapterOption> adapter = new DemoLoginAdapter(this,accountsList);
        demoLoginDialog = new LovelyChoiceDialog(this)
                .setTopColorRes(R.color.green_dark)
                .setTitle("")
                .setIcon(R.drawable.icon_pages_pass)
                .setItems(adapter, new LovelyChoiceDialog.OnItemSelectedListener<DemoLoginAdapterOption>() {
                    @Override
                    public void onItemSelected(int position, DemoLoginAdapterOption item) {
                        LogUsername.setInputWidgetText(item.uname);
                        LogUsernameValue = item.uname;

                        LogPassword.setInputWidgetText(item.pass);
                        LogPasswordValue = item.pass;
                        //new uploadToServer().execute();

                        performLogin();
                        //performLogin2();
                    }
                })
                .setSavedInstanceState(savedInstanceState);




        ((TextView)findViewById(R.id.privacy_layout_text)).setText(Concurrent.getLangSubWords("privacy_policy",""));
        findViewById(R.id.demo_login_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                demoLoginDialog.show();
            }
        });
        findViewById(R.id.privacy_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(App.getAppBaseUrl()+ Constants.TASK_PRIVACY));
                startActivity(browserIntent);
            }
        });

        if(isDemoLoginEnabled){
           // findViewById(R.id.demo_login_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.demo_login_layout).setVisibility(View.GONE);
        }else{
            findViewById(R.id.demo_login_layout).setVisibility(View.GONE);
        }
    }


    public class uploadToServer extends AsyncTask<Void, Void, String> {

        private ProgressDialog pd = new ProgressDialog(LoginPage.this);
        protected void onPreExecute() {
            super.onPreExecute();
            pd.setMessage("Please Wait,receipt uploading...");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(Void... params) {


            try {
                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("username", LogUsernameValue));
                nameValuePairs.add(new BasicNameValuePair("password", LogPasswordValue));


                Log.e("parameters"," "+nameValuePairs);

                HttpClient httpclient = new DefaultHttpClient();

                HttpPost httppost = new HttpPost("https://rootssg.schoolmgmtsys.com/index.php/auth/authenticate/?android_token="+refreshedToken2);
                httppost.setHeader("Authorization","application/json");
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = httpclient.execute(httppost);
                String st = EntityUtils.toString(response.getEntity());

                Log.e("log_tag", " " + st);

              /*  if(jsonObject.getString("status").equals("success")){

                    LoginPage.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(LoginPage.this, "Receipt Uploded Successfully", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
*/
            } catch (Exception e) {
                Log.v("log_tag", "Error in http connection " + e.toString());
            }
            return "Success";
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            pd.hide();
            pd.dismiss();
        }
    }

    /*private void performLogin2() {
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            String URL = App.getAppBaseUrl() + Constants.TASK_LOGIN;
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("username", LogUsernameValue);
            jsonBody.put("password", LogPasswordValue);
            jsonBody.put("android_token", refreshedToken2);

            Log.e("VOLLEY", "" +jsonBody);

            final String requestBody = jsonBody.toString();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.e("my_response"," "+response);
                    try {
                        if (response!=null) {


                        } else {
                            checkErrorType(response);
                        }
                    } catch (final Exception e) {
                        showError("5002");
                    }
                }

            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("VOLLEY", error.toString());
                }
            });
         *//*   {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return requestBody == null ? null : requestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                        return null;
                    }
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    String responseString = "";
                    if (response != null) {
                        responseString = String.valueOf(response.statusCode);
                        // can get more details such as response.headers
                    }
                    return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                }


            };*//*

            requestQueue.add(stringRequest);
            stringRequest.setRetryPolicy(new RetryPolicy() {
                @Override
                public int getCurrentTimeout() {
                    return 50000;
                }

                @Override
                public int getCurrentRetryCount() {
                    return 50000;
                }

                @Override
                public void retry(VolleyError error) throws VolleyError {

                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }*/


    public void performLogin(){
        LogSignIn.setEnabled(false);
        LogSignIn.setProgress(10);

        if (LogUsernameValue != null && LogPasswordValue != null) {

            FormBody.Builder formBody = new FormBody.Builder();
            formBody.add("username", LogUsernameValue);
            formBody.add("password", LogPasswordValue);


            String refreshedToken = FirebaseInstanceId.getInstance().getToken();
            if(refreshedToken != null)formBody.add("android_token", refreshedToken);
            Log.e("asxasxasxas"," "+refreshedToken);

            OkHttpClient client = new OkHttpClient().newBuilder(LoginPage.this).connectTimeout(7, TimeUnit.SECONDS).build();

           /* Request.Builder requestBuilder = new Request.Builder()
                    .url(App.getAppBaseUrl() + Constants.TASK_LOGIN);*/

            Request.Builder requestBuilder = new Request.Builder()
                    .url(App.TASK_BASE_URL + "upload/auth/authenticate");
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
                            LogSignIn.setEnabled(true);
                            LogSignIn.setProgress(-1);
                            if (e instanceof ConnectException) {
                                LogSignIn.setText(Concurrent.getLangSubWords("noConnection", "No Internet Connection"));
                                Toast.makeText(LoginPage.this, Concurrent.getLangSubWords("noConnection", "No Internet Connection"), Toast.LENGTH_LONG).show();
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
                                    if (responseObj.isSuccessful()) {

                                        String token;
                                        JsonParser parser = new JsonParser();
                                        JsonObject ValuesHolder = null;

                                        try {
                                            ValuesHolder = parser.parse(response).getAsJsonObject();
                                        } catch (Exception e) {
                                            showError("5001");
                                        }

                                        if (ValuesHolder != null) {
                                            if(ValuesHolder.has("policy")&& !Concurrent.tagsStringValidator(ValuesHolder, "policy").equalsIgnoreCase("")){

                                                token = Concurrent.tagsStringValidator(ValuesHolder, "token");
                                                if (token != null && token.length() > 1) {
                                                    Concurrent.setAppToken(getBaseContext(), token);

                                                    LogSignIn.setProgress(100);
                                                    LogSignIn.setEnabled(true);

                                                   /* Prefs.edit().putString("app_username", LogUsernameValue).apply();
                                                    Concurrent.APP_USERNAME = LogUsernameValue;
                                                    Prefs.edit().putString("app_password", LogPasswordValue).apply();
                                                    Concurrent.APP_PASS = LogPasswordValue;
*/

                                                    LogSignIn.setProgress(100);
                                                    LogSignIn.setEnabled(true);


                                                    Intent MyIntent = new Intent(getBaseContext(), PolicyScreen.class);
                                                  MyIntent.putExtra("app_username",LogUsernameValue);
                                                  MyIntent.putExtra("app_password",LogPasswordValue);
                                                  MyIntent.putExtra("policy_text", Concurrent.tagsStringValidator(ValuesHolder, "policy"));
                                                  MyIntent.putExtra("userId", Concurrent.tagsStringValidator(ValuesHolder, "user_id"));
                                                   // MyIntent.putExtra("bypass_login", true);
                                                    startActivity(MyIntent);
                                                    overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                                                    finish();
                                                }else{
                                                    showError("5011");
                                                }



                                            }else {


                                                token = Concurrent.tagsStringValidator(ValuesHolder, "token");
                                                if (token != null && token.length() > 1) {
                                                    Concurrent.setAppToken(getBaseContext(), token);

                                                    LogSignIn.setProgress(100);
                                                    LogSignIn.setEnabled(true);

                                                    Prefs.edit().putString("app_username", LogUsernameValue).apply();
                                                    Concurrent.APP_USERNAME = LogUsernameValue;
                                                    Prefs.edit().putString("app_password", LogPasswordValue).apply();
                                                    Concurrent.APP_PASS = LogPasswordValue;


                                                    LogSignIn.setProgress(100);
                                                    LogSignIn.setEnabled(true);


                                                    Intent MyIntent = new Intent(getBaseContext(), SplashPage.class);
                                                    MyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    MyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                    MyIntent.putExtra("bypass_login", true);
                                                    startActivity(MyIntent);
                                                    overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                                                    finish();
                                                }else{
                                                    showError("5011");
                                                }

                                            }

                                        } else {
                                            showError("5001");
                                        }
                                    } else {
                                        checkErrorType(response);
                                    }
                                } catch (final Exception e) {
                                    showError("5002");
                                }
                            }
                        });
                    }

                }
            });
        } else {

            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(LoginPage.this, "Please Fill Required Data", Toast.LENGTH_LONG).show();
                    LogSignIn.setEnabled(true);
                    LogSignIn.setText(Concurrent.getLangSubWordsFromPrefs(getBaseContext(),"signIn_trans", "Sign in"));
                    LogSignIn.setProgress(-1);
                }
            });
        }
    }

    public void checkErrorType(String response) {
        JsonParser parser = new JsonParser();
        JsonObject ValuesHolder = null;

        try {
            ValuesHolder = parser.parse(response).getAsJsonObject();
        } catch (Exception e) {
            showError("5001");
        }

        if (ValuesHolder != null) {
            if (ValuesHolder.has("error")) {
                try {
                    String errorObj = Concurrent.tagsStringValidator(ValuesHolder,"error");
                    if (errorObj != null) {
                        if (errorObj.contains("invalid_credentials")) {
                            Toast.makeText(LoginPage.this, Concurrent.getLangSubWords("invalidcredentials", "Invalid Credentials"), Toast.LENGTH_LONG).show();
                            LogSignIn.setEnabled(true);
                            LogSignIn.setProgress(-1);
                            LogSignIn.setText(Concurrent.getLangSubWords("invalidcredentials", "Invalid Credentials"));
                        } else {
                            showError("5005");
                        }
                    } else {
                        showError("5004");
                    }
                } catch (Exception e) {
                    showError("5003");
                }
            } else {
                showError("5008");
            }
        } else {
            showError("5006");
        }

    }

    public void showError(final String errorCode) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(LoginPage.this, Concurrent.getLangSubWords("errorOccurred", "Error Occurred") + " ( Error Code: " + errorCode + " - Login Process )", Toast.LENGTH_LONG).show();
                LogSignIn.setEnabled(true);
                LogSignIn.setProgress(-1);
                LogSignIn.setText(Concurrent.getLangSubWords("errorOccurred", "Error Occurred"));
            }
        });

    }

    @Override
    public void onTextChanged(FloatingLabelEditText source, String text) {
        if (source == LogUsername) {
            LogUsernameValue = text;
        } else if (source == LogPassword) {
            LogPasswordValue = text;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SolUtils.ovUpEnded = false;
    }
}
