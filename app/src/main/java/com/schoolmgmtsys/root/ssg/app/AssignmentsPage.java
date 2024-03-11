package com.schoolmgmtsys.root.ssg.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import com.schoolmgmtsys.root.ssg.models.AssignmentsModel;

import com.schoolmgmtsys.root.ssg.R;
import com.schoolmgmtsys.root.ssg.utils.App;
import com.schoolmgmtsys.root.ssg.utils.Concurrent;
import com.schoolmgmtsys.root.ssg.utils.Constants;
import com.schoolmgmtsys.root.ssg.utils.Downloader;
import com.schoolmgmtsys.root.ssg.utils.DrawerListFragment;
import com.schoolmgmtsys.root.ssg.utils.InputDialog;
import com.solutionsbricks.solbricksframework.Call;
import com.solutionsbricks.solbricksframework.Callback;
import com.solutionsbricks.solbricksframework.FormBody;
import com.solutionsbricks.solbricksframework.OkHttpClient;
import com.solutionsbricks.solbricksframework.Request;
import com.solutionsbricks.solbricksframework.Response;
import com.solutionsbricks.solbricksframework.helpers.ListManager;
import com.schoolmgmtsys.root.ssg.fonts.ParentStyledTextView;
import com.solutionsbricks.solbricksframework.helpers.TintImageView;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class AssignmentsPage extends SlidingFragmentActivity implements ListManager.ListInterface {

    private ListManager mListManager;
    private ArrayList<AssignmentsModel> LIST_DATA = new ArrayList<AssignmentsModel>();
    private Integer Res_PageLayout = R.layout.page_assign;
    private Integer Res_PageList = R.id.assign_view_list;
    private Integer Res_PageItemList = R.layout.page_assign_list_item;
    private String TOKEN;
    private ProgressBar mProgressBar;
    private boolean TokenRetry;
    private ImageView mRefresh;
    private boolean searchViewInit;
    private Integer uploadAnswerAssignID;
    private String response;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Concurrent.getLangDirection(this).equals("ar"))
            getSlidingMenu().setMode(SlidingMenu.RIGHT);
        else getSlidingMenu().setMode(SlidingMenu.LEFT);

        setContentView(Res_PageLayout);

        TintImageView mAddIcon = (TintImageView) findViewById(R.id.add_icon_imageView);

        if (Concurrent.isUserHavePermission(getBaseContext(), "Assignments.AddAssignments")) {
            mAddIcon.setVisibility(View.VISIBLE);
            mAddIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(AssignmentsPage.this, AssignmentAddNew.class);
                    startActivity(intent);
                }
            });
        } else {
            mAddIcon.setVisibility(View.GONE);

        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            LIST_DATA = extras.getParcelableArrayList("EXTRA_LIST");
            searchViewInit = true;
        }
        ImageView backImage = (ImageView) findViewById(R.id.background_img);
        FrameLayout logBack = (FrameLayout) findViewById(R.id.full_layout);

        Resources res = getResources();
        if (!res.getBoolean(R.bool.x_gen_back_is_image)) {
            backImage.setVisibility(View.GONE);
            logBack.setBackgroundColor(Concurrent.getColor(this, R.color.x_gen_back));
        }

        TextView HeadTitle = (TextView) findViewById(R.id.head_drawer_title);
        HeadTitle.setText(Concurrent.getLangSubWords("Assignments", "Assignments"));

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

        ImageView mSearch = (ImageView) findViewById(R.id.search);
        mSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (LIST_DATA != null && LIST_DATA.size() > 0) {
                    Intent MyIntent = new Intent(getBaseContext(), ControlActivity.class);
                    MyIntent.putExtra("TARGET_FRAGMENT", "SearchView");
                    MyIntent.putExtra("EXTRA_STRING_1", "AssignmentsPage");
                    MyIntent.putExtra("EXTRA_LIST", LIST_DATA);
                    MyIntent.putExtra("EXTRA_HEAD_FIND_WORD", "Search");
                    MyIntent.putExtra("EXTRA_HEAD_REPLACE_WORD", "Search");
                    startActivity(MyIntent);
                    overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                }
            }
        });

        mProgressBar = (ProgressBar) findViewById(R.id.gen_loader);
        mRefresh = (ImageView) findViewById(R.id.refresh);
        mRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadData();
            }
        });
        mRefresh.setVisibility(View.VISIBLE);

        ImageView ToogleBtn = (ImageView) findViewById(R.id.head_drawer_toggle);
        ToogleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        mProgressBar.setVisibility(View.INVISIBLE);

        ListView viewList = (ListView) findViewById(Res_PageList);

        viewList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        mListManager = new ListManager(this, viewList, this, LIST_DATA);
        mListManager.removeFooter();

        findViewById(R.id.error_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadData();
            }
        });
        findViewById(R.id.empty_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadData();
            }
        });


        if (!searchViewInit) {
            loadData();
        } else {
            if (LIST_DATA.size() == 0)
                mListManager.setNoDataView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(Res_PageList));
        }

    }


    public void loadData() {
        TOKEN = Concurrent.getAppToken(this);
        if (TOKEN != null) {
            mProgressBar.setVisibility(VISIBLE);
            mRefresh.setVisibility(INVISIBLE);


            OkHttpClient client = new OkHttpClient().newBuilder(getBaseContext()).connectTimeout(7, TimeUnit.SECONDS).build();

            Request.Builder requestBuilder = new Request.Builder()
                    .url(App.getAppBaseUrl() + Constants.TASK_ASSIGNMENT_LIST);

            requestBuilder.get();

            Request request = requestBuilder.build();


            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, final IOException e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            mListManager.setErrorView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(Res_PageList));
                            mProgressBar.setVisibility(INVISIBLE);
                            mRefresh.setVisibility(VISIBLE);
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

                                        try {
                                            ValuesHolder = parser.parse(response).getAsJsonObject();
                                        } catch (Exception e) {
                                            showError("5001");
                                        }

                                        if (ValuesHolder != null && ValuesHolder.has("assignments")) {

                                            JsonArray ValuesArray = ValuesHolder.getAsJsonArray("assignments");

                                            if (ValuesArray.size() == 0 || ValuesArray.isJsonNull()) {
                                                mListManager.setNoDataView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(Res_PageList));
                                                mProgressBar.setVisibility(INVISIBLE);
                                                mRefresh.setVisibility(VISIBLE);
                                                return;
                                            }

                                            LIST_DATA.clear();

                                            for (JsonElement aValuesArray : ValuesArray) {
                                                JsonObject CurrObj = aValuesArray.getAsJsonObject();
                                                String title = Concurrent.tagsStringValidator(CurrObj, "AssignTitle");

                                                if (title != null && !title.equals(""))
                                                    LIST_DATA.add(new AssignmentsModel(Concurrent.tagsIntValidator(CurrObj, "id"), title, Concurrent.tagsStringValidator(CurrObj, "AssignDescription"), Concurrent.tagsStringValidator(CurrObj, "AssignDeadLine"), Concurrent.tagsStringValidator(CurrObj, "AssignFile")));
                                            }

                                            if (LIST_DATA.size() == 0)
                                                mListManager.setNoDataView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(Res_PageList));
                                            else
                                                mListManager.setDataView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(Res_PageList));

                                            mListManager.getListAdapter().notifyDataSetChanged();


                                        } else {
                                            showError("5001");
                                        }

                                        mProgressBar.setVisibility(INVISIBLE);
                                        mRefresh.setVisibility(VISIBLE);
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
                            mListManager.setErrorView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(Res_PageList));
                            mProgressBar.setVisibility(INVISIBLE);
                            mRefresh.setVisibility(VISIBLE);
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
                                                loadData();

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
            mListManager.setErrorView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(Res_PageList));
            mProgressBar.setVisibility(INVISIBLE);
            mRefresh.setVisibility(VISIBLE);
        }
    }

    public void showError(String errorCode) {
        String errorTitle;
        if (errorCode != null) {
            errorTitle = Concurrent.getLangSubWords("errorOccurred", "Error Occurred") + " ( Error Code: " + errorCode + " )";
        } else {
            errorTitle = Concurrent.getLangSubWords("errorOccurred", "Error Occurred");
        }
        mListManager.setErrorView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(Res_PageList));
        mProgressBar.setVisibility(INVISIBLE);
        mRefresh.setVisibility(VISIBLE);
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
        StaticPagesHolder holder;
        if (convertView == null || convertView.getTag() == null) {
            holder = new StaticPagesHolder();
            convertView = inflater.inflate(Res_PageItemList, null);

            holder.TITLE = convertView.findViewById(R.id.header_title);
            holder.CONTENT =  convertView.findViewById(R.id.header_content);
            holder.DeadLine = convertView.findViewById(R.id.footer_deadline_data);
            holder.MENU_DOWNLOAD = convertView.findViewById(R.id.menu_download);
            holder.FOOTER_VIEW_ANSWERS = convertView.findViewById(R.id.footer_answers);
            holder.AssignmentDeadline = convertView.findViewById(R.id.AssignmentDeadline);
            holder.UPLOAD_ANSWER = convertView.findViewById(R.id.upload_answer);


            convertView.setTag(holder);
        } else {
            holder = (StaticPagesHolder) convertView.getTag();
        }
        AssignmentsModel posValues = LIST_DATA.get(position);
        if (posValues != null) {
            holder.CONTENT.setNotNullText(posValues.content);
            holder.TITLE.setNotNullText(posValues.title);
            holder.DeadLine.setNotNullText(posValues.DeadLine);
            holder.FOOTER_VIEW_ANSWERS.setTag(posValues.id);
            holder.UPLOAD_ANSWER.setTag(posValues.id);


            if (posValues != null && posValues.AssignFile != null && Concurrent.isUserHavePermission(getBaseContext(), "Assignments.Download")) {
                holder.MENU_DOWNLOAD.setVisibility(View.VISIBLE);
                holder.MENU_DOWNLOAD.setTag(posValues);

                holder.MENU_DOWNLOAD.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Integer itemID = ((AssignmentsModel) (v.getTag())).id;
                        String itemFILE = ((AssignmentsModel) (v.getTag())).AssignFile;

                        new Downloader().downloadFile(AssignmentsPage.this, itemFILE, App.getAppBaseUrl() + Constants.TASK_ASSIGN_CONTROL + "/download/" + itemID);
                    }
                });
            } else holder.MENU_DOWNLOAD.setVisibility(View.GONE);

            Concurrent.setLangWords(this, holder.AssignmentDeadline);

            if (Concurrent.isUserHavePermission(getBaseContext(), "Assignments.viewAnswers")) {

                holder.FOOTER_VIEW_ANSWERS.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent MyIntent = new Intent(getBaseContext(), ControlActivity.class);
                        MyIntent.putExtra("TARGET_FRAGMENT", "AssignmentViewAnswers");
                        MyIntent.putExtra("EXTRA_HEAD_FIND_WORD", "Assignments");
                        MyIntent.putExtra("EXTRA_HEAD_REPLACE_WORD", "Assignments");
                        MyIntent.putExtra("EXTRA_INT_1", (Integer) v.getTag());
                        startActivity(MyIntent);
                        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                    }
                });
            } else {
                holder.FOOTER_VIEW_ANSWERS.setVisibility(View.GONE);
            }
        }


        if (Concurrent.isUserHavePermission(getBaseContext(), "Assignments.applyAssAnswer")) {
            holder.UPLOAD_ANSWER.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {

                    TOKEN = Concurrent.getAppToken(AssignmentsPage.this);
                    if (TOKEN != null) {
                        Toast.makeText(AssignmentsPage.this, Concurrent.getLangSubWords("pleaseWait", "Please Wait"), Toast.LENGTH_LONG).show();


                        FormBody.Builder formBody = new FormBody.Builder();
                        formBody.add("assignmentId", String.valueOf(v.getTag()));

                        OkHttpClient client = new OkHttpClient().newBuilder(getBaseContext()).connectTimeout(7, TimeUnit.SECONDS).build();

                        Request.Builder requestBuilder = new Request.Builder()
                                .url(App.getAppBaseUrl() + Constants.TASK_ASSIGNMENT_CHECK_UPLOAD);

                        requestBuilder.post(formBody.build());

                        Request request = requestBuilder.build();


                        Call call = client.newCall(request);
                        call.enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, final IOException e) {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        mListManager.setErrorView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(Res_PageList));
                                        mProgressBar.setVisibility(INVISIBLE);
                                        mRefresh.setVisibility(VISIBLE);
                                        if (e instanceof ConnectException) {
                                            Toast.makeText(getBaseContext(), Concurrent.getLangSubWords("noConnection", "No Internet Connection"), Toast.LENGTH_LONG).show();
                                        } else {
                                            if (Concurrent.isFloat(e.getMessage())) {
                                                Toast.makeText(AssignmentsPage.this,
                                                        Concurrent.getLangSubWords("errorOccurred", "Error Occurred") + " - Error Code : " + e.getMessage(),
                                                        Toast.LENGTH_LONG).show();
                                            } else {
                                                Toast.makeText(AssignmentsPage.this,
                                                        Concurrent.getLangSubWords("errorOccurred", "Error Occurred") + " - Error Code : 5012",
                                                        Toast.LENGTH_LONG).show();
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
                                    Toast.makeText(AssignmentsPage.this,
                                            Concurrent.getLangSubWords("errorOccurred", "Error Occurred") + " - Error Code : 5001",
                                            Toast.LENGTH_LONG).show();
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
                                                        Toast.makeText(AssignmentsPage.this,
                                                                Concurrent.getLangSubWords("errorOccurred", "Error Occurred") + " - Error Code : 5001",
                                                                Toast.LENGTH_LONG).show();
                                                    }
                                                    if (ValuesHolder != null) {
                                                        String canApply;
                                                        Boolean canApplyBool = false;

                                                        if (ValuesHolder.has("status")) {
                                                            canApply = Concurrent.tagsStringValidator(ValuesHolder, "status");
                                                            canApplyBool = !canApply.equals("failed");
                                                        } else if (ValuesHolder.has("canApply")) {
                                                            canApply = Concurrent.tagsStringValidator(ValuesHolder, "canApply");
                                                            canApplyBool = canApply.equals("true");
                                                        } else {
                                                            Toast.makeText(AssignmentsPage.this, Concurrent.getLangSubWords("errorOccurred", "Error Occurred"), Toast.LENGTH_LONG).show();
                                                        }

                                                        if (canApplyBool) {
                                                            uploadAnswerAssignID = (Integer) v.getTag();
                                                            new MaterialFilePicker()
                                                                    .withActivity(AssignmentsPage.this)
                                                                    .withRequestCode(1)
                                                                    .withHiddenFiles(true) // Show hidden files and folders
                                                                    .start();
                                                        } else {
                                                            uploadAnswerAssignID = 0;
                                                            Toast.makeText(AssignmentsPage.this, Concurrent.tagsStringValidator(ValuesHolder, "message"), Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                } else {
                                                    renewToken();
                                                }
                                            } catch (final Exception e) {
                                                Toast.makeText(AssignmentsPage.this,
                                                        Concurrent.getLangSubWords("errorOccurred", "Error Occurred") + " - Error Code : 5002",
                                                        Toast.LENGTH_LONG).show();

                                            }
                                        }
                                    });
                                } else {
                                    Toast.makeText(AssignmentsPage.this,
                                            Concurrent.getLangSubWords("errorOccurred", "Error Occurred") + " - Error Code : 5001",
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                }
            });
        } else {
            holder.UPLOAD_ANSWER.setVisibility(View.GONE);
        }


        return convertView;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (uploadAnswerAssignID != null && uploadAnswerAssignID != 0) {
                final File file = (File) data.getExtras().get(FilePickerActivity.RESULT_FILE);
                SharedPreferences Prefs = PreferenceManager.getDefaultSharedPreferences(AssignmentsPage.this);

                InputDialog.Builder msg = new InputDialog.Builder(AssignmentsPage.this)
                        .setTitle("Write note about assignment")
                        .setInputHint("Assignment Note")
                        .setPositiveButton(Concurrent.getLangSubWords("ok", "OK"), new InputDialog.ButtonActionListener() {
                            public SharedPreferences Prefs;

                            @Override
                            public void onClick(CharSequence inputText) {


                                TOKEN = Concurrent.getAppToken(AssignmentsPage.this);
                                if (TOKEN != null) {
                                    Toast.makeText(AssignmentsPage.this, Concurrent.getLangSubWords("pleaseWait", "Please Wait"), Toast.LENGTH_LONG).show();

                                    Ion.with(AssignmentsPage.this)
                                            .load(OkHttpClient.strip(App.getAppBaseUrl() + Constants.TASK_ASSIGNMENT_UPLOAD + "/" + uploadAnswerAssignID))
                                            .setMultipartParameter("userNotes", String.valueOf(inputText))
                                            .setMultipartFile("fileName", file)
                                            .asJsonObject().setCallback(new FutureCallback<JsonObject>() {

                                        @Override
                                        public void onCompleted(Exception exception, JsonObject ValuesHolder) {
                                            if (exception != null) {
                                                Toast.makeText(AssignmentsPage.this, Concurrent.getLangSubWords("errorOccurred", "Error Occurred"), Toast.LENGTH_LONG).show();
                                            } else if (ValuesHolder != null) {
                                                if (ValuesHolder.has("status")) {
                                                    Toast.makeText(AssignmentsPage.this, Concurrent.tagsStringValidator(ValuesHolder, "message"), Toast.LENGTH_LONG).show();
                                                } else {
                                                    Toast.makeText(AssignmentsPage.this, Concurrent.getLangSubWords("errorOccurred", "Error Occurred"), Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        }


                                    });

                                }

                            }
                        });
                msg.show();

            }
        }
    }

    class StaticPagesHolder {
        public TextView AssignmentDeadline;
        RelativeLayout MENU_DOWNLOAD;
        RelativeLayout FOOTER_VIEW_ANSWERS;
        ParentStyledTextView CONTENT;
        ParentStyledTextView TITLE;
        ParentStyledTextView DeadLine;
        RelativeLayout UPLOAD_ANSWER;
    }
}
