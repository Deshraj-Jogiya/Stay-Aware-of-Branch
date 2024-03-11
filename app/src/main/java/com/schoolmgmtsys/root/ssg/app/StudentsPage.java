package com.schoolmgmtsys.root.ssg.app;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.schoolmgmtsys.root.ssg.models.StudentsModel;
import com.schoolmgmtsys.root.ssg.R;
import com.schoolmgmtsys.root.ssg.utils.App;
import com.schoolmgmtsys.root.ssg.utils.Concurrent;
import com.schoolmgmtsys.root.ssg.utils.Constants;
import com.solutionsbricks.solbricksframework.Call;
import com.solutionsbricks.solbricksframework.Callback;
import com.solutionsbricks.solbricksframework.FormBody;
import com.solutionsbricks.solbricksframework.MediaType;
import com.solutionsbricks.solbricksframework.OkHttpClient;
import com.solutionsbricks.solbricksframework.Request;
import com.solutionsbricks.solbricksframework.RequestBody;
import com.solutionsbricks.solbricksframework.Response;
import com.solutionsbricks.solbricksframework.helpers.CustomImageView;
import com.schoolmgmtsys.root.ssg.utils.DrawerListFragment;
import com.solutionsbricks.solbricksframework.helpers.ListManager;
import com.schoolmgmtsys.root.ssg.fonts.ParentStyledTextView;

