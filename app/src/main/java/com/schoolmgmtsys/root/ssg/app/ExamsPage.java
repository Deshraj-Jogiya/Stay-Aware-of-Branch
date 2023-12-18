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
import com.schoolmgmtsys.root.ssg.models.ClassesModel;
import com.schoolmgmtsys.root.ssg.models.ExamSubjectModel;
import com.schoolmgmtsys.root.ssg.models.ExamsModel;
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
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class ExamsPage extends SlidingFragmentActivity implements ListManager.ListInterface {

    private ListView ViewList;
    private ListManager mListManager;
    private StaticPagesHolder holder;

    private ExamsModel posValues;
    private ArrayList<ExamsModel> LIST_DATA = new ArrayList<>();
    private Integer Res_PageLayout = R.layout.page_exams;
    private Integer Res_PageList = R.id.exams_view_list;
    private Integer Res_PageItemList = R.layout.page_exams_list_item;
    private String TOKEN;
    private JsonArray ClassesValuesArray;
    private ArrayList<ClassesModel> ClassesList;
    private ProgressBar mProgressBar;
    private boolean TokenRetry;
    private ImageView mRefresh;
    private boolean searchViewInit;
    private ArrayList<ExamSubjectModel> subjectsList = new ArrayList<>();
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
                    MyIntent.putExtra("EXTRA_STRING_1", "ExamsPage");
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
        /*mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent MyIntent = new Intent(getBaseContext(), ControlActivity.class);
                MyIntent.putExtra("TARGET_FRAGMENT", "ExamsListControl");
                startActivity(MyIntent);
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            }
        });*/

        mRefresh.setVisibility(View.VISIBLE);
        TextView HeadTitle = (TextView) findViewById(R.id.head_drawer_title);
        HeadTitle.setText(Concurrent.getLangSubWords("examsList", "Exams List"));

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
                    .url(App.getAppBaseUrl() + Constants.TASK_EXAMS_LIST);

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

                                        if(ValuesHolder == null){
                                            mListManager.setNoDataView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(Res_PageList));
                                            mProgressBar.setVisibility(View.INVISIBLE);
                                            mRefresh.setVisibility(View.VISIBLE);
                                            return;
                                        }

                                        JsonObject jsonSubjectsList = ValuesHolder.get("subjects").getAsJsonObject();
                                        if (jsonSubjectsList != null) {
                                            for (Map.Entry<String, JsonElement> entry : jsonSubjectsList.entrySet()) {
                                                subjectsList.add(new ExamSubjectModel(entry.getKey(),entry.getValue().getAsString()));
                                            }
                                        }

                                        JsonArray ValuesArray = ValuesHolder.getAsJsonArray("exams");

                                        if (ValuesArray.size() == 0 || ValuesArray.isJsonNull()) {
                                            mListManager.setNoDataView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(Res_PageList));
                                            mProgressBar.setVisibility(View.INVISIBLE);
                                            mRefresh.setVisibility(View.VISIBLE);
                                            return;
                                        }

                                        ClassesValuesArray = ValuesHolder.getAsJsonArray("classes");

                                        if (ClassesValuesArray.size() == 0 || ClassesValuesArray.isJsonNull()) {
                                            mListManager.setNoDataView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(Res_PageList));
                                            mProgressBar.setVisibility(View.INVISIBLE);
                                            mRefresh.setVisibility(View.VISIBLE);
                                            return;
                                        }

                                        JsonObject CurrObj;
                                        if (ValuesArray.size() > 0) {
                                            LIST_DATA.clear();

                                            for (JsonElement aValuesArray : ValuesArray) {
                                                CurrObj = aValuesArray.getAsJsonObject();
                                                String Title = Concurrent.tagsStringValidator(CurrObj, "examTitle");
                                                if (Title != null && !Title.equals(""))
                                                    LIST_DATA.add(new ExamsModel(Concurrent.tagsIntValidator(CurrObj, "id"), Title, Concurrent.tagsStringValidator(CurrObj, "examDescription"), Concurrent.tagsStringValidator(CurrObj, "examDate")));
                                            }

                                            if (LIST_DATA.size() == 0)
                                                mListManager.setNoDataView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(Res_PageList));
                                            else
                                                mListManager.setDataView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(Res_PageList));

                                            mListManager.getListAdapter().notifyDataSetChanged();
                                        } else {
                                            mListManager.setNoDataView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(Res_PageList));
                                        }


                                        if (ClassesValuesArray.size() > 0) {
                                            Iterator<JsonElement> ValsIter = ClassesValuesArray.iterator();
                                            ClassesList = new ArrayList<>();
                                            while (ValsIter.hasNext()) {
                                                CurrObj = ValsIter.next().getAsJsonObject();
                                                String Title = Concurrent.tagsStringValidator(CurrObj, "className");
                                                if (Title != null && !Title.equals(""))
                                                    ClassesList.add(new ClassesModel(Concurrent.tagsIntValidator(CurrObj, "id"), Title, Concurrent.tagsStringValidator(CurrObj, "classTeacher"), Concurrent.tagsStringValidator(CurrObj, "dormitoryId")));
                                            }

                                            if (ClassesList.size() == 0)
                                                mListManager.setNoDataView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(Res_PageList));
                                            else
                                                mListManager.setDataView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(Res_PageList));

                                            mListManager.getListAdapter().notifyDataSetChanged();
                                        } else {
                                            mListManager.setNoDataView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(Res_PageList));
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
                    }else{
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

            holder.TITLE = convertView.findViewById(R.id.header_title);
            holder.CONTENT = convertView.findViewById(R.id.header_content);
            holder.Date = convertView.findViewById(R.id.footer_deadline_data);
            holder.MENU_SHOW_MARKS = convertView.findViewById(R.id.footer_show_marks);
            holder.examDeadline = convertView.findViewById(R.id.examDeadline);
            holder.container = convertView.findViewById(R.id.holder);

            convertView.setTag(holder);
        } else {
            holder = (StaticPagesHolder) convertView.getTag();
        }
        posValues = LIST_DATA.get(position);
        if (posValues != null) {
            holder.CONTENT.setNotNullText(posValues.content);
            holder.TITLE.setNotNullText(posValues.title);
            holder.Date.setNotNullText(posValues.Date);

            holder.MENU_SHOW_MARKS.setTag(posValues.id);

            if (Concurrent.isUserHavePermission(this, "examsList.showMarks")) {
                holder.MENU_SHOW_MARKS.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent MyIntent = new Intent(getBaseContext(), ControlActivity.class);
                        MyIntent.putExtra("TARGET_FRAGMENT", "ExamsMarksChoose");
                        MyIntent.putExtra("EXTRA_LIST", ClassesList);
                        MyIntent.putExtra("EXTRA_INT_1", (Integer) v.getTag());
                        MyIntent.putExtra("EXTRA_HEAD_FIND_WORD", "examMarks");
                        MyIntent.putExtra("EXTRA_HEAD_REPLACE_WORD", "Exam Marks");
                        startActivity(MyIntent);
                        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                    }
                });
                holder.MENU_SHOW_MARKS.setVisibility(View.VISIBLE);
            }else{
                holder.MENU_SHOW_MARKS.setVisibility(View.GONE);
            }

            Concurrent.setLangWords(this, holder.examDeadline);
        }


        holder.container.setTag(posValues);
        if (Concurrent.isUserHavePermission(this, "examsList.View")) {
            holder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ExamsModel examsModel = (ExamsModel) v.getTag();

                    Intent MyIntent = new Intent(getBaseContext(), ExamDetails.class);
                    MyIntent.putExtra("exam_id", examsModel.id.toString());
                    MyIntent.putExtra("exam_title", examsModel.title);
                    MyIntent.putExtra("exam_subjects", subjectsList);
                    startActivity(MyIntent);
                    overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                }
            });
        }


        return convertView;
    }

    class StaticPagesHolder {
        RelativeLayout container;
        TextView examDeadline;
        ParentStyledTextView CONTENT;
        ParentStyledTextView TITLE;
        ParentStyledTextView Date;
        RelativeLayout MENU_SHOW_MARKS;
    }
}
