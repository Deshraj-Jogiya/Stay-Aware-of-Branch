package com.schoolmgmtsys.root.ssg.app;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.util.Log;
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
import com.solutionsbricks.solbricksframework.helpers.CustomImageView;

import java.io.IOException;
import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by SolutionsBricks Mobile Dev. Team.
 */

public class HomeworkView extends SlidingFragmentActivity {

    private String homeworkID;
    public HashMap<String, String> appliedStudents = new HashMap<>();
    public HashMap<String, String> notAppliedStudents = new HashMap<>();
    JsonObject ValuesHolder = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Concurrent.getLangDirection(this).equals("ar"))
            getSlidingMenu().setMode(SlidingMenu.RIGHT);
        else getSlidingMenu().setMode(SlidingMenu.LEFT);

        setContentView(R.layout.page_homework_view);

        ImageView backImage = (ImageView) findViewById(R.id.background_img);
        FrameLayout logBack = (FrameLayout) findViewById(R.id.full_layout);
        TextView HeadTitle = (TextView) findViewById(R.id.head_drawer_title);
        HeadTitle.setText(Concurrent.getLangSubWords("Homework", "Homework"));

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

        homeworkID = getIntent().getStringExtra("homework_id");

        ImageView ToggleBtn = (ImageView) findViewById(R.id.head_drawer_toggle);
        ToggleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        if (homeworkID != null) {
            loadData();
        } else {
            changePageView(pageLayer.ErrorLoading);
        }


        findViewById(R.id.error_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (homeworkID != null) {
                    loadData();
                } else {
                    changePageView(pageLayer.ErrorLoading);
                }
            }
        });

        if (Concurrent.isUserHavePermission(getBaseContext(), "Homework.Answers")) {
            findViewById(R.id.answers_con).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.answers_con).setVisibility(View.GONE);
        }

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
                    .url(App.getAppBaseUrl() + Constants.TASK_LOAD_HOMEWORK_DETAILS + "/" + homeworkID);

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
                    final Response responseObj = (Response) serverResponse;
                    final String response;
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

                                        try {
                                            ValuesHolder = parser.parse(response).getAsJsonObject();
                                        } catch (Exception e) {
                                            showError("5001");
                                        }

                                        if (ValuesHolder == null) {
                                            changePageView(pageLayer.ErrorLoading);
                                            return;
                                        }

                                        /**********************************
                                         *  Header Part
                                         *  *******************************
                                         */

                                        //===== homeworkTitle ======//
                                        String homeworkTitle = Concurrent.tagsStringValidator(ValuesHolder, "homeworkTitle");
                                        if (!homeworkTitle.equals(""))
                                            ((TextView) findViewById(R.id.homeworkTitle)).setText(homeworkTitle);

                                        //===== homeworkDescription ======//
                                        String homeworkDescription = Concurrent.tagsStringValidator(ValuesHolder, "homeworkDescription");
                                        if (!homeworkDescription.equals(""))
                                            ((TextView) findViewById(R.id.homeworkDescription)).setText(Concurrent.fromHtml(homeworkDescription));


                                        //===== From User Photo ======//
                                        String userID = Concurrent.tagsStringValidator(ValuesHolder, "teacherId");
                                        if (!userID.equals("")) {
                                            CustomImageView userImage = (CustomImageView) findViewById(R.id.teacherPhoto);
                                            userImage.profileID = String.valueOf(userID);
                                            userImage.load();
                                        }

                                        //===== Teacher photo ======//
                                        if (ValuesHolder.has("teacher")) {
                                            JsonObject teacherObj = ValuesHolder.get("teacher").getAsJsonObject();

                                            String fullName = Concurrent.tagsStringValidator(teacherObj, "fullName");
                                            if (!fullName.equals(""))
                                                ((TextView) findViewById(R.id.teacherName)).setText(fullName);
                                        }

                                        //===== Date ======//
                                        String homeworkDate = Concurrent.tagsStringValidator(ValuesHolder, "homeworkDate");
                                        if (!homeworkDate.equals(""))
                                            ((TextView) findViewById(R.id.date)).setText(homeworkDate);

                                        //===== Sub - Eva. Date ======//
                                        String homeworkSubmissionDate = Concurrent.tagsStringValidator(ValuesHolder, "homeworkSubmissionDate");
                                        String homeworkEvaluationDate = Concurrent.tagsStringValidator(ValuesHolder, "homeworkEvaluationDate");
                                        ((TextView) findViewById(R.id.sub_eva_date)).setText(homeworkSubmissionDate + " - " + homeworkEvaluationDate);

                                        //===== Classes ======//
                                        JsonArray classesArray = new JsonArray();
                                        if (ValuesHolder.has("classes") && !ValuesHolder.get("classes").isJsonNull()) {
                                            classesArray = ValuesHolder.get("classes").getAsJsonArray();
                                            if (classesArray.size() > 0) {
                                                String classesString = "";
                                                Integer i = 1;
                                                for (JsonElement classObj : classesArray) {
                                                    JsonObject CurrObj = classObj.getAsJsonObject();
                                                    if (CurrObj != null) {
                                                        classesString += Concurrent.tagsStringValidator(CurrObj, "className");
                                                        if (classesArray.size() != i)
                                                            classesString += " , ";
                                                    }
                                                    i++;
                                                }
                                                ((TextView) findViewById(R.id.classes)).setText(classesString);
                                            }
                                        }

                                        //===== Sections ======//
                                        JsonArray sectionsArray = new JsonArray();
                                        if (ValuesHolder.has("sections") && !ValuesHolder.get("sections").isJsonNull()) {
                                            sectionsArray = ValuesHolder.get("sections").getAsJsonArray();
                                            if (sectionsArray.size() > 0) {
                                                String sectionsString = "";
                                                Integer i = 1;
                                                for (JsonElement classObj : sectionsArray) {
                                                    JsonObject CurrObj = classObj.getAsJsonObject();
                                                    if (CurrObj != null) {
                                                        sectionsString += Concurrent.tagsStringValidator(CurrObj, "sectionName");
                                                        if (sectionsArray.size() != i)
                                                            sectionsString += " , ";
                                                    }
                                                    i++;
                                                }
                                                ((TextView) findViewById(R.id.sections)).setText(Concurrent.getLangSubWords("sections", "Sections") + " : " + sectionsString);
                                            }
                                        }

                                        //===== Subject ======//
                                        if (ValuesHolder.has("subject") && !ValuesHolder.get("subject").isJsonNull()) {
                                            JsonObject subject = ValuesHolder.get("subject").getAsJsonObject();
                                            if (subject != null) {
                                                String subjectTitle = Concurrent.tagsStringValidator(subject, "subjectTitle");
                                                if (!subjectTitle.equals(""))
                                                    ((TextView) findViewById(R.id.subjects)).setText(Concurrent.getLangSubWords("subjects", "Subjects") + " : " + subjectTitle);
                                            }
                                        }

                                        //===== applied students ======//
                                        if (ValuesHolder.has("student_applied") && !ValuesHolder.get("student_applied").isJsonNull() && ValuesHolder.get("student_applied").isJsonObject()) {
                                            JsonObject student_applied = ValuesHolder.get("student_applied").getAsJsonObject();
                                            if (student_applied != null) {
                                                LinearLayout answersAppliedLayout = (LinearLayout) findViewById(R.id.answers_applied_layout);
                                                answersAppliedLayout.removeAllViews();
                                                for (Map.Entry<String, JsonElement> entry : student_applied.entrySet()) {
                                                    String userNameValue = entry.getValue().getAsString();
                                                    String userIDValue = entry.getKey();

                                                    appliedStudents.put(userIDValue, userNameValue);
                                                    View userView = createUserLayout(userNameValue, userIDValue, true);
                                                    answersAppliedLayout.addView(userView);

                                                    userView.setTag(userIDValue);
                                                    userView.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            String stdID = (String) view.getTag();
                                                            applyStudent(homeworkID, stdID, false);
                                                        }
                                                    });

                                                }
                                            }
                                        }

                                        //===== not applied students ======//
                                        if (ValuesHolder.has("student_not_applied") && !ValuesHolder.get("student_not_applied").isJsonNull() && ValuesHolder.get("student_not_applied").isJsonObject()) {
                                            JsonObject student_not_applied = ValuesHolder.get("student_not_applied").getAsJsonObject();
                                            if (student_not_applied != null) {
                                                LinearLayout answersNotAppliedLayout = (LinearLayout) findViewById(R.id.answers_not_applied_layout);
                                                answersNotAppliedLayout.removeAllViews();
                                                for (Map.Entry<String, JsonElement> entry : student_not_applied.entrySet()) {
                                                    String userNameValue = entry.getValue().getAsString();
                                                    String userIDValue = entry.getKey();

                                                    notAppliedStudents.put(entry.getValue().getAsString(), entry.getKey());
                                                    View userView = createUserLayout(userNameValue, userIDValue, false);
                                                    answersNotAppliedLayout.addView(userView);

                                                    userView.setTag(userIDValue);
                                                    userView.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            String stdID = (String) view.getTag();
                                                            applyStudent(homeworkID, stdID, true);
                                                        }
                                                    });


                                                }
                                            }
                                        }


                                        //===== download ======//
                                        if (Concurrent.isUserHavePermission(getBaseContext(), "Homework.Download")) {
                                            final String hFile = Concurrent.tagsStringValidator(ValuesHolder, "homeworkFile");
                                            if (!hFile.equals("")) {
                                                findViewById(R.id.menu_download).setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        new Downloader().downloadFile(HomeworkView.this, hFile, App.getAppBaseUrl() + Constants.TASK_DOWNLOAD_HOMEWORK + "/" + homeworkID);
                                                    }
                                                });
                                            } else {
                                                findViewById(R.id.menu_download).setVisibility(View.GONE);
                                            }
                                        } else {
                                            findViewById(R.id.menu_download).setVisibility(View.GONE);
                                        }


                                        changePageView(pageLayer.DataView);
                                    } else {
                                        showError("5010");
                                    }
                                } catch (final Exception e) {
                                    showError("5002");
                                    Log.v("mtag", "sdds " + e);
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

    public void applyStudent(final String homeworkID, String studentID, Boolean isAppliedHomework) {
        String TOKEN = Concurrent.getAppToken(this);
        Toast.makeText(HomeworkView.this, Concurrent.getLangSubWords("pleaseWait", "Please Wait"), Toast.LENGTH_SHORT).show();

        FormBody.Builder formBody = new FormBody.Builder();
        formBody.add("status", isAppliedHomework ? "1" : "0");
        formBody.add("student", studentID);

        OkHttpClient client = new OkHttpClient().newBuilder(getBaseContext()).connectTimeout(7, TimeUnit.SECONDS).build();

        Request.Builder requestBuilder = new Request.Builder()
                .url(App.getAppBaseUrl() + Constants.TASK_APPLY_HOMEWORK + "/" + homeworkID);

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
                final String response;
                try {
                    response = responseObj.body().string();
                } catch (Exception e) {
                    Toast.makeText(HomeworkView.this, Concurrent.getLangSubWords("errorOccurred", "Error Occurred"), Toast.LENGTH_LONG).show();
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
                                        Toast.makeText(HomeworkView.this, Concurrent.getLangSubWords("errorOccurred", "Error Occurred"), Toast.LENGTH_LONG).show();
                                    }

                                    if (ValuesHolder != null) {
                                        if (Concurrent.tagsStringValidator(ValuesHolder, "status").equals("success")) {
                                            loadData();
                                        } else {
                                            Toast.makeText(HomeworkView.this, Concurrent.getLangSubWords("errorOccurred", "Error Occurred"), Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        Toast.makeText(HomeworkView.this, Concurrent.getLangSubWords("errorOccurred", "Error Occurred"), Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    showError(Concurrent.checkErrorType(getBaseContext(), response));
                                }
                            } catch (final Exception e) {

                                Toast.makeText(HomeworkView.this, Concurrent.getLangSubWords("errorOccurred", "Error Occurred"), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else {
                    Log.v("mtag", "gbbb ");

                    Toast.makeText(HomeworkView.this, Concurrent.getLangSubWords("errorOccurred", "Error Occurred"), Toast.LENGTH_LONG).show();
                }
            }
        });


    }

    public View createUserLayout(String userName, String userPhotoID, Boolean isOverlayDelete) {
        View itemView = getLayoutInflater().inflate(R.layout.page_homework_user, null);

        ((TextView) itemView.findViewById(R.id.userName)).setText(userName);
        ImageView actionImage = itemView.findViewById(R.id.action_btn);

        if (isOverlayDelete) {
            actionImage.setImageResource(R.drawable.menu_delete);
        } else {
            actionImage.setImageResource(R.drawable.icon_pages_pass);
        }

        CustomImageView userImage = itemView.findViewById(R.id.userPhoto);
        userImage.profileID = String.valueOf(userPhotoID);
        userImage.load();

        return itemView;
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

}