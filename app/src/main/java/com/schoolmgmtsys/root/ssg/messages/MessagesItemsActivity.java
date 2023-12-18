package com.schoolmgmtsys.root.ssg.messages;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jeremyfeinstein.slidingmenu.lib.app.MyContextWrapper;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
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
import com.solutionsbricks.solbricksframework.helpers.CustomImageView;
import com.solutionsbricks.solbricksframework.messages.model.Message;
import com.solutionsbricks.solbricksframework.messages.model.User;
import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class MessagesItemsActivity extends BaseMessagesActivity
        implements MessageInput.InputListener,
        MessagesListAdapter.OnMessageLongClickListener<Message>,
        MessagesListAdapter.OnLoadMoreListener {

    private String TOKEN;
    private boolean TokenRetry;
    private User myUser;
    private String talkToUserID;
    private String talkToUserFullName;
    private String msgDialogID = "0";
    private String currentMsgText;
    private MessageInput messageInput;
    private User destinationUser;
    private String lastMsgTimeStamp;
    private Handler checkNewMessagesHandler;
    private Runnable checkNewMessagesRunnable;
    private boolean checkNewMessagesLocked;


    private MessagesList messagesListView;
    private List<Message> messagesListData = new ArrayList<>();
    private String response;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messages_layout_message);

        setViewsClickListener();

        messagesListView = (MessagesList) findViewById(R.id.messagesList);
        messageInput = (MessageInput) findViewById(R.id.input);
        messageInput.setInputListener(this);
        messageInput.changeButtonStatus(MessageInput.sendButtonStatus.DISABLED);


        myUser = new User(
                "0",
                Concurrent.getAppUsername(getBaseContext()),
                App.getAppBaseUrl() + Constants.TASK_PROFILE_IMG + "/" + Concurrent.getAppUserID(getBaseContext()),
                false);

        //============== In case redirected from messages dialogs view ===============//
        msgDialogID = getIntent().getStringExtra("msg_id");
        loadMessages(0);
        setLoadingView();

        findViewById(R.id.head_drawer_toggle).setVisibility(View.GONE);
    }


    public void loadMessages(final Integer lastTimeStamp) {
        TOKEN = Concurrent.getAppToken(this);
        if (TOKEN != null) {
            String mLink = null;
            if (lastTimeStamp == 0)
                mLink = App.getAppBaseUrl() + Constants.TASK_MESSAGES + "/" + msgDialogID;



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
                                        JsonObject CurrObj;
                                        JsonArray ValuesArray;

                                        try {
                                            ValuesHolder = parser.parse(response).getAsJsonObject();
                                        } catch (Exception e) {
                                            showError("5001");
                                        }

                                        if (ValuesHolder != null) {

                                            //=================== Get destination User Data ============//
                                            JsonObject destinationUserObj = ValuesHolder.getAsJsonObject("messageDet");
                                            if (destinationUserObj == null) {
                                                setErrorView();
                                                return;
                                            }
                                            talkToUserID = Concurrent.tagsStringValidator(destinationUserObj, "toId");
                                            talkToUserFullName = Concurrent.tagsStringValidator(destinationUserObj, "fullName");

                                            destinationUser = new User(
                                                    "1",
                                                    talkToUserFullName,
                                                    App.getAppBaseUrl() + Constants.TASK_PROFILE_IMG + "/" + talkToUserID,
                                                    false);


                                            setHeaderUserData(talkToUserFullName, Integer.valueOf(talkToUserID));

                                            //=================== Get messages Data ============//
                                            ValuesArray = ValuesHolder.getAsJsonArray("messages");
                                            if (ValuesArray == null) {
                                                setErrorView();
                                                return;
                                            }
                                            if (ValuesArray.size() > 0) {

                                                Iterator<JsonElement> ValuesIterator = ValuesArray.iterator();
                                                String msgID;
                                                String msgDateSent;
                                                String msgDateSentFull;
                                                String msgText;
                                                String fromID;


                                                while (ValuesIterator.hasNext()) {
                                                    CurrObj = ValuesIterator.next().getAsJsonObject();
                                                    msgID = Concurrent.tagsStringValidator(CurrObj, "id");
                                                    msgText = Concurrent.tagsStringValidator(CurrObj, "messageText");
                                                    msgDateSent = Concurrent.tagsStringValidator(CurrObj, "dateSent");
                                                    msgDateSentFull = Concurrent.tagsStringValidator(CurrObj, "dateSentH");
                                                    fromID = Concurrent.tagsStringValidator(CurrObj, "fromId");

                                                    messagesListData.add(0,
                                                            new Message(msgID, fromID.equals(String.valueOf(Concurrent.getAppUserID(getBaseContext()))) ? myUser : destinationUser, msgText, msgDateSentFull, msgDateSent)
                                                    );
                                                }
                                                initAdapter();
                                                setMessagesListView();
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
                                                loadMessages(0);
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
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(MyContextWrapper.wrap(newBase, Concurrent.getLangDirection(newBase)));
    }

    public void loadOldMessages(final String lastTimeStamp) {
        TOKEN = Concurrent.getAppToken(this);
        if (TOKEN != null) {
            String mLink = App.getAppBaseUrl() + Constants.TASK_MESSAGES_OLD + "/" + Concurrent.getAppUserID(getBaseContext()) + "/" + talkToUserID + "/" + lastTimeStamp;





            OkHttpClient client = new OkHttpClient().newBuilder(getBaseContext()).connectTimeout(7, TimeUnit.SECONDS).build();

            Request.Builder requestBuilder = new Request.Builder()
                    .url(mLink);

            requestBuilder.get();

            Request request = requestBuilder.build();


            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, final IOException e) {

                }

                @Override
                public void onResponse(Call call, final Object serverResponse) {
                    final Response responseObj = (Response)serverResponse;
                    try {
                        response = responseObj.body().string();
                    } catch (Exception ignored) {
                        return;
                    }

                    if (response != null) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                try {
                                    if (responseObj.isSuccessful()) {

                                        JsonParser parser = new JsonParser();
                                        JsonObject CurrObj;
                                        List<Message> oldMessagesListData = new ArrayList<>();
                                        JsonArray ValuesArray = null;

                                        try {
                                            ValuesArray = parser.parse(response).getAsJsonArray();
                                        } catch (Exception ignored) {
                                        }

                                        if (ValuesArray != null) {
                                            if (ValuesArray.size() > 0) {

                                                Iterator<JsonElement> ValuesIterator = ValuesArray.iterator();
                                                String msgID;
                                                String msgDateSent;
                                                String msgDateSentFull;
                                                String msgText;
                                                String fromID;


                                                while (ValuesIterator.hasNext()) {
                                                    CurrObj = ValuesIterator.next().getAsJsonObject();
                                                    msgID = Concurrent.tagsStringValidator(CurrObj, "id");
                                                    msgText = Concurrent.tagsStringValidator(CurrObj, "messageText");
                                                    msgDateSent = Concurrent.tagsStringValidator(CurrObj, "dateSent");
                                                    msgDateSentFull = Concurrent.tagsStringValidator(CurrObj, "dateSentH");
                                                    fromID = Concurrent.tagsStringValidator(CurrObj, "fromId");

                                                    oldMessagesListData.add(
                                                            new Message(msgID, fromID.equals(String.valueOf(Concurrent.getAppUserID(getBaseContext()))) ? myUser : destinationUser, msgText, msgDateSentFull, msgDateSent)
                                                    );
                                                }
                                                messagesListData.addAll(oldMessagesListData);
                                                messagesAdapter.addToEnd(oldMessagesListData, false);
                                            }
                                        }
                                    }
                                } catch (final Exception ignored) {}
                            }
                        });
                    }
                }
            });
        }
    }


    public void checkNewMessages() {
        if (lastMsgTimeStamp != null && !lastMsgTimeStamp.equals("") && !checkNewMessagesLocked) {
            checkNewMessagesLocked = true;

            TOKEN = Concurrent.getAppToken(this);
            if (TOKEN != null) {

                Ion.with(getBaseContext()).load(OkHttpClient.strip(App.getAppBaseUrl() + Constants.TASK_MESSAGES_NEW + "/" + Concurrent.getAppUserID(getBaseContext()) + "/" + talkToUserID + "/" + lastMsgTimeStamp)).setTimeout(15000)
                        .asJsonArray().setCallback(new FutureCallback<JsonArray>() {
                    @Override
                    public void onCompleted(Exception exception, JsonArray ValuesArray) {
                        if (ValuesArray != null) {
                            JsonObject CurrObj;

                            if (ValuesArray.size() > 0) {

                                Iterator<JsonElement> ValuesIterator = ValuesArray.iterator();
                                String msgID;
                                String msgDateSent;
                                String msgDateSentFull;
                                String msgText;
                                String fromID;
                                Message newMessage;

                                while (ValuesIterator.hasNext()) {
                                    CurrObj = ValuesIterator.next().getAsJsonObject();
                                    msgID = Concurrent.tagsStringValidator(CurrObj, "id");
                                    msgText = Concurrent.tagsStringValidator(CurrObj, "messageText");
                                    msgDateSent = Concurrent.tagsStringValidator(CurrObj, "dateSent");
                                    msgDateSentFull = Concurrent.tagsStringValidator(CurrObj, "dateSentH");
                                    fromID = Concurrent.tagsStringValidator(CurrObj, "fromId");
                                    newMessage = new Message(msgID, fromID.equals(String.valueOf(Concurrent.getAppUserID(getBaseContext()))) ? myUser : destinationUser, msgText, msgDateSentFull, msgDateSent);

                                    messagesListData.add(0, newMessage);
                                    messagesAdapter.addToStart(newMessage, true);

                                    lastMsgTimeStamp = msgDateSent;
                                }
                            }
                            checkNewMessagesLocked = false;
                        } else if (exception != null) {
                            checkNewMessagesLocked = false;
                        }
                    }
                });

            }
        }

    }


    @Override
    public boolean onSubmit(CharSequence input) {
        currentMsgText = input.toString();

        if (!currentMsgText.equals("")) {
            messageInput.changeButtonStatus(MessageInput.sendButtonStatus.LOADING);
            postMessage(msgDialogID, currentMsgText, talkToUserID);
        }
        return true;
    }

    public void postMessage(String msgID, String messageText, String toUserID) {

        TOKEN = Concurrent.getAppToken(getBaseContext());
        if (TOKEN != null) {

            FormBody.Builder formBody = new FormBody.Builder();
            formBody.add("reply", messageText);
            formBody.add("toId", toUserID);
            formBody.add("disable", "true");

            OkHttpClient client = new OkHttpClient().newBuilder(getBaseContext()).connectTimeout(7, TimeUnit.SECONDS).build();

            Request.Builder requestBuilder = new Request.Builder()
                    .url(App.getAppBaseUrl() + Constants.TASK_MESSAGES + "/" + msgID);

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
                                    Toast.makeText(getBaseContext(), Concurrent.getLangSubWords("errorOccurred", "Error Occurred") + " ( Error Code: "+e.getMessage()+" )", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getBaseContext(), Concurrent.getLangSubWords("errorOccurred", "Error Occurred") + " ( Error Code: 5012 )", Toast.LENGTH_LONG).show();
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
                        Toast.makeText(getBaseContext(), Concurrent.getLangSubWords("errorOccurred", "Error Occurred") + " ( Error Code: 5001 )", Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (response != null) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                try {
                                    if (responseObj.isSuccessful()) {
                                        messageInput.changeButtonStatus(MessageInput.sendButtonStatus.ENABLED);
                                    }
                                } catch (final Exception e) {
                                    Toast.makeText(getBaseContext(), Concurrent.getLangSubWords("errorOccurred", "Error Occurred") + " ( Error Code: 5002 )", Toast.LENGTH_LONG).show();

                                }
                            }
                        });
                    } else {
                        Toast.makeText(getBaseContext(), Concurrent.getLangSubWords("errorOccurred", "Error Occurred") + " ( Error Code: 5001 )", Toast.LENGTH_LONG).show();
                    }
                }
            });

        }

    }


    private void initAdapter() {
        MessageHolders holdersConfig = new MessageHolders()
                .setIncomingTextLayout(R.layout.item_custom_incoming_text_message)
                .setOutcomingTextLayout(R.layout.item_custom_outcoming_text_message);

        super.messagesAdapter = new MessagesListAdapter<>(super.senderId, holdersConfig, super.imageLoader);
        super.messagesAdapter.setLoadMoreListener(this);
        super.messagesAdapter.setOnMessageLongClickListener(this);
        messagesListView.setAdapter(super.messagesAdapter);

        if (messagesListData.size() > 0) {
            messagesAdapter.addToEnd(messagesListData, true);
            messageInput.changeButtonStatus(MessageInput.sendButtonStatus.ENABLED);
        }


        //============ Set last message timestamp ===========//
        if (messagesListData.size() > 0)
            lastMsgTimeStamp = messagesListData.get(0).getDateSentInTimeStamp();

        registerCheckNewMessagesCallback();
    }

    public void registerCheckNewMessagesCallback() {
        final long inMessageInterval = (long) Concurrent.getRefreshInMessagesInterval(getBaseContext());
        checkNewMessagesHandler = new Handler();
        checkNewMessagesRunnable = new Runnable() {
            @Override
            public void run() {
                checkNewMessages();
                checkNewMessagesHandler.postDelayed(checkNewMessagesRunnable, inMessageInterval);
            }
        };
        checkNewMessagesHandler.post(checkNewMessagesRunnable);

    }


    @Override
    public void onLoadMore(int page, int totalItemsCount) {
        loadOldMessages(messagesListData.get(messagesListData.size() - 1).getDateSentInTimeStamp());
    }


    public String getCurrentTimeStamp() {
        Long tsLong = System.currentTimeMillis() / 1000;
        return tsLong.toString();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (checkNewMessagesHandler != null && checkNewMessagesRunnable != null)
            checkNewMessagesHandler.removeCallbacks(checkNewMessagesRunnable);

    }

    @Override
    protected void onResume() {
        super.onResume();
        checkNewMessagesLocked = false;
        registerCheckNewMessagesCallback();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (checkNewMessagesHandler != null && checkNewMessagesRunnable != null)
            checkNewMessagesHandler.removeCallbacks(checkNewMessagesRunnable);

    }

    public void setHeaderUserData(String userFullName, Integer userID) {

        findViewById(R.id.head_drawer_title).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.head_drawer_title)).setText(userFullName);

        findViewById(R.id.head_drawer_img).setVisibility(View.VISIBLE);
        CustomImageView userImage = (CustomImageView) findViewById(R.id.head_drawer_img);
        userImage.profileID = String.valueOf(userID);
        userImage.load();

    }

    public void setLoadingView() {
        findViewById(R.id.messagesListLoaderCon).setVisibility(View.VISIBLE);
        findViewById(R.id.messagesList).setVisibility(View.GONE);
        findViewById(R.id.error_view).setVisibility(View.GONE);
        findViewById(R.id.empty_view).setVisibility(View.GONE);
    }

    public void setMessagesListView() {
        findViewById(R.id.messagesListLoaderCon).setVisibility(View.GONE);
        findViewById(R.id.messagesList).setVisibility(View.VISIBLE);
        findViewById(R.id.error_view).setVisibility(View.GONE);
        findViewById(R.id.empty_view).setVisibility(View.GONE);

    }

    public void setErrorView() {
        findViewById(R.id.messagesListLoaderCon).setVisibility(View.GONE);
        findViewById(R.id.messagesList).setVisibility(View.GONE);
        findViewById(R.id.error_view).setVisibility(View.VISIBLE);
        findViewById(R.id.empty_view).setVisibility(View.GONE);

    }

    public void setEmptyView() {
        findViewById(R.id.messagesListLoaderCon).setVisibility(View.GONE);
        findViewById(R.id.messagesList).setVisibility(View.GONE);
        findViewById(R.id.error_view).setVisibility(View.GONE);
        findViewById(R.id.empty_view).setVisibility(View.VISIBLE);

    }

    public void setViewsClickListener() {
        View.OnClickListener mListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMessages(0);
                setLoadingView();
            }
        };

        findViewById(R.id.empty_view).setOnClickListener(mListener);
        findViewById(R.id.error_view).setOnClickListener(mListener);
    }

    @Override
    public void onMessageLongClick(Message message) {

    }
}
