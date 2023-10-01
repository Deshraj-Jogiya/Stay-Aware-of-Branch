package com.schoolmgmtsys.root.ssg.messages;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jeremyfeinstein.slidingmenu.lib.app.MyContextWrapper;
import com.schoolmgmtsys.root.ssg.R;
import com.schoolmgmtsys.root.ssg.app.ControlActivity;
import com.schoolmgmtsys.root.ssg.app.SearchUserView;

import com.schoolmgmtsys.root.ssg.utils.App;
import com.schoolmgmtsys.root.ssg.utils.Concurrent;
import com.schoolmgmtsys.root.ssg.utils.Constants;
import com.solutionsbricks.solbricksframework.Call;
import com.solutionsbricks.solbricksframework.Callback;
import com.solutionsbricks.solbricksframework.FormBody;
import com.solutionsbricks.solbricksframework.OkHttpClient;
import com.solutionsbricks.solbricksframework.Request;
import com.solutionsbricks.solbricksframework.Response;
import com.solutionsbricks.solbricksframework.messages.model.Dialog;
import com.solutionsbricks.solbricksframework.messages.model.Message;
import com.solutionsbricks.solbricksframework.messages.model.User;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MessagesDialogsActivity extends BaseDialogsActivity {

    private String TOKEN;
    private boolean TokenRetry;
    private int nextPage = 1;
    private ArrayList<Dialog> messagesList = new ArrayList<>();

    private DialogsList dialogsList;
    private String response;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messages_dialogs_main);

        dialogsList = (DialogsList) findViewById(R.id.dialogsList);

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey("EXTRA_LIST")) {
            ArrayList<Dialog> messagesDialogSearchResult = extras.getParcelableArrayList("EXTRA_LIST");
            if (messagesDialogSearchResult != null && messagesDialogSearchResult.size() > 0) {
                messagesList.clear();
                messagesList.addAll(messagesDialogSearchResult);
                initAdapter();
                setDialogListView();
                findViewById(R.id.search).setVisibility(View.GONE);
            } else {
                setEmptyView();
            }
        } else {
            loadMessagesDialogs(nextPage);
            setLoadingView();
        }

        setViewsClickListener();

        // =========== Start new conversation ==========//
        ImageView mAdd = (ImageView) findViewById(R.id.add);
        mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent MyIntent = new Intent(getBaseContext(), SearchUserView.class);
                startActivity(MyIntent);
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            }
        });

        ImageView mRefresh = (ImageView) findViewById(R.id.refresh);
        mRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextPage = 1;
                loadMessagesDialogs(nextPage);
                setLoadingView();
            }
        });
        TextView HeadTitle = (TextView) findViewById(R.id.head_drawer_title);
        HeadTitle.setText(Concurrent.getLangSubWords("messages", "Messages"));

        ImageView mSearch = (ImageView) findViewById(R.id.search);
        mSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (messagesList != null && messagesList.size() > 0) {
                    Intent MyIntent = new Intent(getBaseContext(), ControlActivity.class);
                    MyIntent.putExtra("TARGET_FRAGMENT", "SearchView");
                    MyIntent.putExtra("EXTRA_STRING_1", "MessagesPage");
                    MyIntent.putExtra("EXTRA_HEAD_FIND_WORD", "Search");
                    MyIntent.putExtra("EXTRA_HEAD_REPLACE_WORD", "Search");
                    MyIntent.putExtra("EXTRA_LIST", messagesList);
                    startActivity(MyIntent);
                    overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                }
            }
        });

        findViewById(R.id.head_drawer_toggle).setVisibility(View.GONE);
        findViewById(R.id.gen_loader).setVisibility(View.GONE);

    }

    @Override
    protected void onStart() {
        super.onStart();
        Locale locale = new Locale(Concurrent.getLangDirection(getBaseContext()));
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(MyContextWrapper.wrap(newBase, Concurrent.getLangDirection(newBase)));
    }


    public void loadMessagesDialogs(final Integer page) {
        TOKEN = Concurrent.getAppToken(this);
        if (TOKEN != null) {
            String mLink;
            mLink = App.getAppBaseUrl() + Constants.TASK_MESSAGES_DIALOGS_LIST + "/" + page;


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
                            setErrorView();
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
                    final Response responseObj = (Response) serverResponse;
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
                                        JsonObject CurrObj;
                                        JsonArray ValuesArray;

                                        try {
                                            ValuesHolder = parser.parse(response).getAsJsonObject();
                                        } catch (Exception e) {
                                            showError("5001");
                                        }

                                        if (ValuesHolder != null) {

                                            ValuesArray = ValuesHolder.getAsJsonArray("messages");

                                            if (ValuesArray == null) {
                                                setErrorView();
                                                return;
                                            }
                                            if (ValuesArray.size() > 0) {

                                                if (page == null || page == 1) messagesList.clear();

                                                Iterator<JsonElement> ValuesIterator = ValuesArray.iterator();
                                                String msgID;
                                                String msgLastTextSentDate;
                                                String msgLastTextSent;
                                                String destinationUserName;
                                                String destinationUserID;
                                                Integer senderUserID;
                                                Integer msgStatus;

                                                User myUser = new User(
                                                        String.valueOf(Concurrent.getAppUserID(getBaseContext())),
                                                        Concurrent.getAppUsername(getBaseContext()),
                                                        App.getAppBaseUrl() + Constants.TASK_PROFILE_IMG + "/" + Concurrent.getAppUserID(getBaseContext()),
                                                        false);


                                                while (ValuesIterator.hasNext()) {
                                                    CurrObj = ValuesIterator.next().getAsJsonObject();
                                                    msgID = Concurrent.tagsStringValidator(CurrObj, "id");
                                                    msgLastTextSentDate = Concurrent.tagsStringValidator(CurrObj, "lastMessageDate");
                                                    msgLastTextSent = Concurrent.tagsStringValidator(CurrObj, "lastMessage");
                                                    destinationUserID = Concurrent.tagsStringValidator(CurrObj, "userId");
                                                    destinationUserName = Concurrent.tagsStringValidator(CurrObj, "fullName");
                                                    msgStatus = Concurrent.tagsIntValidator(CurrObj, "messageStatus");
                                                    senderUserID = Concurrent.tagsIntValidator(CurrObj, "userId");

                                                    User destinationUser = new User(
                                                            destinationUserID,
                                                            destinationUserName,
                                                            App.getAppBaseUrl() + Constants.TASK_PROFILE_IMG + "/" + destinationUserID,
                                                            false);

                                                    ArrayList<User> mUsers = new ArrayList<>();
                                                    mUsers.add(destinationUser);
                                                    mUsers.add(myUser);


                                                    messagesList.add(
                                                            new Dialog(
                                                                    msgID,
                                                                    destinationUserName,
                                                                    App.getAppBaseUrl() + Constants.TASK_PROFILE_IMG + "/" + destinationUserID,
                                                                    mUsers,
                                                                    new Message(
                                                                            msgID,
                                                                            null,
                                                                            msgLastTextSent,
                                                                            msgLastTextSentDate,
                                                                            null),
                                                                    msgStatus == 1 ? 1 : 0)
                                                    );

                                                }
                                                Collections.reverse(messagesList);
                                                initAdapter();
                                                nextPage = Integer.valueOf(page + 1);
                                                setDialogListView();
                                            } else {
                                                setEmptyView();
                                            }
                                        } else {
                                            setErrorView();
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
                    }
                }
            });
        } else {
            setErrorView();
        }
    }


    public void renewToken() {
        if (!TokenRetry) {

            FormBody.Builder formBody = new FormBody.Builder();
            formBody.add("username", Concurrent.getAppUsername(getBaseContext()));
            formBody.add("password", Concurrent.getAppPassword(getBaseContext()));

            String refreshedToken = FirebaseInstanceId.getInstance().getToken();
            if (refreshedToken != null) formBody.add("android_token", refreshedToken);

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
                            setErrorView();
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
                    final Response responseObj = (Response) serverResponse;
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
                                                TokenRetry = true;
                                                nextPage = 1;
                                                loadMessagesDialogs(nextPage);
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
                    } else {
                        showError("5001");
                    }
                    TokenRetry = true;
                }
            });

        } else {
            showError("5010");
            setErrorView();
        }
    }

    public void showError(String errorCode) {
        String errorTitle;
        if (errorCode != null) {
            errorTitle = Concurrent.getLangSubWords("errorOccurred", "Error Occurred") + " ( Error Code: " + errorCode + " )";
        } else {
            errorTitle = Concurrent.getLangSubWords("errorOccurred", "Error Occurred");
        }
        setErrorView();
        Toast.makeText(getBaseContext(), errorTitle, Toast.LENGTH_LONG).show();
    }


    @Override
    public void onDialogClick(Dialog dialog) {
        Intent MyIntent = new Intent(getBaseContext(), MessagesItemsActivity.class);
        MyIntent.putExtra("msg_id", dialog.getId());
        startActivity(MyIntent);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }

    private void initAdapter() {
        super.dialogsAdapter = new DialogsListAdapter<>(R.layout.messages_dialog, super.imageLoader);
        super.dialogsAdapter.setItems(messagesList);
        super.dialogsAdapter.setOnDialogClickListener(this);
        super.dialogsAdapter.setOnDialogLongClickListener(this);

        dialogsList.setAdapter(super.dialogsAdapter);
    }

    public void setViewsClickListener() {
        View.OnClickListener mListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextPage = 1;
                loadMessagesDialogs(nextPage);
                setLoadingView();
            }
        };

        findViewById(R.id.empty_view).setOnClickListener(mListener);
        findViewById(R.id.error_view).setOnClickListener(mListener);
    }

    public void setLoadingView() {
        findViewById(R.id.dialog_loader_con).setVisibility(View.VISIBLE);
        findViewById(R.id.dialogsList).setVisibility(View.GONE);
        findViewById(R.id.error_view).setVisibility(View.GONE);
        findViewById(R.id.empty_view).setVisibility(View.GONE);
    }

    public void setDialogListView() {
        findViewById(R.id.dialog_loader_con).setVisibility(View.GONE);
        findViewById(R.id.dialogsList).setVisibility(View.VISIBLE);
        findViewById(R.id.error_view).setVisibility(View.GONE);
        findViewById(R.id.empty_view).setVisibility(View.GONE);

    }

    public void setErrorView() {
        findViewById(R.id.dialog_loader_con).setVisibility(View.GONE);
        findViewById(R.id.dialogsList).setVisibility(View.GONE);
        findViewById(R.id.error_view).setVisibility(View.VISIBLE);
        findViewById(R.id.empty_view).setVisibility(View.GONE);

    }

    public void setEmptyView() {
        findViewById(R.id.dialog_loader_con).setVisibility(View.GONE);
        findViewById(R.id.dialogsList).setVisibility(View.GONE);
        findViewById(R.id.error_view).setVisibility(View.GONE);
        findViewById(R.id.empty_view).setVisibility(View.VISIBLE);

    }

}
