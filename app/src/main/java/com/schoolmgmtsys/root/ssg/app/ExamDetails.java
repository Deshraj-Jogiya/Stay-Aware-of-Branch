package com.schoolmgmtsys.root.ssg.app;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.schoolmgmtsys.root.ssg.R;
import com.schoolmgmtsys.root.ssg.models.ExamSubjectModel;
import com.schoolmgmtsys.root.ssg.utils.App;
import com.schoolmgmtsys.root.ssg.utils.Concurrent;
import com.schoolmgmtsys.root.ssg.utils.Constants;
import com.solutionsbricks.solbricksframework.Call;
import com.solutionsbricks.solbricksframework.Callback;
import com.solutionsbricks.solbricksframework.OkHttpClient;
import com.solutionsbricks.solbricksframework.Request;
import com.solutionsbricks.solbricksframework.Response;
import com.schoolmgmtsys.root.ssg.utils.DrawerListFragment;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by SolutionsBricks Mobile Dev. Team.
 */

public class ExamDetails extends SlidingFragmentActivity {

    private String examTitle;
    private String examID;
    private HashMap<String,String> subjectsMap = new HashMap<>();
    private String response;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Concurrent.getLangDirection(this).equals("ar"))
            getSlidingMenu().setMode(SlidingMenu.RIGHT);
        else getSlidingMenu().setMode(SlidingMenu.LEFT);

        setContentView(R.layout.page_exam_details);

        ImageView backImage = (ImageView) findViewById(R.id.background_img);
        FrameLayout logBack = (FrameLayout) findViewById(R.id.full_layout);

        Resources res = getResources();
        if (!res.getBoolean(R.bool.x_gen_back_is_image)) {
            backImage.setVisibility(View.GONE);
            logBack.setBackgroundColor(Concurrent.getColor(this, R.color.x_gen_back));
        }

        getSlidingMenu().setBehindWidth((int) Math.round(Concurrent.getScreenWidth(this) * 70.0 / 100.0));
        setBehindContentView(R.layout.drawer_frame);

        findViewById(R.id.gen_loader).setVisibility(View.GONE);

        ListFragment mFrag;
        FragmentTransaction t = this.getSupportFragmentManager().beginTransaction();
        if (savedInstanceState == null) {
            mFrag = new DrawerListFragment();
        } else {
            mFrag = (ListFragment) this.getSupportFragmentManager().findFragmentById(R.id.menu_frame);
        }
        t.replace(R.id.menu_frame, mFrag);
        t.commit();

        examTitle = getIntent().getStringExtra("exam_title");
        examID = getIntent().getStringExtra("exam_id");
        ArrayList<ExamSubjectModel> examSubjects = getIntent().getParcelableArrayListExtra("exam_subjects");

        try{
            for(ExamSubjectModel subject : examSubjects){
                subjectsMap.put(subject.id,subject.Name);
            }
        }catch (Exception ignored ){}



        TextView HeadTitle = (TextView) findViewById(R.id.head_drawer_title);
        HeadTitle.setText(examTitle);

        ImageView ToggleBtn = (ImageView) findViewById(R.id.head_drawer_toggle);
        ToggleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        if(examID != null){
            loadData();
        }else{
            changePageView(pageLayer.ErrorLoading);
        }


        findViewById(R.id.error_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(examID != null){
                    loadData();
                }else{
                    changePageView(pageLayer.ErrorLoading);
                }
            }
        });


    }


    private enum pageLayer {
        ErrorLoading,
        DataView,
        Loading
    }

    public void changePageView(pageLayer layerIndex) {
        findViewById(R.id.error_view).setVisibility(layerIndex.equals(pageLayer.ErrorLoading) ? View.VISIBLE : View.GONE);
        findViewById(R.id.loading_view).setVisibility(layerIndex.equals(pageLayer.Loading) ? View.VISIBLE : View.GONE);
        findViewById(R.id.data_view).setVisibility(layerIndex.equals(pageLayer.DataView) ? View.VISIBLE : View.GONE);
    }

    public void loadData() {
        String TOKEN = Concurrent.getAppToken(getBaseContext());
        if (TOKEN != null) {
            changePageView(pageLayer.Loading);


            OkHttpClient client = new OkHttpClient().newBuilder(getBaseContext()).connectTimeout(7, TimeUnit.SECONDS).build();

            Request.Builder requestBuilder = new Request.Builder()
                    .url(App.getAppBaseUrl() + Constants.TASK_EXAM_DETAILS + "/" + examID);

            requestBuilder.get();

            Request request = requestBuilder.build();


            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, final IOException e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            changePageView(pageLayer.ErrorLoading);
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

                                        if (ValuesHolder == null) {
                                            changePageView(pageLayer.ErrorLoading);
                                            return;
                                        }

                                        String examClasses = "";
                                        JsonArray examClassesNames = ValuesHolder.get("examClassesNames").getAsJsonArray();
                                        JsonObject CurrObj;

                                        if(examClassesNames != null && examClassesNames.size() > 0){
                                            for(int i = 0; i < examClassesNames.size(); i++){

                                                CurrObj = examClassesNames.get(i).getAsJsonObject();

                                                if(CurrObj != null && CurrObj.has("className")){
                                                    examClasses += Concurrent.tagsStringValidator(CurrObj,"className");
                                                    if(i != examClassesNames.size() -1) examClasses += " , ";
                                                }

                                            }

                                        }

                                        setExamDetailsView(Concurrent.tagsStringValidator(ValuesHolder,"examTitle"),
                                                Concurrent.tagsStringValidator(ValuesHolder,"examDescription"),
                                                examClasses,
                                                Concurrent.tagsStringValidator(ValuesHolder,"examDate"),
                                                Concurrent.tagsStringValidator(ValuesHolder,"examEndDate"),
                                                ValuesHolder.get("examSchedule").getAsJsonArray()
                                        );


                                        changePageView(pageLayer.DataView);
                                    } else {
                                        showError("5010");
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


    public void showError(String errorCode) {
        String errorTitle;
        if (errorCode != null) {
            errorTitle = Concurrent.getLangSubWords("errorOccurred", "Error Occurred") + " ( Error Code: " + errorCode + " )";
        } else {
            errorTitle = Concurrent.getLangSubWords("errorOccurred", "Error Occurred");
        }
        changePageView(pageLayer.ErrorLoading);
        Toast.makeText(getBaseContext(), errorTitle, Toast.LENGTH_LONG).show();
    }

    public void setExamDetailsView(String examTitle,String examDescription,String examClasses,String examDate,String examEndDate,JsonArray schArrayList){
        if(examTitle != null) ((TextView)findViewById(R.id.examTitle)).setText(examTitle);
        if(examDescription != null) ((TextView)findViewById(R.id.examDescription)).setText(examDescription);
        if(examClasses != null) ((TextView)findViewById(R.id.examClassesNames)).setText(examClasses);
        if(examDate != null) ((TextView)findViewById(R.id.examDate)).setText(examDate);
        if(examEndDate != null) ((TextView)findViewById(R.id.examEndDate)).setText(examEndDate);

        LinearLayout examSchParentCon = (LinearLayout) findViewById(R.id.examScheduleCon);
        LinearLayout examSchCon = (LinearLayout) findViewById(R.id.examSchedule);

        if(schArrayList != null && schArrayList.size() > 0){
            examSchParentCon.setVisibility(View.VISIBLE);

            for(JsonElement schItem:schArrayList){
                JsonObject CurrObj = schItem.getAsJsonObject();
                if(CurrObj != null){
                    View schConItem = getLayoutInflater().inflate(R.layout.page_exam_details_sch_item, null);

                    String subjectID = Concurrent.tagsStringValidator(CurrObj,"subject");
                    if(subjectsMap.containsKey(subjectID))((TextView)schConItem.findViewById(R.id.subject)).setText(subjectsMap.get(subjectID));

                    ((TextView)schConItem.findViewById(R.id.stDate)).setText(Concurrent.tagsStringValidator(CurrObj,"stDate"));
                    examSchCon.addView(schConItem);
                }else{
                    examSchParentCon.setVisibility(View.GONE);
                }
            }
        }else{
            examSchParentCon.setVisibility(View.GONE);
        }

    }
}