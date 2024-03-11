package com.schoolmgmtsys.root.ssg.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.processbutton.iml.ActionProcessButton;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.marvinlabs.widget.floatinglabel.edittext.FloatingLabelEditText;
import com.schoolmgmtsys.root.ssg.R;
import com.schoolmgmtsys.root.ssg.messages.MessagesItemsActivity;
import com.schoolmgmtsys.root.ssg.models.UserModel;
import com.schoolmgmtsys.root.ssg.utils.App;
import com.schoolmgmtsys.root.ssg.utils.Concurrent;
import com.schoolmgmtsys.root.ssg.utils.Constants;
import com.solutionsbricks.solbricksframework.Call;
import com.solutionsbricks.solbricksframework.Callback;
import com.solutionsbricks.solbricksframework.FormBody;
import com.solutionsbricks.solbricksframework.OkHttpClient;
import com.solutionsbricks.solbricksframework.Request;
import com.solutionsbricks.solbricksframework.Response;
import com.solutionsbricks.solbricksframework.helpers.CustomImageView;
import com.schoolmgmtsys.root.ssg.utils.DrawerListFragment;
import com.schoolmgmtsys.root.ssg.utils.InputDialog;
import com.solutionsbricks.solbricksframework.helpers.ListManager;
import com.schoolmgmtsys.root.ssg.fonts.ParentStyledTextView;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SearchUserView extends SlidingFragmentActivity implements FloatingLabelEditText.EditTextListener, ListManager.ListInterface {


    private FloatingLabelEditText mSearchInput;
    private String mSearchInputValue;
    private String TOKEN;
    private boolean TokenRetry;
    private ArrayList<UserModel> LIST_DATA = new ArrayList<>();
    private ListManager mListManager;
    private int Res_PageList = R.id.users_view_list;
    private String noMatchesLang;
    private ActionProcessButton mSearchBtn;
    private String response;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_users_search_view);


        ImageView backImage = (ImageView) findViewById(R.id.background_img);
        LinearLayout logBack = (LinearLayout) findViewById(R.id.full_layout);

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

        findViewById(R.id.gen_loader).setVisibility(View.GONE);

        TextView HeadTitle = (TextView) findViewById(R.id.head_drawer_title);
        HeadTitle.setText(Concurrent.getSchoolName(getBaseContext()));

        ImageView ToogleBtn = (ImageView) findViewById(R.id.head_drawer_toggle);
        ToogleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });


        ListView ViewList = (ListView) findViewById(Res_PageList);

        ViewList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        mListManager = new ListManager(this, ViewList, this, LIST_DATA);
        mListManager.removeFooter();

        findViewById(R.id.error_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchUser();
            }
        });
        findViewById(R.id.empty_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchUser();
            }
        });


        mSearchInput = (FloatingLabelEditText) findViewById(R.id.search_input);
        mSearchBtn = (ActionProcessButton) findViewById(R.id.search_btn);
        mSearchInput.setEditTextListener(this);
        noMatchesLang = Concurrent.getLangSubWords("noMatches", "Sorry No Matches");

        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchUser();
            }
        });
    }


    public void searchUser() {
        if (mSearchInputValue != null && !mSearchInputValue.equals("")) {
            mSearchBtn.setEnabled(false);
            mSearchBtn.setProgress(10);

            TOKEN = Concurrent.getAppToken(getBaseContext());
            if (TOKEN != null) {
                String mLink;
                mLink = App.getAppBaseUrl() + Constants.TASK_MESSAGES_SEARCH_USER + "/" + mSearchInputValue;



                OkHttpClient client = new OkHttpClient().newBuilder(getBaseContext()).connectTimeout(7, TimeUnit.SECONDS).build();

                Request.Builder requestBuilder = new Request.Builder()
                        .url(mLink);

                requestBuilder.get();

                Request request = requestBuilder.build();


                Call call = client.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, final IOException e) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                mSearchBtn.setProgress(-1);
                                mSearchBtn.setEnabled(true);
                                if (e instanceof ConnectException) {
                                    Toast.makeText(getBaseContext(), Concurrent.getLangSubWords("noConnection", "No Internet Connection"), Toast.LENGTH_LONG).show();
                                } else {
                                    if (Concurrent.isFloat(e.getMessage())) {
                                        showError(e.getMessage());
                                    } else {
                                        showError("5012");
                                    }
                                }
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, final Object serverResponse) {
                        final Response responseObj = (Response)serverResponse;
                        try {
                            response = responseObj.body().string();
                        } catch (Exception e) {
                            showError("5001");
                            return;
                        }

                        if (response != null) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    try {
                                        if (responseObj.isSuccessful()) {

                                            JsonParser parser = new JsonParser();
                                            JsonObject ValuesHolder = null;

                                            try {
                                                ValuesHolder = parser.parse(response).getAsJsonObject();
                                            } catch (Exception e) {
                                                showError("5001");
                                            }

                                            if (ValuesHolder != null) {
                                                for (Map.Entry<String, JsonElement> entry : ValuesHolder.entrySet()) {
                                                    JsonObject objResult = entry.getValue().getAsJsonObject();

                                                    LIST_DATA.add(new UserModel(
                                                            Concurrent.tagsStringValidator(objResult, "id"),
                                                            Concurrent.tagsStringValidator(objResult, "name"),
                                                            Concurrent.tagsStringValidator(objResult, "email"),
                                                            Concurrent.tagsStringValidator(objResult, "role"),
                                                            Concurrent.tagsStringValidator(objResult, "username")
                                                    ));
                                                }
                                                if (LIST_DATA.size() == 0) {
                                                    Toast.makeText(SearchUserView.this, noMatchesLang, Toast.LENGTH_LONG).show();
                                                    mListManager.setNoDataView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(Res_PageList));
                                                } else {
                                                    mListManager.getListAdapter().notifyDataSetChanged();
                                                }
                                                mSearchBtn.setProgress(100);
                                                mSearchBtn.setEnabled(true);
                                            } else {
                                                mSearchBtn.setProgress(-1);
                                                mSearchBtn.setEnabled(true);
                                                mListManager.setErrorView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(Res_PageList));
                                            }
                                            TokenRetry = false;
                                        } else {
                                            renewToken();
                                        }
                                    } catch (final Exception e) {
                                        showError("5002");
                                    }
                                }
                            });
                        } else {
                            showError("5001");
                            /*
                                mSearchBtn.setProgress(-1);
                                mSearchBtn.setEnabled(true);
                             */
                        }
                    }
                });
            } else {
                mSearchBtn.setProgress(-1);
                mSearchBtn.setEnabled(true);
            }
        } else {
            mSearchBtn.setProgress(-1);
            mSearchBtn.setEnabled(true);
        }
    }

    @Override
    public void onTextChanged(FloatingLabelEditText source, String text) {
        if (source == mSearchInput) {
            mSearchInputValue = text;
        }
    }


    public void renewToken(){
        if (!TokenRetry) {

            FormBody.Builder formBody = new FormBody.Builder();
            formBody.add("username", Concurrent.getAppUsername(getBaseContext()));
            formBody.add("password", Concurrent.getAppPassword(getBaseContext()));

            String refreshedToken = FirebaseInstanceId.getInstance().getToken();            if(refreshedToken != null)formBody.add("android_token", refreshedToken);

            OkHttpClient client = new OkHttpClient().newBuilder(getBaseContext()).connectTimeout(7, TimeUnit.SECONDS).build();

            Request.Builder requestBuilder = new Request.Builder()
                    .url(App.getAppBaseUrl() + Constants.TASK_LOGIN);

            requestBuilder.post(formBody.build());

            Request request = requestBuilder.build();


            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, final IOException e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            mSearchBtn.setProgress(-1);
                            mSearchBtn.setEnabled(true);
                            if (e instanceof ConnectException) {
                                Toast.makeText(getBaseContext(), Concurrent.getLangSubWords("noConnection", "No Internet Connection"), Toast.LENGTH_LONG).show();
                            } else {
                                if (Concurrent.isFloat(e.getMessage())) {
                                    showError(e.getMessage());
                                } else {
                                    showError("5010");
                                }
                            }
                        }
                    });
                }

                @Override
                public void onResponse(Call call, final Object serverResponse) {
                    final Response responseObj = (Response)serverResponse;
                    try {
                        response = responseObj.body().string();
                    } catch (Exception e) {
                        showError("5001");
                        return;
                    }

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
                                            token = Concurrent.tagsStringValidator(ValuesHolder, "token");
                                            if (token != null && token.length() > 1) {

                                                token = Concurrent.tagsStringValidator(ValuesHolder, "token");
                                                Concurrent.setAppToken(getBaseContext(), token);
                                                searchUser();

                                            } else {
                                                showError("5011");
                                            }
                                        } else {
                                            showError("5001");
                                        }
                                    } else {
                                        showError(Concurrent.checkErrorType(getBaseContext(), response));
                                    }
                                } catch (final Exception e) {
                                    showError("5002");
                                }
                            }
                        });
                    }else{
                        showError("5001");
                    }
                    TokenRetry = true;
                }
            });

        } else {
            showError("5010");
            mListManager.setErrorView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(Res_PageList));
        }
    }

    public void showError(String errorCode) {
        String errorTitle;
        if (errorCode != null) {
            errorTitle = Concurrent.getLangSubWords("errorOccurred", "Error Occurred") + " ( Error Code: " + errorCode + " )";
        } else {
            errorTitle = Concurrent.getLangSubWords("errorOccurred", "Error Occurred");
        }
        mSearchBtn.setProgress(-1);
        mSearchBtn.setEnabled(true);
        Toast.makeText(getBaseContext(), errorTitle, Toast.LENGTH_LONG).show();
    }

    @Override
    public void loadMore() {

    }

    @Override
    public void AdapterConstructor() {

    }


    @Override
    public View AdapterGetView(int position, View convertView) {
        LayoutInflater inflater = getLayoutInflater();
        UserHolder holder;
        if (convertView == null || convertView.getTag() == null) {
            holder = new UserHolder();
            convertView = inflater.inflate(R.layout.page_search_user_result_item, null);

            holder.holderView = (RelativeLayout) convertView.findViewById(R.id.holder);
            holder.name = (ParentStyledTextView) convertView.findViewById(R.id.header_title);
            holder.role = (ParentStyledTextView) convertView.findViewById(R.id.header_content);
            holder.username = (ParentStyledTextView) convertView.findViewById(R.id.footer_username_data);
            holder.email = (ParentStyledTextView) convertView.findViewById(R.id.footer_email_data);
            holder.img = (CustomImageView) convertView.findViewById(R.id.header_img);

            convertView.setTag(holder);
        } else {
            holder = (UserHolder) convertView.getTag();
        }
        UserModel posValues = LIST_DATA.get(position);
        if (posValues != null) {

            holder.name.setNotNullText(posValues.name);
            holder.role.setNotNullText(posValues.role);
            holder.username.setNotNullText(posValues.username);
            holder.email.setNotNullText(posValues.email);

            holder.img.profileID = String.valueOf(posValues.id);
            holder.img.load();

            holder.holderView.setTag(posValues);
            holder.holderView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final UserModel userObject = (UserModel) v.getTag();

                    InputDialog.Builder msg = new InputDialog.Builder(SearchUserView.this)
                            .setTitle("Write message to send")
                            .setInputHint("message")
                            .setPositiveButton("Send", new InputDialog.ButtonActionListener() {
                                public SharedPreferences Prefs;

                                @Override
                                public void onClick(CharSequence inputText) {
                                    String TOKEN = Concurrent.getAppToken(getBaseContext());
                                    if (TOKEN != null) {

                                        Toast.makeText(getBaseContext(), "Sending...", Toast.LENGTH_LONG).show();


                                        FormBody.Builder formBody = new FormBody.Builder();
                                        formBody.add("messageText", String.valueOf(inputText));
                                        formBody.add("toId", userObject.username);


                                        OkHttpClient client = new OkHttpClient().newBuilder(getBaseContext()).connectTimeout(7, TimeUnit.SECONDS).build();

                                        Request.Builder requestBuilder = new Request.Builder()
                                                .url(App.getAppBaseUrl() + Constants.TASK_MESSAGES);

                                        requestBuilder.post(formBody.build());

                                        Request request = requestBuilder.build();


                                        Call call = client.newCall(request);
                                        call.enqueue(new Callback() {
                                            @Override
                                            public void onFailure(Call call, final IOException e) {
                                                runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        if (e instanceof ConnectException) {
                                                            Toast.makeText(getBaseContext(), Concurrent.getLangSubWords("noConnection", "No Internet Connection"), Toast.LENGTH_LONG).show();
                                                        } else {
                                                            if (Concurrent.isFloat(e.getMessage())) {
                                                                showError(e.getMessage());
                                                            } else {
                                                                showError("5012");
                                                            }
                                                        }
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onResponse(Call call, final Object serverResponse) {
                                                final Response responseObj = (Response)serverResponse;
                                                try {
                                                    response = responseObj.body().string();
                                                } catch (Exception e) {
                                                    showError("5001");
                                                    return;
                                                }

                                                if (response != null) {
                                                    runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            try {
                                                                if (responseObj.isSuccessful()) {

                                                                    JsonParser parser = new JsonParser();
                                                                    JsonObject ValuesHolder = null;

                                                                    try {
                                                                        ValuesHolder = parser.parse(response).getAsJsonObject();
                                                                    } catch (Exception e) {
                                                                        showError("5001");
                                                                    }

                                                                    if (ValuesHolder != null) {
                                                                        String messageId = Concurrent.tagsStringValidator(ValuesHolder, "messageId");
                                                                        if(messageId != null && !messageId.equals("")){
                                                                            Intent MyIntent = new Intent(getBaseContext(), MessagesItemsActivity.class);
                                                                            MyIntent.putExtra("msg_id", messageId);
                                                                            startActivity(MyIntent);
                                                                            overridePendingTransition(R.anim.fadein, R.anim.fadeout);

                                                                        }else{
                                                                            Toast.makeText(getBaseContext(), Concurrent.getLangSubWords("errorOccurred","Error Occurred"), Toast.LENGTH_LONG).show();
                                                                        }
                                                                    }else{
                                                                        Toast.makeText(getBaseContext(), Concurrent.getLangSubWords("errorOccurred","Error Occurred"), Toast.LENGTH_LONG).show();
                                                                    }
                                                                }
                                                            } catch (final Exception e) {
                                                                showError("5002");
                                                            }
                                                        }
                                                    });
                                                } else {
                                                    showError("5001");
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                    msg.show();
                }
            });

        }
        return convertView;
    }

    private class UserHolder {

        public RelativeLayout holderView;
        public ParentStyledTextView name;
        public ParentStyledTextView role;
        public ParentStyledTextView username;
        public ParentStyledTextView email;
        public CustomImageView img;
    }

}
