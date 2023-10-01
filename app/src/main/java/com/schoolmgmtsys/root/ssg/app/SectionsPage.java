package com.schoolmgmtsys.root.ssg.app;

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
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.schoolmgmtsys.root.ssg.models.GradesModel;
import com.schoolmgmtsys.root.ssg.models.SectionsModel;
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
import com.solutionsbricks.solbricksframework.helpers.GsonParser;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class SectionsPage extends SlidingFragmentActivity {

    private ListView ViewList;
    private ArrayList<GradesModel> LIST_DATA = new ArrayList<>();
    private Integer Res_PageLayout = R.layout.page_sections;
    private Integer Res_PageList = R.id.sections_list;
    private String TOKEN;
    private ProgressBar mProgressBar;
    private boolean TokenRetry;
    private ImageView mRefresh;
    private boolean searchViewInit;
    private ArrayList<NLevelItem> NLListDATA;
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
        HeadTitle.setText(Concurrent.getLangSubWords("sections", "Sections"));

        ImageView ToogleBtn = (ImageView) findViewById(R.id.head_drawer_toggle);
        ToogleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        mProgressBar.setVisibility(View.INVISIBLE);
        ViewList = (ListView) findViewById(Res_PageList);


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
        }
    }


    public void loadData() {
        TOKEN = Concurrent.getAppToken(this);
        NLListDATA = new ArrayList<>();
        final LayoutInflater inflater = LayoutInflater.from(this);

        if (TOKEN != null) {
            mProgressBar.setVisibility(View.VISIBLE);
            mRefresh.setVisibility(View.INVISIBLE);




            OkHttpClient client = new OkHttpClient().newBuilder(getBaseContext()).connectTimeout(7, TimeUnit.SECONDS).build();

            Request.Builder requestBuilder = new Request.Builder()
                    .url(App.getAppBaseUrl() + Constants.TASK_SECTIONS_LIST);

            requestBuilder.get();

            Request request = requestBuilder.build();


            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, final IOException e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
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

                                        if (ValuesHolder != null) {

                                            LIST_DATA.clear();
                                            GsonParser parserManager = new GsonParser();
                                            HashMap<Integer, String> teachersItems = new HashMap<>();
                                            HashMap<Object, Object> sectionsItems = parserManager.getItemsFromJsonObject(ValuesHolder, "sections");

                                            JsonObject teachersArray = ValuesHolder.get("teachers").getAsJsonObject();
                                            if (teachersArray != null) {
                                                for (Map.Entry<String, JsonElement> entry : teachersArray.entrySet()) {
                                                    teachersItems.put(Integer.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
                                                }

                                            }

                                            if (sectionsItems != null && sectionsItems.size() > 0) {
                                                for (Map.Entry<Object, Object> entry : sectionsItems.entrySet()) {
                                                    String ClassName = (String) entry.getKey();

                                                    final NLevelItem grandParent = new NLevelItem(ClassName, null, new NLevelView() {

                                                        @Override
                                                        public View getView(NLevelItem item) {
                                                            View view = inflater.inflate(R.layout.page_sections_parent, null);
                                                            TextView tv = (TextView) view.findViewById(R.id.parent_title);
                                                            String name = (String) item.getWrappedObject();
                                                            tv.setText(name);
                                                            return view;
                                                        }
                                                    });
                                                    if(!ClassName.equalsIgnoreCase("Graduate Students") && !ClassName.equalsIgnoreCase("Old students"))
                                                    NLListDATA.add(grandParent);

                                                    JsonArray SectionData = (JsonArray) entry.getValue();
                                                    if (SectionData.size() > 0) {
                                                        for (JsonElement aSectionData : SectionData) {
                                                            JsonObject CurrObj = aSectionData.getAsJsonObject();

                                                            JsonArray TeachersIDs = CurrObj.getAsJsonArray("teacherId");

                                                            String teachersNames = "";
                                                            if (TeachersIDs.size() > 0) {

                                                                Iterator<JsonElement> TeachersIter = TeachersIDs.iterator();
                                                                while (TeachersIter.hasNext()) {
                                                                    JsonElement item = TeachersIter.next();

                                                                    int key = Integer.parseInt(Concurrent.repairJsonValueQuotes(item.getAsString()));

                                                                    if (teachersItems.size() > 0) {
                                                                        if (teachersItems.containsKey(key)) {
                                                                            teachersNames = teachersNames.concat(Concurrent.repairJsonValueQuotes(teachersItems.get(key)));
                                                                            if (TeachersIter.hasNext())
                                                                                teachersNames = teachersNames.concat(", ");

                                                                        }
                                                                    }
                                                                }
                                                            }

                                                            NLevelItem child = new NLevelItem(new SectionsModel(Concurrent.tagsIntValidator(CurrObj, "id"), ClassName, Concurrent.tagsStringValidator(CurrObj, "sectionName"), Concurrent.tagsStringValidator(CurrObj, "sectionTitle"), teachersNames), grandParent, new NLevelView() {

                                                                @Override
                                                                public View getView(NLevelItem item) {


                                                                    View view = inflater.inflate(R.layout.page_sections_child, null);
                                                                    TextView SectionName = (TextView) view.findViewById(R.id.section_name);
                                                                    TextView SectionTitle = (TextView) view.findViewById(R.id.section_title);
                                                                    TextView SectionTeachers = (TextView) view.findViewById(R.id.section_teachers_data);

                                                                    SectionsModel section = (SectionsModel) item.getWrappedObject();
                                                                    SectionName.setText(section.SectionName);
                                                                    SectionTitle.setText(section.SectionTitle);
                                                                    SectionTeachers.setText(section.SectionTeacher);

                                                                    Concurrent.setLangWords(SectionsPage.this, ((TextView) view.findViewById(R.id.teachers)));
                                                                    return view;
                                                                }
                                                            });
                                                            if(!ClassName.equalsIgnoreCase("Graduate Students") && !ClassName.equalsIgnoreCase("Old students"))
                                                            NLListDATA.add(child);
                                                        }
                                                    }
                                                }
                                                NLevelAdapter adapter = new NLevelAdapter(NLListDATA);
                                                ViewList.setAdapter(adapter);
                                                ViewList.setOnItemClickListener(new OnItemClickListener() {
                                                    @Override
                                                    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                                                        ((NLevelAdapter) ViewList.getAdapter()).toggle(arg2);
                                                        ((NLevelAdapter) ViewList.getAdapter()).getFilter().filter();
                                                    }
                                                });

                                            }

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
                    }else{
                        showError("5001");
                    }
                    TokenRetry = true;

                }
            });

        } else {
            showError("5010");
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
        mProgressBar.setVisibility(INVISIBLE);
        mRefresh.setVisibility(VISIBLE);
        Toast.makeText(getBaseContext(), errorTitle, Toast.LENGTH_LONG).show();
    }
}
