package com.schoolmgmtsys.root.ssg.app;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
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
import com.schoolmgmtsys.root.ssg.models.ParentsModel;
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

public class ParentsPage extends SlidingFragmentActivity implements ListManager.ListInterface {

    private ListView ViewList;
    private ListManager mListManager;
    private StaticPagesHolder holder;

    private ParentsModel posValues;
    private ArrayList<ParentsModel> LIST_DATA = new ArrayList<ParentsModel>();
    private Integer Res_PageLayout = R.layout.page_parents;
    private Integer Res_PageList = R.id.parents_view_list;
    private Integer Res_PageItemList = R.layout.page_parents_list_item;
    private Integer Res_PageApproveItemList = R.layout.page_parents_approve_item;

    private String TOKEN;
    private boolean Locked;
    private ProgressBar mProgressBar;
    private int nextPage = 1;
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

        getSlidingMenu().setBehindWidth((int) Math.round(Concurrent.getScreenWidth(this) * 70.0 / 100.0));
        setBehindContentView(R.layout.drawer_frame);

        ImageView backImage = (ImageView) findViewById(R.id.background_img);
        FrameLayout logBack = (FrameLayout) findViewById(R.id.full_layout);

        Resources res = getResources();
        if (!res.getBoolean(R.bool.x_gen_back_is_image)) {
            backImage.setVisibility(View.GONE);
            logBack.setBackgroundColor(Concurrent.getColor(this, R.color.x_gen_back));
        }

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
                MyIntent.putExtra("EXTRA_STRING_1", "ParentsPage");
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
                Intent MyIntent = new Intent(getBaseContext(), ParentsPage.class);
                MyIntent.putExtra("TARGET_LOAD", "WaitApprove");
                startActivity(MyIntent);
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            }
        });
        if (!WaitApprove && Concurrent.isUserHavePermission(this, "parents.Approve")) {
            WaitingApprove.setVisibility(View.VISIBLE);
        } else {
            WaitingApprove.setVisibility(View.GONE);
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
        HeadTitle.setText(Concurrent.getLangSubWords("parents", "Parents"));

        ImageView ToogleBtn = (ImageView) findViewById(R.id.head_drawer_toggle);
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
                String mLink = App.getAppBaseUrl() + Constants.TASK_PARENTS_WAIT_APPROVE;


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
                                                Toast.makeText(ParentsPage.this, Concurrent.getLangSubWords("errorOccurred","Error Occurred"), Toast.LENGTH_LONG).show();
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
                                                        if (title != null && !title.equals(""))
                                                            LIST_DATA.add(new ParentsModel(Concurrent.tagsIntValidator(CurrObj, "id"), Concurrent.tagsStringValidator(CurrObj, "fullName"), title, Concurrent.tagsStringValidator(CurrObj, "email"), Concurrent.tagsStringValidator(CurrObj, "parentOf"), Concurrent.tagsStringValidator(CurrObj, "mobileNo")));
                                                    }

                                                    mListManager.getListAdapter().notifyDataSetChanged();
                                                } else {
                                                    mListManager.removeFooter();
                                                }
                                                if (LIST_DATA.size() == 0)
                                                    mListManager.setNoDataView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(Res_PageList));
                                                else
                                                    mListManager.setDataView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(Res_PageList));

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
            if (TOKEN != null) {
                String mLink = App.getAppBaseUrl() + Constants.TASK_PARENTS_LIST + "/" + page;

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
                                                Toast.makeText(ParentsPage.this, Concurrent.getLangSubWords("errorOccurred", "Error Occurred"), Toast.LENGTH_LONG).show();
                                            }
                                            if (ValuesHolder == null || !ValuesHolder.has("parents")) {
                                                Toast.makeText(ParentsPage.this, Concurrent.getLangSubWords("errorOccurred", "Error Occurred"), Toast.LENGTH_LONG).show();
                                                mListManager.setErrorView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(Res_PageList));
                                                mProgressBar.setVisibility(View.INVISIBLE);
                                                mRefresh.setVisibility(View.VISIBLE);
                                                return;
                                            }

                                            JsonArray ValuesArray = ValuesHolder.getAsJsonArray("parents");

                                            if (ValuesArray != null) {
                                                if (ValuesArray.size() > 0) {
                                                    if (page == null || page == 1) LIST_DATA.clear();
                                                    for (JsonElement aValuesArray : ValuesArray) {
                                                        JsonObject CurrObj = aValuesArray.getAsJsonObject();
                                                        String title = Concurrent.tagsStringValidator(CurrObj, "username");
                                                        if (title != null && !title.equals(""))
                                                            LIST_DATA.add(new ParentsModel(Concurrent.tagsIntValidator(CurrObj, "id"), Concurrent.tagsStringValidator(CurrObj, "fullName"), title, Concurrent.tagsStringValidator(CurrObj, "email"), Concurrent.tagsStringValidator(CurrObj, "parentOf"), Concurrent.tagsStringValidator(CurrObj, "mobileNo")));
                                                    }

                                                    mListManager.getListAdapter().notifyDataSetChanged();
                                                    nextPage = page + 1;
                                                } else {
                                                    mListManager.removeFooter();
                                                }
                                                if (LIST_DATA.size() == 0)
                                                    mListManager.setNoDataView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(Res_PageList));
                                                else
                                                    mListManager.setDataView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(Res_PageList));

                                            } else {
                                                mListManager.removeFooter();
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

            holder.FullName = (ParentStyledTextView) convertView.findViewById(R.id.header_title);
            holder.UserName = (ParentStyledTextView) convertView.findViewById(R.id.footer_username_data);
            holder.Phone = (ParentStyledTextView) convertView.findViewById(R.id.footer_phone_data);
            holder.Email = (ParentStyledTextView) convertView.findViewById(R.id.footer_email_data);
            holder.StudentImg = (CustomImageView) convertView.findViewById(R.id.header_img);
            holder.username = (TextView) convertView.findViewById(R.id.username);
            holder.email = (TextView) convertView.findViewById(R.id.email);

            if (WaitApprove) {
                holder.ApproveBtn = (RelativeLayout) convertView.findViewById(R.id.approve_btn);
                holder.DeleteBtn = (RelativeLayout) convertView.findViewById(R.id.delete_btn);
            }

            convertView.setTag(holder);
        } else {
            holder = (StaticPagesHolder) convertView.getTag();
        }
        posValues = LIST_DATA.get(position);
        if (posValues != null) {
            String nextline = "";
            String namesOfStudents = "";

            JsonParser parser = new JsonParser();
            if(parser.parse(posValues.parentOf).isJsonArray()){
            JsonArray ValuesArray =  parser.parse(posValues.parentOf).getAsJsonArray();
//            ValuesArray.get(0);

            for (int i = 0 ; i < ValuesArray.size(); i++) {

                JsonObject object3 = ValuesArray.get(i).getAsJsonObject();
                String comp_id = object3.get("student").getAsString();

                namesOfStudents = namesOfStudents+nextline+object3.get("student").getAsString();
                nextline=" \n";


            }
            }
            holder.FullName.setNotNullText(posValues.FullName);
            holder.UserName.setNotNullText(namesOfStudents);
            holder.Email.setNotNullText(posValues.Email);
            holder.Phone.setNotNullText(posValues.mobileNo);
            holder.StudentImg.profileID = String.valueOf(posValues.id);
            holder.StudentImg.load();

            if (WaitApprove) {
                holder.ApproveBtn.setTag(posValues.id);
                holder.DeleteBtn.setTag(posValues.id);
            }

            Concurrent.setLangWords(this, holder.email);
            //Concurrent.setLangWords(this, holder.username, holder.email);

        }

        if (WaitApprove) {
            holder.ApproveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Concurrent.ApproveUser(ParentsPage.this, App.getAppBaseUrl() + Constants.TASK_APPROVE_PARENTS + "/" + v.getTag());

                }
            });
            holder.DeleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Concurrent.deleteItem(ParentsPage.this, App.getAppBaseUrl() + Constants.TASK_PARENTS + "/delete/" + v.getTag());
                }
            });
        }

        return convertView;
    }

    class StaticPagesHolder {
        public TextView username;
        public TextView email;
        RelativeLayout ApproveBtn;
        RelativeLayout DeleteBtn;
        ParentStyledTextView FullName;
        ParentStyledTextView UserName;
        ParentStyledTextView Email;
        ParentStyledTextView Phone;
        CustomImageView StudentImg;
    }
}
