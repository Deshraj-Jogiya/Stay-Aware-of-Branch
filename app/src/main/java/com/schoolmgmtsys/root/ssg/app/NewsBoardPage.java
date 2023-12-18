package com.schoolmgmtsys.root.ssg.app;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.schoolmgmtsys.root.ssg.models.NewsBoardModel;
import com.schoolmgmtsys.root.ssg.R;
import com.schoolmgmtsys.root.ssg.utils.App;
import com.schoolmgmtsys.root.ssg.utils.Concurrent;
import com.schoolmgmtsys.root.ssg.utils.Constants;
import com.schoolmgmtsys.root.ssg.utils.DrawerListFragment;
import com.solutionsbricks.solbricksframework.Call;
import com.solutionsbricks.solbricksframework.Callback;
import com.solutionsbricks.solbricksframework.FormBody;
import com.solutionsbricks.solbricksframework.OkHttpClient;
import com.solutionsbricks.solbricksframework.Request;
import com.solutionsbricks.solbricksframework.Response;
import com.solutionsbricks.solbricksframework.helpers.ListManager;
import com.schoolmgmtsys.root.ssg.fonts.ParentStyledTextView;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class NewsBoardPage extends SlidingFragmentActivity implements ListManager.ListInterface {

    private ListView ViewList;
    private ListManager mListManager;
    private StaticPagesHolder holder;

    private NewsBoardModel posValues;
    private ArrayList<NewsBoardModel> LIST_DATA = new ArrayList<NewsBoardModel>();
    private Integer Res_PageLayout = R.layout.page_news;
    private Integer Res_PageList = R.id.news_view_list;
    private Integer Res_PageItemList = R.layout.page_news_list_item;
    private String TOKEN;
    private boolean Locked;
    private ProgressBar mProgressBar;
    private int nextPage = 1;
    private boolean TokenRetry;
    private ImageView mRefresh;
    private String EXTRA_SEARCH;
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
                MyIntent.putExtra("EXTRA_STRING_1", "NewsBoardPage");
                MyIntent.putExtra("EXTRA_HEAD_FIND_WORD", "Search");
                MyIntent.putExtra("EXTRA_HEAD_REPLACE_WORD", "Search");
                startActivity(MyIntent);
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            }
        });

        mProgressBar = (ProgressBar) findViewById(R.id.gen_loader);
        mRefresh = (ImageView) findViewById(R.id.refresh);
        mRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextPage = 1;
                loadData(nextPage);
            }
        });
        mRefresh.setVisibility(View.VISIBLE);
        TextView HeadTitle = (TextView) findViewById(R.id.head_drawer_title);
        HeadTitle.setText(Concurrent.getLangSubWords("newsboard", "News Board"));

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
                nextPage = 1;
                loadData(nextPage);
            }
        });
        findViewById(R.id.empty_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextPage = 1;
                loadData(nextPage);
            }
        });

        nextPage = 1;
        loadData(nextPage);
    }


    public void loadData(final Integer page) {
        if (!Locked) {
            Locked = true;
            mProgressBar.setVisibility(View.VISIBLE);
            mRefresh.setVisibility(View.INVISIBLE);
            TOKEN = Concurrent.getAppToken(this);
            if (TOKEN != null) {
                String mLink;
                if (EXTRA_SEARCH != null && !EXTRA_SEARCH.equals(""))
                    mLink = App.getAppBaseUrl() + Constants.TASK_NEWS_SEARCH + "/" + EXTRA_SEARCH + "/" + page;
                else mLink = App.getAppBaseUrl() + Constants.TASK_NEWS_LIST + "/" + page;


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

                                            if (ValuesHolder != null && ValuesHolder.has("newsboard")) {

                                                JsonArray ValuesArray = ValuesHolder.getAsJsonArray("newsboard");

                                                if (ValuesArray.size() > 0) {
                                                    if (page == null || page == 1) LIST_DATA.clear();

                                                    for (JsonElement aValuesArray : ValuesArray) {
                                                        JsonObject CurrObj = aValuesArray.getAsJsonObject();
                                                        String title = Concurrent.tagsStringValidator(CurrObj, "newsTitle");
                                                        if (title != null && !title.equals(""))
                                                            LIST_DATA.add(new NewsBoardModel(Concurrent.tagsIntValidator(CurrObj, "id"), title, Concurrent.tagsStringValidator(CurrObj, "newsText"), Concurrent.tagsStringValidator(CurrObj, "newsFor"), Concurrent.tagsStringValidator(CurrObj, "newsDate")));
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
                                                showError("5001");
                                            }

                                        } else {
                                            renewToken();
                                        }
                                    } catch (final Exception e) {
                                        showError("5002");
                                    }
                                    mProgressBar.setVisibility(INVISIBLE);
                                    mRefresh.setVisibility(VISIBLE);
                                    TokenRetry = false;
                                    Locked = false;
                                }
                            });
                        }else{
                            showError("5001");
                        }
                    }
                });
            } else {
                showError(null);
                Locked = false;
                mProgressBar.setVisibility(View.INVISIBLE);
            }
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
                                                nextPage = 1;
                                                loadData(nextPage);

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
        loadData(nextPage);
    }

    @Override
    public void AdapterConstructor() {

    }

    @Override
    public View AdapterGetView(int position, View convertView) {
        LayoutInflater inflater = getLayoutInflater();
        if (convertView == null || convertView.getTag() == null) {
            holder = new StaticPagesHolder();
            convertView = inflater.inflate(Res_PageItemList, null);

            holder.card = (CardView) convertView.findViewById(R.id.card);

            holder.TITLE = (ParentStyledTextView) convertView.findViewById(R.id.header_title);
            holder.CONTENT = (ParentStyledTextView) convertView.findViewById(R.id.header_content);
            holder.DATE = (ParentStyledTextView) convertView.findViewById(R.id.header_date);

            convertView.setTag(holder);
        } else {
            holder = (StaticPagesHolder) convertView.getTag();
        }
        posValues = LIST_DATA.get(position);
        if (posValues != null) {
            holder.CONTENT.setNotNullText(Concurrent.fromHtml(posValues.content));
            holder.TITLE.setNotNullText(posValues.title);
            holder.DATE.setNotNullText(posValues.date);

            holder.card.setTag(posValues.id);
            if (Concurrent.isUserHavePermission(getBaseContext(), "newsboard.View")) {
                holder.card.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent MyIntent = new Intent(NewsBoardPage.this, NewsViewPage.class);
                        MyIntent.putExtra("PageID", (Integer) v.getTag());
                        startActivity(MyIntent);
                        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                    }
                });
            }



        }
        return convertView;
    }

    class StaticPagesHolder {
        ParentStyledTextView CONTENT;
        ParentStyledTextView TITLE;
        ParentStyledTextView DATE;
        public CardView card;
    }
}
