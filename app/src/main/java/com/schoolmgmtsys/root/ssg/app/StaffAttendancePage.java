package com.schoolmgmtsys.root.ssg.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.processbutton.iml.ActionProcessButton;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.schoolmgmtsys.root.ssg.R;
import com.schoolmgmtsys.root.ssg.models.StaffAttendanceStatModel;
import com.schoolmgmtsys.root.ssg.utils.App;
import com.schoolmgmtsys.root.ssg.utils.Concurrent;
import com.schoolmgmtsys.root.ssg.utils.Constants;
import com.schoolmgmtsys.root.ssg.utils.DrawerListFragment;
import com.schoolmgmtsys.root.ssg.fonts.ParentStyledTextView;
import com.solutionsbricks.solbricksframework.Call;
import com.solutionsbricks.solbricksframework.Callback;
import com.solutionsbricks.solbricksframework.FormBody;
import com.solutionsbricks.solbricksframework.OkHttpClient;
import com.solutionsbricks.solbricksframework.Request;
import com.solutionsbricks.solbricksframework.Response;
import com.solutionsbricks.solbricksframework.helpers.SBDatePickerDialog;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class StaffAttendancePage extends SlidingFragmentActivity implements SBDatePickerDialog.DatePickerMultiCalsInterface {

    private ActionProcessButton ProcessDataBtn;
    private SharedPreferences Prefs;
    private String chooseDate;
    private HashMap<Integer, String> statusIdentifierKeyFirst;
    private LinearLayout datePickerContainer;
    private ParentStyledTextView datePickerText;
    private String response;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Concurrent.getLangDirection(this).equals("ar"))
            getSlidingMenu().setMode(SlidingMenu.RIGHT);
        else getSlidingMenu().setMode(SlidingMenu.LEFT);

        setContentView(R.layout.page_staff_attendance_select);

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

        TextView HeadTitle = (TextView) findViewById(R.id.head_drawer_title);
        HeadTitle.setText(Concurrent.getLangSubWords("staffAttendance", "Teachers Attendance"));

        ImageView ToogleBtn = (ImageView) findViewById(R.id.head_drawer_toggle);
        ToogleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });


        statusIdentifierKeyFirst = new HashMap<>();
        statusIdentifierKeyFirst.put(1, Concurrent.getLangSubWords("Present", "Present"));
        statusIdentifierKeyFirst.put(0, Concurrent.getLangSubWords("Absent", "Absent"));
        statusIdentifierKeyFirst.put(2, Concurrent.getLangSubWords("Late", "Late"));
        statusIdentifierKeyFirst.put(3, Concurrent.getLangSubWords("LateExecuse", "Late with excuse"));
        statusIdentifierKeyFirst.put(-1, "Not Defined");


        Prefs = PreferenceManager.getDefaultSharedPreferences(this);

        final String dateLang = Concurrent.getLangSubWords("Date", "Date");
        final String searchLang = Concurrent.getLangSubWords("Search", "Search");

        findViewById(R.id.gen_loader).setVisibility(View.GONE);


        ProcessDataBtn = (ActionProcessButton) findViewById(R.id.process_data);
        ProcessDataBtn.setMode(ActionProcessButton.Mode.ENDLESS);
        ProcessDataBtn.setText(searchLang);

        ProcessDataBtn.setOnClickListener(new View.OnClickListener() {
            private String TOKEN;

            @Override
            public void onClick(View v) {
                TOKEN = Concurrent.getAppToken(StaffAttendancePage.this);
                if (TOKEN != null) {
                    if (chooseDate != null) {
                        ProcessDataBtn.setEnabled(false);
                        ProcessDataBtn.setProgress(10);

                        FormBody.Builder formBody = new FormBody.Builder();
                        formBody.add("attendanceDay", String.valueOf(chooseDate));


                        OkHttpClient client = new OkHttpClient().newBuilder(getBaseContext()).connectTimeout(7, TimeUnit.SECONDS).build();

                        Request.Builder requestBuilder = new Request.Builder()
                                .url(App.getAppBaseUrl() + Constants.TASK_STAFF_ATTENDANCE_SELECT_POST);

                        requestBuilder.post(formBody.build());

                        Request request = requestBuilder.build();


                        Call call = client.newCall(request);
                        call.enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, final IOException e) {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        ProcessDataBtn.setProgress(-1);
                                        ProcessDataBtn.setEnabled(true);
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
                                                        ProcessDataBtn.setProgress(-1);
                                                        ProcessDataBtn.setEnabled(true);
                                                        Toast.makeText(StaffAttendancePage.this, Concurrent.getLangSubWords("errorOccurred","Error Occurred"), Toast.LENGTH_LONG).show();
                                                        return;
                                                    }

                                                    ArrayList<StaffAttendanceStatModel> attendanceList = new ArrayList<>();
                                                    JsonArray ValuesArray = ValuesHolder.getAsJsonArray("teachers");
                                                    if (ValuesArray != null ) {
                                                        if(ValuesArray.size() == 0){
                                                            ProcessDataBtn.setProgress(-1);
                                                            ProcessDataBtn.setEnabled(true);
                                                            ProcessDataBtn.setText("Empty Result");
                                                            return;
                                                        }

                                                        for(JsonElement obj:ValuesArray){
                                                            JsonObject objResult = obj.getAsJsonObject();

                                                            String attendWord = Concurrent.repairJsonValueQuotes(objResult.get("attendance").toString());
                                                            Integer attendanceId = -1;
                                                            if (attendWord != null && !attendWord.equals(""))
                                                                attendanceId = Integer.valueOf(attendWord);
                                                            attendanceList.add(new StaffAttendanceStatModel(
                                                                    Integer.valueOf(objResult.get("id").toString()),
                                                                    Concurrent.repairJsonValueQuotes(objResult.get("name").toString()),
                                                                    statusIdentifierKeyFirst.get(attendanceId),
                                                                    objResult.has("vacation") ? objResult.get("vacation").toString():null,
                                                                    objResult.has("vacationStat") ? objResult.get("vacationStat").toString():null,
                                                                    objResult.has("check_in_time") ? objResult.get("check_in_time").toString().replaceAll("\"", ""):null,
                                                                    objResult.has("check_out_time") ? objResult.get("check_out_time").toString().replaceAll("\"", ""):null,
                                                                    objResult.has("attNotes") ? objResult.get("attNotes").toString():null));
                                                        }


                                                        ProcessDataBtn.setProgress(100);
                                                        ProcessDataBtn.setEnabled(true);
                                                        Intent i = new Intent(StaffAttendancePage.this, StaffAttendanceStatPage.class);
                                                        i.putExtra("staffAttendanceList", attendanceList);
                                                        i.putExtra("attendanceDay", String.valueOf(chooseDate));

                                                        startActivity(i);
                                                    } else {
                                                        ProcessDataBtn.setProgress(-1);
                                                        ProcessDataBtn.setEnabled(true);
                                                        ProcessDataBtn.setText(Concurrent.getLangSubWords("errorOccurred","Error Occurred"));
                                                    }
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
                    } else {
                        ProcessDataBtn.setText("Error, Please select required data");
                        ProcessDataBtn.setProgress(-1);
                        ProcessDataBtn.setEnabled(true);
                    }
                }
            }
        });


        datePickerContainer = (LinearLayout) findViewById(R.id.date_input_con);
        datePickerText = (ParentStyledTextView) findViewById(R.id.date_input);


        datePickerContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //============================= Open View When Click  ====================//
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                Fragment prev = getSupportFragmentManager().findFragmentByTag("staff_attendance");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);

                // Create and show the dialog.
                DialogFragment newFragment = new SBDatePickerDialog().newInstance(getBaseContext(), "staff_attendance",Concurrent.DateFormat);
                newFragment.show(ft, "staff_attendance");

            }
        });


    }

    public void showError(String errorCode) {
        String errorTitle;
        if (errorCode != null) {
            errorTitle = Concurrent.getLangSubWords("errorOccurred", "Error Occurred") + " ( Error Code: " + errorCode + " )";
        } else {
            errorTitle = Concurrent.getLangSubWords("errorOccurred", "Error Occurred");
        }
        ProcessDataBtn.setProgress(-1);
        ProcessDataBtn.setEnabled(true);
        Toast.makeText(getBaseContext(), errorTitle, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDatePicked(Intent data) {
        Bundle bundle = data.getExtras();
        String SelectedDateAsString = bundle.getString("date");
        //String SelectedDateAsTimeStamp = bundle.getString("timestamp");       // Not used
        //String senderTag = bundle.getString("tag");                           // Not used

        datePickerText.setText(SelectedDateAsString);
        chooseDate = SelectedDateAsString;
    }
}
