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
import com.schoolmgmtsys.root.ssg.models.MaterialModel;
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
import com.schoolmgmtsys.root.ssg.fonts.MediumStyledTextView;
import com.schoolmgmtsys.root.ssg.fonts.RegularStyledTextView;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class StudyMaterialPage extends SlidingFragmentActivity implements ListManager.ListInterface {

    private ListView ViewList;
    private ListManager mListManager;
    private StaticPagesHolder holder;

    private MaterialModel posValues;
    private ArrayList<MaterialModel> LIST_DATA = new ArrayList<>();
    private Integer Res_PageLayout = R.layout.page_material;
    private Integer Res_PageList = R.id.material_view_list;
    private Integer Res_PageItemList = R.layout.page_material_list_item;
    private String TOKEN;
    private ProgressBar mProgressBar;
    private boolean TokenRetry;
    private ImageView mRefresh;
    private boolean searchViewInit;
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
                    MyIntent.putExtra("EXTRA_STRING_1", "StudyMaterialPage");
                    MyIntent.putExtra("EXTRA_HEAD_FIND_WORD", "Search");
                    MyIntent.putExtra("EXTRA_HEAD_REPLACE_WORD", "Search");
                    MyIntent.putExtra("EXTRA_LIST", LIST_DATA);
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
        TextView HeadTitle = (TextView) findViewById(R.id.head_drawer_title);
        HeadTitle.setText(Concurrent.getLangSubWords("studyMaterial", "Study Material"));

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
            mProgressBar.setVisibility(View.VISIBLE);
            mRefresh.setVisibility(View.INVISIBLE);


            OkHttpClient client = new OkHttpClient().newBuilder(getBaseContext()).connectTimeout(7, TimeUnit.SECONDS).build();

            Request.Builder requestBuilder = new Request.Builder()
                    .url(App.getAppBaseUrl() + Constants.TASK_STUDY_MATERIALS_LIST);

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

                                        if (ValuesHolder != null && ValuesHolder.has("materials")) {

                                            JsonArray ValuesArray = ValuesHolder.getAsJsonArray("materials");

                                            if (ValuesArray.size() == 0 || ValuesArray.isJsonNull()) {
                                                mListManager.setNoDataView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(Res_PageList));
                                                mProgressBar.setVisibility(View.INVISIBLE);
                                                mRefresh.setVisibility(View.VISIBLE);
                                                return;
                                            }


                                            LIST_DATA.clear();
                                            for (JsonElement aValuesArray : ValuesArray) {
                                                JsonObject CurrObj = aValuesArray.getAsJsonObject();
                                                String title = Concurrent.tagsStringValidator(CurrObj, "material_title");
                                                if (title != null && !title.equals(""))
                                                    LIST_DATA.add(new MaterialModel(Concurrent.tagsIntValidator(CurrObj, "id"), Concurrent.tagsStringValidator(CurrObj, "subjectId"), Concurrent.tagsStringValidator(CurrObj, "subject"), title, Concurrent.tagsStringValidator(CurrObj, "material_description"), Concurrent.tagsStringValidator(CurrObj, "material_file"), Concurrent.tagsStringValidator(CurrObj, "classes").trim().replaceAll(",$", "")));
                                            }
                                            if (LIST_DATA.size() == 0)
                                                mListManager.setNoDataView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(Res_PageList));
                                            else
                                                mListManager.setDataView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(Res_PageList));

                                            mListManager.getListAdapter().notifyDataSetChanged();

                                            mProgressBar.setVisibility(INVISIBLE);
                                            mRefresh.setVisibility(VISIBLE);
                                            TokenRetry = false;

                                        } else {
                                            showError("5001");
                                        }
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
        if (convertView == null || convertView.getTag() == null) {
            holder = new StaticPagesHolder();
            convertView = inflater.inflate(Res_PageItemList, null);

            holder.Title = (MediumStyledTextView) convertView.findViewById(R.id.header_title);
            holder.Description = (RegularStyledTextView) convertView.findViewById(R.id.header_content);
            holder.SubjectData = (RegularStyledTextView) convertView.findViewById(R.id.footer_subject_data);
            holder.ClassesData = (RegularStyledTextView) convertView.findViewById(R.id.footer_classes_data);
            holder.MENU_DOWNLOAD = (RelativeLayout) convertView.findViewById(R.id.menu_download);

            holder.Subject = (TextView) convertView.findViewById(R.id.Subject);
            holder.classes = (TextView) convertView.findViewById(R.id.classes);

            convertView.setTag(holder);
        } else {
            holder = (StaticPagesHolder) convertView.getTag();
        }
        posValues = LIST_DATA.get(position);
        if (posValues != null) {
            holder.Title.setNotNullText(posValues.materialTitle);
            holder.Description.setNotNullText(posValues.materialDescription);
            holder.SubjectData.setNotNullText(posValues.subjectTitle);
            holder.ClassesData.setNotNullText(posValues.classes);
            if (posValues.materialFile != null) {
                holder.MENU_DOWNLOAD.setVisibility(View.VISIBLE);
                holder.MENU_DOWNLOAD.setTag(posValues);
            } else holder.MENU_DOWNLOAD.setVisibility(View.GONE);

            Concurrent.setLangWords(this, holder.Subject, holder.classes);
        }
        /*holder.MENU_DELETE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Concurrent.deleteItem(StudyMaterialPage.this, App.getAppBaseUrl() + Constants.TASK_MATERIAL_CONTROL + "/delete/" + v.getTag());
            }
        });*/
        if (posValues != null && posValues.materialFile != null) {
            holder.MENU_DOWNLOAD.setOnClickListener(new View.OnClickListener() {


                @Override
                public void onClick(final View v) {
                    Integer itemID = ((MaterialModel) (v.getTag())).id;
                    String itemFILE = ((MaterialModel) (v.getTag())).materialFile;

                    new Downloader().downloadFile(StudyMaterialPage.this, itemFILE, App.getAppBaseUrl() + Constants.TASK_MATERIAL_CONTROL + "/download/" + itemID);
                }
            });
        }
        return convertView;
    }


    class StaticPagesHolder {
        RelativeLayout MENU_DOWNLOAD;
        MediumStyledTextView Title;
        RegularStyledTextView Description;
        RegularStyledTextView SubjectData;
        RegularStyledTextView ClassesData;

        TextView Subject;
        TextView classes;
    }
}