import org.json.JSONObject;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class StudentsPage extends SlidingFragmentActivity implements ListManager.ListInterface {

    private ListView ViewList;
    private ListManager mListManager;
    private StaticPagesHolder holder;

    private StudentsModel posValues;
    private ArrayList<StudentsModel> LIST_DATA = new ArrayList<>();
    private Integer Res_PageLayout = R.layout.page_students;
    private Integer Res_PageList = R.id.students_view_list;
    private Integer Res_PageItemList = R.layout.page_students_list_item;
    private Integer Res_PageApproveItemList = R.layout.page_students_approve_item;
    private String TOKEN;
    private boolean Locked;
    private Integer nextPage = 1;
    private ProgressBar mProgressBar;
    private ImageView ToogleBtn;
    private boolean TokenRetry;
    private ImageView mRefresh;
    private String EXTRA_SEARCH;
    private boolean WaitApprove;
    private LinearLayout WaitingApprove;
    private String response;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Concurrent.getLangDirection(this).equals("ar"))
            getSlidingMenu().setMode(SlidingMenu.RIGHT);
        else getSlidingMenu().setMode(SlidingMenu.LEFT);

        setContentView(Res_PageLayout);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            EXTRA_SEARCH = extras.getString("EXTRA_SEARCH");
            String TargetLoad = extras.getString("TARGET_LOAD");
            if (TargetLoad != null) WaitApprove = TargetLoad.equals("WaitApprove") ? true : false;
        }
        ImageView backImage = (ImageView) findViewById(R.id.background_img);
        FrameLayout logBack = (FrameLayout) findViewById(R.id.full_layout);

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

        ImageView mSearch = (ImageView) findViewById(R.id.search);
        mSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent MyIntent = new Intent(getBaseContext(), ControlActivity.class);
                MyIntent.putExtra("TARGET_FRAGMENT", "SearchView");
                MyIntent.putExtra("EXTRA_STRING_1", "StudentsPage");
                MyIntent.putExtra("EXTRA_HEAD_FIND_WORD", "Search");
                MyIntent.putExtra("EXTRA_HEAD_REPLACE_WORD", "Search");
                startActivity(MyIntent);
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            }
        });

        mProgressBar = (ProgressBar) findViewById(R.id.gen_loader);
        WaitingApprove = (LinearLayout) findViewById(R.id.footer);
        TextView WaitingApproveTxt = (TextView) findViewById(R.id.wait_approve_txt);
        WaitingApproveTxt.setText(Concurrent.getLangSubWords("waitingApproval", "Waiting Approval"));
        findViewById(R.id.wait_approve_con).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent MyIntent = new Intent(getBaseContext(), StudentsPage.class);
                MyIntent.putExtra("TARGET_LOAD", "WaitApprove");
                startActivity(MyIntent);
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            }
        });
        if (WaitApprove || !Concurrent.isUserHavePermission(getBaseContext(), "students.Approve")) {
            WaitingApprove.setVisibility(View.GONE);
        } else {
            WaitingApprove.setVisibility(View.VISIBLE);
        }
        mRefresh = (ImageView) findViewById(R.id.refresh);
        mRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (WaitApprove) {
                    loadWaitApproveData();
                } else {
                    nextPage = 1;
                    loadData(nextPage);
                }
            }
        });
        mRefresh.setVisibility(View.VISIBLE);
        TextView HeadTitle = (TextView) findViewById(R.id.head_drawer_title);
        HeadTitle.setText(Concurrent.getLangSubWords("students", "Students"));

        ToogleBtn = (ImageView) findViewById(R.id.head_drawer_toggle);
        ToogleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        mProgressBar.setVisibility(View.INVISIBLE);

        ViewList = (ListView) findViewById(Res_PageList);

        ViewList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
        mListManager = new ListManager(this, ViewList, this, LIST_DATA);

        findViewById(R.id.error_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (WaitApprove) {
                    loadWaitApproveData();
                } else {
                    nextPage = 1;
                    loadData(nextPage);
                }
            }
        });
        findViewById(R.id.empty_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (WaitApprove) {
                    loadWaitApproveData();
                } else {
                    nextPage = 1;
                    loadData(nextPage);
                }
            }
        });

        if (WaitApprove) {
            loadWaitApproveData();
        } else {
            nextPage = 1;
            loadData(nextPage);
        }

    }


    public void loadWaitApproveData() {
        if (!Locked) {
            mListManager.removeFooter();
            Locked = true;
            mProgressBar.setVisibility(View.VISIBLE);
            mRefresh.setVisibility(View.INVISIBLE);
            TOKEN = Concurrent.getAppToken(this);
            if (TOKEN != null) {
                String mLink = App.getAppBaseUrl() + Constants.TASK_STUDENTS_WAIT_APPROVE;


                OkHttpClient client = new OkHttpClient().newBuilder(getBaseContext()).connectTimeout(7, TimeUnit.SECONDS).build();

                Request.Builder requestBuilder = new Request.Builder()
                        .addHeader("Content-Type", "application/json")
                        .url(mLink);

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
                                            JsonArray ValuesArray = null;

                                            try {
                                                ValuesArray = parser.parse(response).getAsJsonArray();
                                            } catch (Exception e) {
                                                Toast.makeText(StudentsPage.this, Concurrent.getLangSubWords("errorOccurred","Error Occurred"), Toast.LENGTH_LONG).show();
                                            }
                                            if(ValuesArray != null){
                                                if (ValuesArray.size() == 0 || ValuesArray.isJsonNull()) {
                                                    mListManager.setNoDataView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(Res_PageList));
                                                    Locked = false;
                                                    mProgressBar.setVisibility(View.INVISIBLE);
                                                    mRefresh.setVisibility(View.VISIBLE);

                                                    return;
                                                }


                                                if (ValuesArray.size() > 0) {
                                                    LIST_DATA.clear();
                                                    for (JsonElement aValuesArray : ValuesArray) {
                                                        JsonObject CurrObj = aValuesArray.getAsJsonObject();
                                                        String title = Concurrent.tagsStringValidator(CurrObj, "username");
                                                        JsonObject parentObj = CurrObj.getAsJsonObject("parents");
                                                        JsonObject fatherObj = Concurrent.getJsonObject(parentObj, "mother");
                                                        if (title != null && !title.equals(""))
                                                            LIST_DATA.add(new StudentsModel(Concurrent.tagsIntValidator(CurrObj, "id"), Concurrent.tagsStringValidator(CurrObj, "fullName"), title, Concurrent.tagsStringValidator(CurrObj, "email"), Concurrent.tagsStringValidator(CurrObj, "studentClass"), Concurrent.tagsStringValidator(CurrObj, "isLeaderBoard"), Concurrent.tagsStringValidator(fatherObj, "mobile"), Concurrent.tagsStringValidator(fatherObj, "name"), Concurrent.tagsStringValidator(fatherObj, "email"), Concurrent.tagsStringValidator(CurrObj, "admissionDate"), Concurrent.tagsStringValidator(CurrObj, "phoneNo"),Concurrent.tagsStringValidator(CurrObj, "cat_title"), Concurrent.tagsStringValidator(CurrObj, "attendanceRec"), Concurrent.tagsStringValidator(CurrObj, "studentSection"), Concurrent.tagsStringValidator(CurrObj, "age"), Concurrent.tagsStringValidator(CurrObj, "school")));
                                                    }

                                                    if (LIST_DATA.size() == 0)
                                                        mListManager.setNoDataView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(Res_PageList));
                                                    else
                                                        mListManager.setDataView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(Res_PageList));

                                                    mListManager.getListAdapter().notifyDataSetChanged();
                                                } else {
                                                    mListManager.removeFooter();
                                                    if (LIST_DATA.size() == 0)
                                                        mListManager.setNoDataView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(Res_PageList));
                                                }
                                                Locked = false;
                                                mProgressBar.setVisibility(View.INVISIBLE);
                                                mRefresh.setVisibility(View.VISIBLE);
                                                TokenRetry = false;
                                            }

                                        } else {
                                            renewToken(true);
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
                Locked = false;
                mProgressBar.setVisibility(View.INVISIBLE);
                mRefresh.setVisibility(View.VISIBLE);
            }
        }
    }


    public void loadData(final Integer page) {
        if (!Locked) {
            Locked = true;
            mProgressBar.setVisibility(View.VISIBLE);
            mRefresh.setVisibility(View.INVISIBLE);
            TOKEN = Concurrent.getAppToken(this);
            Log.e("Token"," "+TOKEN);
            if (TOKEN != null) {
                String mLink = App.getAppBaseUrl() + Constants.TASK_STUDENTS_LIST + "/" + page;


                OkHttpClient client = new OkHttpClient().newBuilder(getBaseContext()).connectTimeout(7, TimeUnit.SECONDS).build();

                Request.Builder requestBuilder = new Request.Builder()
                        .addHeader("Content-Type", "application/json")
                        .url(mLink);


                if (EXTRA_SEARCH != null && !EXTRA_SEARCH.equals("")){
                    MediaType MEDIA_TYPE = MediaType.parse("application/json");
                    JSONObject paramsObject = new JSONObject();
                    JSONObject insideParamsObject = new JSONObject();
                    try {
                        insideParamsObject.put("text", EXTRA_SEARCH);
                        paramsObject.put("searchInput", insideParamsObject);
                    } catch (Exception ignored) {
                    }
                    RequestBody body = RequestBody.create(MEDIA_TYPE, paramsObject.toString());
                    requestBuilder.post(body);
                }else{
                    requestBuilder.get();
                }


                final Request request = requestBuilder.build();


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
                    public void onResponse(final Call call, final Object serverResponse) {
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
                                        Log.e("here request",call.request().url().toString());

                                        if (responseObj.isSuccessful()) {

                                            JsonParser parser = new JsonParser();
                                            JsonObject ValuesHolder = null;

                                            try {
                                                ValuesHolder = parser.parse(response).getAsJsonObject();
                                            } catch (Exception e) {
                                                Toast.makeText(StudentsPage.this, Concurrent.getLangSubWords("errorOccurred", "Error Occurred"), Toast.LENGTH_LONG).show();
                                            }
                                            if(ValuesHolder != null){

                                                JsonArray ValuesArray = ValuesHolder.getAsJsonArray("students");
                                                if (ValuesArray != null) {
                                                    if (ValuesArray.size() > 0) {
                                                        if (page == null || page == 1) LIST_DATA.clear();
                                                        for (JsonElement aValuesArray : ValuesArray) {
                                                            JsonObject CurrObj = aValuesArray.getAsJsonObject();
                                                            String title = Concurrent.tagsStringValidator(CurrObj, "username");

                                                            JsonObject parentObj = CurrObj.getAsJsonObject("parents");
                                                            JsonObject fatherObj = Concurrent.getJsonObject(parentObj, "mother");
                                                            if (title != null && !title.equals(""))
                                                                LIST_DATA.add(new StudentsModel(Concurrent.tagsIntValidator(CurrObj, "id"), Concurrent.tagsStringValidator(CurrObj, "fullName"), title, Concurrent.tagsStringValidator(CurrObj, "email"), Concurrent.tagsStringValidator(CurrObj, "studentClass"), Concurrent.tagsStringValidator(CurrObj, "isLeaderBoard"), Concurrent.tagsStringValidator(fatherObj, "mobile"), Concurrent.tagsStringValidator(fatherObj, "name"), Concurrent.tagsStringValidator(fatherObj, "email"), Concurrent.tagsStringValidator(CurrObj, "admissionDate"), Concurrent.tagsStringValidator(CurrObj, "phoneNo"),Concurrent.tagsStringValidator(CurrObj, "cat_title"), Concurrent.tagsStringValidator(CurrObj, "attendanceRec"), Concurrent.tagsStringValidator(CurrObj, "studentSection"), Concurrent.tagsStringValidator(CurrObj, "age"), Concurrent.tagsStringValidator(CurrObj, "school")));
                                                        }
                                                        if (LIST_DATA.size() == 0)
                                                            mListManager.setNoDataView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(Res_PageList));
                                                        else
                                                            mListManager.setDataView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(Res_PageList));

                                                        mListManager.getListAdapter().notifyDataSetChanged();
                                                        nextPage = page + 1;
                                                    } else {
                                                        mListManager.removeFooter();
                                                        if (LIST_DATA.size() == 0)
                                                            mListManager.setNoDataView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(Res_PageList));
                                                    }
                                                } else {
                                                    mListManager.removeFooter();
                                                }
                                            }
                                            Locked = false;
                                            mProgressBar.setVisibility(View.INVISIBLE);
                                            mRefresh.setVisibility(View.VISIBLE);
                                            TokenRetry = false;
                                        } else {
                                            renewToken(false);
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
                Locked = false;
                mProgressBar.setVisibility(View.INVISIBLE);
                mRefresh.setVisibility(View.VISIBLE);
            }
        }
    }



    public void renewToken(final Boolean isLoadingWaitingApprove){
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
                    Locked = false;
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
                                                if(!isLoadingWaitingApprove){
                                                    nextPage = 1;
                                                    loadData(nextPage);
                                                }else{
                                                    loadWaitApproveData();
                                                }

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
                    Locked = false;
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
        if (!WaitApprove) loadData(nextPage);
    }

    @Override
    public void AdapterConstructor() {

    }

    @Override
    public View AdapterGetView(int position, View convertView) {
        LayoutInflater inflater = getLayoutInflater();
        if (convertView == null || convertView.getTag() == null) {
            holder = new StaticPagesHolder();
            if (!WaitApprove) convertView = inflater.inflate(Res_PageItemList, null);
            else convertView = inflater.inflate(Res_PageApproveItemList, null);


            holder.FullName =  convertView.findViewById(R.id.header_title);
            holder.UserName =  convertView.findViewById(R.id.footer_username_data);
            holder.MobileNumber =  convertView.findViewById(R.id.footer_mobile_data);
            holder.FatherName =  convertView.findViewById(R.id.footer_parent_name_data);
            holder.FatherEmail =  convertView.findViewById(R.id.footer_email_data);
            holder.Class =  convertView.findViewById(R.id.header_class);
            holder.image =  convertView.findViewById(R.id.header_img);
            holder.username =  convertView.findViewById(R.id.username);
            holder.email =  convertView.findViewById(R.id.email);



            if (!WaitApprove) {


                holder.MENU_LEADERBORAD =  convertView.findViewById(R.id.menu_leaderboard);
                holder.MENU_MARK_SHEET =  convertView.findViewById(R.id.menu_marksheet);
                holder.MENU_ATTEND =  convertView.findViewById(R.id.menu_attend);

                holder.group =  convertView.findViewById(R.id.footer_group_data);
                holder.level =  convertView.findViewById(R.id.footer_level_data);
                holder.dateofjoining =  convertView.findViewById(R.id.footer_date_of_joining_data);
                holder.footer_attendence_rec =  convertView.findViewById(R.id.footer_attendence_rec);

                holder.footer_school_data =  convertView.findViewById(R.id.footer_school_data);
                holder.footer_age_data =  convertView.findViewById(R.id.footer_age_data);
                

            } else {
                holder.ApproveBtn =  convertView.findViewById(R.id.approve_btn);
                holder.DeleteBtn =  convertView.findViewById(R.id.delete_btn);
            }


            convertView.setTag(holder);
        } else {
            holder = (StaticPagesHolder) convertView.getTag();
        }
        posValues = LIST_DATA.get(position);
        if (posValues != null) {
            holder.FullName.setNotNullText(posValues.FullName);
            holder.UserName.setNotNullText(posValues.Username);
            holder.MobileNumber.setNotNullText(posValues.MobileNo);
            holder.FatherEmail.setNotNullText(posValues.FatherEmail);
            holder.FatherName.setNotNullText(posValues.FatherName);
            holder.Class.setNotNullText(posValues.Class+"\n"+posValues.studentSection);
            holder.image.profileID = String.valueOf(posValues.id);
            holder.image.load();

            if (!WaitApprove) {
                holder.MENU_LEADERBORAD.setTag(posValues);
                holder.MENU_MARK_SHEET.setTag(posValues.id);
                holder.MENU_ATTEND.setTag(posValues.id);

                holder.group.setText(posValues.phoneNo);
                holder.level.setText(posValues.cat_title);
                holder.dateofjoining.setText(posValues.admissionDate);
                holder.footer_attendence_rec.setText(posValues.attendanceRec);
                holder.footer_school_data.setText(posValues.school);
                holder.footer_age_data.setText(posValues.age);


            } else {
                holder.ApproveBtn.setTag(posValues.id);
                holder.DeleteBtn.setTag(posValues.id);
            }

            //Concurrent.setLangWords(this, holder.username, holder.email);
        }
        if (!WaitApprove) {
            if (Concurrent.isUserHavePermission(getBaseContext(), "students.stdLeaderBoard")) {
                //holder.MENU_LEADERBORAD.setVisibility(View.VISIBLE);
                holder.MENU_LEADERBORAD.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        StudentsModel clickedItem = (StudentsModel) v.getTag();
                        Concurrent.makeLeaderboard(StudentsPage.this,clickedItem.Leaderboard, App.getAppBaseUrl() + Constants.TASK_STUDNETS_LEADERBOARD + "/" + clickedItem.id);
                    }
                });
            } else {
                holder.MENU_LEADERBORAD.setVisibility(View.GONE);
            }

            if (Concurrent.isUserHavePermission(getBaseContext(), "students.Marksheet","Marksheet.Marksheet")) {
                holder.MENU_MARK_SHEET.setVisibility(View.VISIBLE);
                holder.MENU_MARK_SHEET.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent MyIntent = new Intent(getBaseContext(), ControlActivity.class);
                        MyIntent.putExtra("TARGET_FRAGMENT", "studentShowMarks");
                        MyIntent.putExtra("EXTRA_INT_1", (Integer) v.getTag());
                        MyIntent.putExtra("EXTRA_HEAD_FIND_WORD", "Marksheet");
                        MyIntent.putExtra("EXTRA_HEAD_REPLACE_WORD", "Marksheet");
                        startActivity(MyIntent);
                        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                    }
                });
            }else{
                holder.MENU_MARK_SHEET.setVisibility(View.GONE);
            }

            holder.MENU_MARK_SHEET.setVisibility(View.GONE);


            if (Concurrent.isUserHavePermission(getBaseContext(), "myAttendance.myAttendance","students.Attendance")) {
                holder.MENU_ATTEND.setVisibility(View.VISIBLE);
                holder.MENU_ATTEND.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent MyIntent = new Intent(getBaseContext(), ControlActivity.class);
                        MyIntent.putExtra("TARGET_FRAGMENT", "studentAttendance");
                        MyIntent.putExtra("EXTRA_INT_1", (Integer) v.getTag());
                        MyIntent.putExtra("EXTRA_HEAD_FIND_WORD", "Attendance");
                        MyIntent.putExtra("EXTRA_HEAD_REPLACE_WORD", "Attendance");
                        startActivity(MyIntent);
                        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                    }
                });

            }else{
                holder.MENU_ATTEND.setVisibility(View.GONE);
            }

        } else {
            holder.ApproveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Concurrent.ApproveUser(StudentsPage.this, App.getAppBaseUrl() + Constants.TASK_APPROVE_STUDENT + "/" + v.getTag());

                }
            });
            holder.DeleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Concurrent.deleteItem(StudentsPage.this, App.getAppBaseUrl() + Constants.TASK_STUDENTS + "/delete/" + v.getTag());
                }
            });
        }

        return convertView;
    }

    class StaticPagesHolder {
        public TextView username;
        public TextView email;
        public TextView group;
        public TextView level;
        public TextView dateofjoining;
        public TextView footer_attendence_rec;
        RelativeLayout MENU_LEADERBORAD;
        RelativeLayout MENU_MARK_SHEET;
        RelativeLayout MENU_ATTEND;
        RelativeLayout ApproveBtn;
        RelativeLayout DeleteBtn;
        ParentStyledTextView FullName;
        ParentStyledTextView UserName;
        ParentStyledTextView MobileNumber;
        ParentStyledTextView FatherName;
        ParentStyledTextView FatherEmail;
        ParentStyledTextView Class;
        ParentStyledTextView footer_school_data;
        ParentStyledTextView footer_age_data;
        CustomImageView image;


    }
}
