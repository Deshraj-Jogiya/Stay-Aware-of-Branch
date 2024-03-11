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
import com.schoolmgmtsys.root.ssg.models.LibraryModel;
import com.schoolmgmtsys.root.ssg.R;
import com.schoolmgmtsys.root.ssg.utils.App;
import com.schoolmgmtsys.root.ssg.utils.Concurrent;
import com.schoolmgmtsys.root.ssg.utils.Constants;
import com.schoolmgmtsys.root.ssg.utils.Downloader;
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

public class LibraryPage extends SlidingFragmentActivity implements ListManager.ListInterface {

    private ListManager mListManager;

    private ArrayList<LibraryModel> LIST_DATA = new ArrayList<LibraryModel>();
    private Integer Res_PageLayout = R.layout.page_library;
    private Integer Res_PageList = R.id.library_view_list;
    private Integer Res_PageItemList = R.layout.page_library_list_item;
    private ProgressBar mProgressBar;
    private int nextPage = 1;
    private boolean Locked;
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
                MyIntent.putExtra("EXTRA_STRING_1", "LibraryPage");
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
        HeadTitle.setText(Concurrent.getLangSubWords("booksLibrary", "Books library"));

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
            String TOKEN = Concurrent.getAppToken(this);
            if (TOKEN != null) {
                Locked = true;
                mProgressBar.setVisibility(View.VISIBLE);
                mRefresh.setVisibility(View.INVISIBLE);


                String mLink;
                if (EXTRA_SEARCH != null && !EXTRA_SEARCH.equals(""))
                    mLink = App.getAppBaseUrl() + Constants.TASK_LIBRARY_SEARCH + "/" + EXTRA_SEARCH + "/" + page;
                else mLink = App.getAppBaseUrl() + Constants.TASK_LIBRARY_LIST + "/" + page;


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

                                            if (ValuesHolder != null && ValuesHolder.has("bookLibrary")) {

                                                JsonArray ValuesArray = ValuesHolder.getAsJsonArray("bookLibrary");

                                                if (ValuesArray.size() > 0) {
                                                    if (page == null || page == 1) LIST_DATA.clear();

                                                    for (JsonElement aValuesArray : ValuesArray) {
                                                        JsonObject CurrObj = aValuesArray.getAsJsonObject();
                                                        String title = Concurrent.tagsStringValidator(CurrObj, "bookName");
                                                        if (title != null && !title.equals(""))
                                                            LIST_DATA.add(new LibraryModel(Concurrent.tagsIntValidator(CurrObj, "id"), title, Concurrent.tagsStringValidator(CurrObj, "bookAuthor"), Concurrent.tagsStringValidator(CurrObj, "bookType"), Concurrent.tagsStringValidator(CurrObj, "bookPrice"), Concurrent.tagsStringValidator(CurrObj, "bookFile")));
                                                    }
                                                    nextPage = page + 1;
                                                } else {
                                                    mListManager.removeFooter();
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
                                            Locked = false;
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
        ViewHolder holder;
        if (convertView == null || convertView.getTag() == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(Res_PageItemList, null);

            holder.TITLE =  convertView.findViewById(R.id.header_title);
            holder.AUTHOR =  convertView.findViewById(R.id.footer_author_data);
            holder.STATE =  convertView.findViewById(R.id.header_state);
            holder.PRICE =  convertView.findViewById(R.id.footer_price_data);
            holder.MENU_DOWNLOAD =  convertView.findViewById(R.id.menu_download);
            holder.MENU_FARE =  convertView.findViewById(R.id.fare_con);
            holder.bookAuthor = convertView.findViewById(R.id.bookAuthor);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        LibraryModel posValues = LIST_DATA.get(position);
        if (posValues != null) {
            holder.TITLE.setNotNullText(posValues.title);
            holder.AUTHOR.setNotNullText(posValues.author + " - ");
            if(posValues.state.equals("traditional")){
                holder.STATE.setNotNullText(Concurrent.getLangSubWords("traditionalBook","Traditional Book"));
            }else if(posValues.state.equals("electronic")){
                holder.STATE.setNotNullText(Concurrent.getLangSubWords("electronicBook","Electronic Book"));
            }

            if(posValues.price != null && !posValues.price.equals("")){
                holder.MENU_FARE.setVisibility(View.VISIBLE);
                holder.PRICE.setNotNullText(posValues.price);
            }else{
                holder.MENU_FARE.setVisibility(View.GONE);
            }

            Concurrent.setLangWords(this, holder.bookAuthor);

            if (posValues.bookFile != null) {
                if (Concurrent.isUserHavePermission(this, "Library.Download")) {
                    holder.MENU_DOWNLOAD.setVisibility(View.VISIBLE);
                    holder.MENU_DOWNLOAD.setTag(posValues);
                    holder.MENU_DOWNLOAD.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Integer itemID = ((LibraryModel) (v.getTag())).id;
                            String itemFILE = ((LibraryModel) (v.getTag())).bookFile;
                            new Downloader().downloadFile(LibraryPage.this, itemFILE, App.getAppBaseUrl() + Constants.TASK_LIBRARY_CONTROL + "/download/" + itemID);
                        }
                    });
                }else{
                    holder.MENU_DOWNLOAD.setVisibility(View.GONE);
                }
            }else{
                holder.MENU_DOWNLOAD.setVisibility(View.GONE);
            }

        }


        return convertView;
    }

    class ViewHolder {
        public TextView bookAuthor;
        ParentStyledTextView PRICE;
        ParentStyledTextView STATE;
        ParentStyledTextView AUTHOR;
        ParentStyledTextView TITLE;
        RelativeLayout MENU_DOWNLOAD;
        LinearLayout MENU_FARE;
    }
}
